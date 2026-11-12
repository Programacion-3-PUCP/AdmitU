import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.reporte.*;
import pe.edu.pucp.admitu.reporte.bo.ReporteBOImpl;

/** Integración real Jasper -> DAO/SP -> MySQL, sin datos ni claves en stdout. */
public class PruebaReportes {
 public static void main(String[] args)throws Exception{
  Path out=Path.of(args.length==0?"work/evidencias/reportes":args[0]);Files.createDirectories(out);
  var bo=new ReporteBOImpl();var f=new FiltroReporte();
  for(TipoReporte tipo:TipoReporte.values()){
   byte[] pdf=bo.generar(tipo,f);
   if(pdf.length<1000||!new String(pdf,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))throw new AssertionError("PDF inválido "+tipo);
   Files.write(out.resolve(tipo.name()+".pdf"),pdf);System.out.println("PASS PDF "+tipo+" bytes="+pdf.length);
  }
  f.setConvocatoriaId(1);f.setModalidadId(1);Files.write(out.resolve("INGRESOS-FILTRO.pdf"),bo.generar(TipoReporte.INGRESOS,f));
  f.setConvocatoriaId(999999);Files.write(out.resolve("INGRESOS-VACIO.pdf"),bo.generar(TipoReporte.INGRESOS,f));
  f.setDesde(LocalDate.of(2026,12,1));f.setHasta(LocalDate.of(2026,1,1));
  try{bo.generar(TipoReporte.INGRESOS,f);throw new AssertionError("Aceptó fechas invertidas");}catch(IllegalArgumentException ok){System.out.println("PASS fechas invertidas rechazadas");}
  f.setDesde(null);f.setHasta(null);f.setConvocatoriaId(-1);
  try{bo.generar(TipoReporte.INGRESOS,f);throw new AssertionError("Aceptó filtro negativo");}catch(IllegalArgumentException ok){System.out.println("PASS filtro negativo rechazado");}
  try(Connection con=DBManager.getInstance().getConnection();Statement st=con.createStatement()){
   String[] names={"INGRESOS","INGRESOS_FILTRO","OBSERVADOS","FINALIZADA"};
   String[] queries={"SELECT COUNT(*),COALESCE(SUM(monto),0) FROM pago WHERE estado_pago='APROBADO'","SELECT COUNT(*),COALESCE(SUM(pg.monto),0) FROM pago pg JOIN postulacion p ON p.id=pg.postulacion_id JOIN convocatoria_modalidad cm ON cm.id=p.modalidad_elegida_id WHERE pg.estado_pago='APROBADO' AND p.convocatoria_id=1 AND cm.modalidad_id=1","SELECT COUNT(*),0 FROM documento_observacion WHERE estado_observacion='PENDIENTE'","SELECT COUNT(*),0 FROM postulacion p JOIN estado_postulacion e ON e.id=p.estado_actual_id WHERE e.codigo='FINALIZADA'"};
   for(int i=0;i<queries.length;i++){try(ResultSet rs=st.executeQuery(queries[i])){rs.next();System.out.println("SQL "+names[i]+" cantidad="+rs.getLong(1)+" monto="+rs.getBigDecimal(2));}}
  }
 }
}
