package pe.edu.pucp.admitu.postulacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.configuracion.dao.*;
import pe.edu.pucp.admitu.configuracion.impl.ConvocatoriaDAOImpl;
import pe.edu.pucp.admitu.configuracion.impl.ConvocatoriaModalidadDAOImpl;
import pe.edu.pucp.admitu.configuracion.impl.OfertaCarreraDAOImpl;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;
import pe.edu.pucp.admitu.persona.impl.PostulanteDAOImpl;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.dao.EstadoPostulacionDAO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class PostulacionDAOImpl implements PostulacionDAO {
    private final PostulanteDAO postulanteDAO = new PostulanteDAOImpl();
    private final ConvocatoriaDAO convocatoriaDAO = new ConvocatoriaDAOImpl();
    private final ConvocatoriaModalidadDAOImpl convModDAO = new ConvocatoriaModalidadDAOImpl();
    private final OfertaCarreraDAO ofertaCarreraDAO = new OfertaCarreraDAOImpl();
    private final EstadoPostulacionDAO estadoDAO = new EstadoPostulacionDAOImpl();

    @Override
    public boolean insertar(Postulacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacion_insertar(?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPostulante().getId());
            cs.setInt(2, obj.getConvocatoria().getId());
            cs.setInt(3, obj.getModalidadElegida().getId());
            cs.setInt(4, obj.getCarreraElegida().getId());
            cs.setInt(5, obj.getEstadoActual().getId());
            cs.setDate(6, obj.getFechaRegistro() != null ? Date.valueOf(obj.getFechaRegistro()) : null);
            cs.setDate(7, obj.getFechaEnvio() != null ? Date.valueOf(obj.getFechaEnvio()) : null);
            cs.setDate(8, obj.getFechaFinalizacion() != null ? Date.valueOf(obj.getFechaFinalizacion()) : null);
            cs.setString(9, obj.getCodigoInscripcion());
            cs.setString(10, obj.getObservacionGeneral());
            cs.registerOutParameter(11, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(11)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Postulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Postulacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacion_actualizar(?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setInt(2, obj.getModalidadElegida().getId());
            cs.setInt(3, obj.getCarreraElegida().getId());
            cs.setInt(4, obj.getEstadoActual().getId());
            cs.setDate(5, obj.getFechaEnvio() != null ? Date.valueOf(obj.getFechaEnvio()) : null);
            cs.setDate(6, obj.getFechaFinalizacion() != null ? Date.valueOf(obj.getFechaFinalizacion()) : null);
            cs.setString(7, obj.getCodigoInscripcion());
            cs.setString(8, obj.getObservacionGeneral());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Postulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacion_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Postulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Postulacion buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacion_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Postulacion: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Postulacion> listar() { throw new UnsupportedOperationException("Usar listarPorPostulante(id)"); }
    @Override
    public List<Postulacion> listarPorPostulante(int idPostulante) {
        List<Postulacion> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacion_listar_por_postulante(?)}")) {
            cs.setInt(1, idPostulante);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar Postulacion: " + ex.getMessage()); }
        return lista;
    }
    // Nota: se resuelve modalidadElegida via ConvocatoriaModalidad (no via Modalidad directo),
    // ya que la FK de postulacion apunta a convocatoria_modalidad.id según el modelo de dominio.
    private Postulacion mapear(ResultSet rs) throws SQLException {
        Postulante postulante = postulanteDAO.buscarPorId(rs.getInt("id_postulante"));
        Convocatoria convocatoria = convocatoriaDAO.buscarPorId(rs.getInt("id_convocatoria"));
        ConvocatoriaModalidad modalidad = convModDAO.buscarPorId(rs.getInt("id_modalidad_elegida"));
        OfertaCarrera carrera = ofertaCarreraDAO.buscarPorId(rs.getInt("id_carrera_elegida"));
        EstadoPostulacion estado = estadoDAO.buscarPorId(rs.getInt("id_estado_actual"));
        Date fr = rs.getDate("fecha_registro"), fe = rs.getDate("fecha_envio"), ff = rs.getDate("fecha_finalizacion");
        Postulacion p = new Postulacion(postulante, convocatoria, modalidad, carrera, estado,
                fr != null ? fr.toLocalDate() : null, fe != null ? fe.toLocalDate() : null,
                ff != null ? ff.toLocalDate() : null, rs.getString("codigo_inscripcion"),
                rs.getString("observacion_general"), null, null, null, null, null);
        p.setId(rs.getInt("id"));
        return p;
    }
}
