package pe.edu.pucp.admitu.pruebas;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import pe.edu.pucp.admitu.config.ActorContext;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionHistorialBOImpl;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionHistorialImpl;

/** Integración opt-in. Fixtures íntegramente sintéticos y rollback final sin borrar históricos. */
public final class PruebasAuditoriaPreaws {
    private static int checks;
    private static final LocalDate HOY=LocalDate.now(ZoneId.of("America/Lima"));
    private static void check(boolean value,String label) {
        if (!value) throw new AssertionError(label);
        System.out.println("PASS "+(++checks)+" "+label);
    }
    @FunctionalInterface private interface SqlAction { void run() throws Exception; }
    private static int insert(Connection c,String sql,Object... values) throws SQLException {
        try (PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            for (int i=0;i<values.length;i++) s.setObject(i+1,values[i]);
            s.executeUpdate();
            try (ResultSet r=s.getGeneratedKeys()) { if (!r.next()) throw new SQLException("Falta clave");return r.getInt(1); }
        }
    }
    private static String text(Connection c,String sql) throws SQLException {
        try (Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)) {r.next();return r.getString(1);}
    }
    private static int number(Connection c,String sql) throws SQLException {return new java.math.BigDecimal(text(c,sql)).intValueExact();}
    private static void sql(Connection c,String sql) throws SQLException {try(Statement s=c.createStatement()){s.execute(sql);}}
    private static void actor(Connection c,String rol,int persona) throws SQLException {
        try (PreparedStatement s=c.prepareStatement("SET @admitu_actor_rol=?,@admitu_actor_persona=?,@admitu_actor_cuenta=91,@admitu_actor=?")) {
            s.setString(1,rol);s.setInt(2,persona);s.setString(3,rol+":"+persona+" cuenta:91");s.execute();
        }
    }
    private static int persona(Connection c,String tipo) throws SQLException {
        String tag=UUID.randomUUID().toString().replace("-","");
        int id=insert(c,"INSERT INTO persona(nombres,apellido_paterno,correo,tipo_documento,numero_documento) VALUES('Auditoria','Fixture',?,'CE',?)","preaws-"+tag+"@example.test",tag.substring(0,18));
        sql(c,"INSERT INTO "+tipo+"(persona_id) VALUES("+id+")");return id;
    }
    private record Oferta(int convocatoria,int modalidad,int oferta) { }
    private static Oferta oferta(Connection c,int diasFin,int modalidad,int carrera,int tarifa) throws SQLException {
        int conv=insert(c,"INSERT INTO convocatoria(codigo_convocatoria,nombre,fecha_inicio,fecha_fin,estado) VALUES(?,'Ensayo auditoría',?,?,'PUBLICADA')","AUD-"+UUID.randomUUID().toString().substring(0,15),HOY.minusDays(3),HOY.plusDays(diasFin));
        int cm=insert(c,"INSERT INTO convocatoria_modalidad(convocatoria_id,modalidad_id,costo_inscripcion) VALUES(?,?,?)",conv,modalidad,tarifa);
        int oc=insert(c,"INSERT INTO oferta_carrera(convocatoria_id,carrera_id,cantidad_vacantes) VALUES(?,?,10)",conv,carrera);
        return new Oferta(conv,cm,oc);
    }
    private static int postular(Connection c,int persona,Oferta o) throws SQLException {
        try (CallableStatement s=c.prepareCall("{call INSERTAR_POSTULACION(?,?,?,?,?,?,?,?)}")) {
            s.registerOutParameter(1,Types.INTEGER);s.setInt(2,persona);s.setInt(3,o.convocatoria());s.setInt(4,o.modalidad());s.setInt(5,o.oferta());s.setInt(6,2);s.setString(7,"AUD-"+UUID.randomUUID().toString().substring(0,15));s.setString(8,"Fixture sintético");s.execute();return s.getInt(1);
        }
    }
    private static String snapshot(Connection c) throws SQLException {
        // Huella de todos los campos, sin GROUP_CONCAT truncable ni datos personales en stdout.
        // Todas estas tablas tienen una clave primaria estable en su primera columna.
        String[] tablas={"persona","postulante","evaluador","convocatoria","convocatoria_modalidad",
            "oferta_carrera","requisito_convocatoria_modalidad","postulacion","pago",
            "postulacion_historial","documento_postulacion","documento_observacion"};
        try {
            var digest=java.security.MessageDigest.getInstance("SHA-256");
            for (String tabla:tablas) {
                digest.update(tabla.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                try (Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM "+tabla+" ORDER BY 1")) {
                    int columnas=r.getMetaData().getColumnCount();
                    while(r.next()) {
                        digest.update((byte) 0x7e);
                        for(int i=1;i<=columnas;i++) {
                            byte[] value=r.getBytes(i);
                            digest.update(java.nio.ByteBuffer.allocate(4).putInt(value==null?-1:value.length).array());
                            if(value!=null)digest.update(value);
                        }
                    }
                }
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch(java.security.NoSuchAlgorithmException impossible) {throw new IllegalStateException(impossible);}
    }
    private static void reject(Connection c,String label,SqlAction action) throws Exception {
        String before=snapshot(c);boolean rejected=false;
        try {action.run();} catch(SQLException ex) { if(!"45000".equals(ex.getSQLState()))throw ex;rejected=true; }
        check(rejected,label);check(before.equals(snapshot(c)),label+" conserva estado SQL");
    }
    private static void readonly() {
        var bo=new PostulacionHistorialBOImpl();var dao=new PostulacionHistorialImpl();
        Runnable[] attempts={()->bo.insertar(null),()->bo.modificar(null),()->bo.eliminar(1),()->dao.insertar(null),()->dao.modificar(null),()->dao.eliminar(1)};
        for (Runnable attempt:attempts) {boolean denied=false;try{attempt.run();}catch(UnsupportedOperationException expected){denied=true;}check(denied,"historial BO/DAO inmutable antes de acceder a MySQL");}
    }
    private static void context() throws Exception {
        try {
            ActorContext.set(91,73,"evaluador");
            try (Connection c=DBManager.getInstance().getConnection()) {
                check("evaluador:73 cuenta:91".equals(text(c,"SELECT @admitu_actor")),"JDBC toma actor del contexto del servidor");
                check(number(c,"SELECT @admitu_actor_persona")==73,"JDBC fija identidad para ámbito SQL");
            }
            ActorContext.clear();
            try (Connection c=DBManager.getInstance().getConnection()) {
                check("SISTEMA".equals(text(c,"SELECT @admitu_actor"))&&number(c,"SELECT @admitu_actor_rol IS NULL")==1,"limpiar contexto evita heredar identidad");
            }
            ActorContext.set(91,0,"admin");
            try (Connection c=DBManager.getInstance().getConnection()) {check("admin:0 cuenta:91".equals(text(c,"SELECT @admitu_actor")),"administrador sin persona conserva su cuenta autenticada");}
        } finally {ActorContext.clear();}
    }
    public static void main(String[] args) throws Exception {
        if (!"true".equals(System.getenv("ADMITU_PRUEBAS_ESCRITURA"))) throw new IllegalStateException("Solo admitu_db ensayo propia verificada; active ADMITU_PRUEBAS_ESCRITURA=true");
        readonly();context();
        try(Connection c=DBManager.getInstance().getConnection()) {
            if(!"admitu_db".equals(text(c,"SELECT DATABASE()")))throw new IllegalStateException("Esquema no autorizado");
            String antesFixtures=snapshot(c);
            c.setAutoCommit(false);
            try {
                int p1=persona(c,"postulante"),p2=persona(c,"postulante"),e1=persona(c,"evaluador"),e2=persona(c,"evaluador");
                int modalidad=number(c,"SELECT id FROM modalidad WHERE activo=1 LIMIT 1"),carrera=number(c,"SELECT id FROM carrera WHERE activo=1 LIMIT 1");
                Oferta antes=oferta(c,1,modalidad,carrera,125),limite=oferta(c,0,modalidad,carrera,480),despues=oferta(c,-1,modalidad,carrera,125);
                actor(c,"postulante",p1);
                int post1=postular(c,p1,antes),post2=postular(c,p1,limite);
                check(post1>0,"inscripción antes del cierre aceptada");check(post2>0,"día límite de Lima inclusivo aceptado");
                reject(c,"inscripción después del cierre rechazada",()->postular(c,p1,despues));
                check(number(c,"SELECT monto FROM pago WHERE postulacion_id="+post1)==125&&number(c,"SELECT monto FROM pago WHERE postulacion_id="+post2)==480,"misma modalidad dos convocatorias conserva tarifas SQL distintas");
                check(text(c,"SELECT responsable_cambio FROM postulacion_historial WHERE postulacion_id="+post1).equals("postulante:"+p1+" cuenta:91"),"historial inicial registra actor del servidor");
                reject(c,"postulante no crea expediente de tercero",()->postular(c,p2,antes));
                actor(c,"evaluador",e1);
                reject(c,"evaluador no crea postulaciones",()->postular(c,p2,antes));
                reject(c,"evaluador no asignado no cambia estado",()->sql(c,"CALL MODIFICAR_POSTULACION("+post1+",7,'Intento ajeno')"));
                sql(c,"UPDATE postulacion SET evaluador_id="+e1+" WHERE id="+post1);
                sql(c,"CALL MODIFICAR_POSTULACION("+post1+",7,'Revisión asignada sintética')");
                check(number(c,"SELECT estado_actual_id FROM postulacion WHERE id="+post1)==7,"evaluador asignado realiza transición permitida");
                check(text(c,"SELECT responsable_cambio FROM postulacion_historial WHERE postulacion_id="+post1+" ORDER BY id DESC LIMIT 1").equals("evaluador:"+e1+" cuenta:91"),"transición legítima conserva autor autenticado");
                actor(c,"evaluador",e2);
                reject(c,"segundo evaluador no modifica expediente del primero",()->sql(c,"CALL MODIFICAR_POSTULACION("+post1+",7,'Intento')"));
                actor(c,"admin",0);
                reject(c,"historial SQL no admite inserciones administrativas",()->sql(c,"CALL INSERTAR_POSTULACION_HISTORIAL(@h,"+post1+",2,7,CURRENT_DATE(),'Falso','Falso')"));
                int historia=number(c,"SELECT MIN(id) FROM postulacion_historial WHERE postulacion_id="+post1);
                reject(c,"historial SQL no admite modificaciones administrativas",()->sql(c,"CALL MODIFICAR_POSTULACION_HISTORIAL("+historia+","+post1+",2,7,CURRENT_DATE(),'Falso','Falso')"));
                reject(c,"historial SQL no admite borrado administrativo",()->sql(c,"CALL ELIMINAR_POSTULACION_HISTORIAL("+historia+")"));
                sql(c,"UPDATE postulacion SET evaluador_id="+e1+" WHERE id="+post2);
                int requisito=number(c,"SELECT id FROM requisito WHERE activo=1 LIMIT 1");
                int rm=insert(c,"INSERT INTO requisito_convocatoria_modalidad(convocatoria_modalidad_id,requisito_id,obligatorio) VALUES(?,?,1)",limite.modalidad(),requisito);
                int doc=insert(c,"INSERT INTO documento_postulacion(postulacion_id,requisito_aplicable_id,nombre_archivo,tipo_archivo,tamanio_archivo,ruta_archivo) VALUES(?,?,'fixture.pdf','PDF',12,'archivo:fixture')",post2,rm);
                actor(c,"evaluador",e1);
                reject(c,"observación rechaza suplantar segundo evaluador",()->sql(c,"CALL INSERTAR_DOCUMENTO_OBSERVACION(@o,"+doc+","+e2+",'OTRO','Fixture',CURRENT_DATE(),'PENDIENTE',NULL,NULL)"));
                sql(c,"CALL INSERTAR_DOCUMENTO_OBSERVACION(@o,"+doc+","+e1+",'OTRO','Fixture','2000-01-01','PENDIENTE',NULL,NULL)");
                int obs=number(c,"SELECT @o");
                check(number(c,"SELECT evaluador_id FROM documento_observacion WHERE id="+obs)==e1&&HOY.toString().equals(text(c,"SELECT fecha_observacion FROM documento_observacion WHERE id="+obs)),"observación legítima usa autor y fecha del servidor");
                reject(c,"observación no permite sustituir autor existente",()->sql(c,"CALL MODIFICAR_DOCUMENTO_OBSERVACION("+obs+","+doc+","+e2+",'OTRO','Cambio',CURRENT_DATE(),'PENDIENTE',NULL,NULL)"));
                sql(c,"CALL MODIFICAR_DOCUMENTO_OBSERVACION("+obs+","+doc+","+e1+",'ILEGIBLE','Corrección','2000-01-01','PENDIENTE',NULL,NULL)");
                check(HOY.toString().equals(text(c,"SELECT fecha_observacion FROM documento_observacion WHERE id="+obs)),"editar observación conserva fecha original");
                sql(c,"CALL MODIFICAR_DOCUMENTO_OBSERVACION("+obs+","+doc+","+e1+",'ILEGIBLE','Corrección','2000-01-01','SUBSANADA','2000-01-01','Revisión autorizada')");
                check(HOY.toString().equals(text(c,"SELECT fecha_subsanacion FROM documento_observacion WHERE id="+obs)),"subsanación autorizada toma fecha del servidor");
                reject(c,"subsanación no permite borrar su traza",()->sql(c,"CALL MODIFICAR_DOCUMENTO_OBSERVACION("+obs+","+doc+","+e1+",'ILEGIBLE','Corrección',CURRENT_DATE(),'PENDIENTE',NULL,NULL)"));
                actor(c,"evaluador",e2);
                reject(c,"segundo evaluador no observa expediente no asignado",()->sql(c,"CALL INSERTAR_DOCUMENTO_OBSERVACION(@o,"+doc+","+e2+",'OTRO','Fixture',CURRENT_DATE(),'PENDIENTE',NULL,NULL)"));
            } finally {c.rollback();ActorContext.clear();}
            String despuesRollback=snapshot(c);
            check(antesFixtures.equals(despuesRollback),"rollback conserva SHA-256 de todas las filas de las 12 tablas de negocio verificadas");
            System.out.println("SQL_SHA256_ANTES="+antesFixtures);
            System.out.println("SQL_SHA256_DESPUES="+despuesRollback);
            System.out.println("RESULTADO: "+checks+" comprobaciones aprobadas; rollback de todas las fixtures.");
        }
    }
}
