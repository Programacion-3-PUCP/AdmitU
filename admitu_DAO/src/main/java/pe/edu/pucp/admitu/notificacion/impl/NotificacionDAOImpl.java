package pe.edu.pucp.admitu.notificacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.notificacion.*;
import pe.edu.pucp.admitu.notificacion.dao.NotificacionDAO;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoObservacionDAO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.DocumentoObservacionDAOImpl;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionDAOImpl;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class NotificacionDAOImpl implements NotificacionDAO {
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final DocumentoObservacionDAO observacionDAO = new DocumentoObservacionDAOImpl();

    @Override
    public boolean insertar(Notificacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_notificacion_insertar(?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPostulacion().getId());
            if (obj.getObservacionOrigen() != null) cs.setInt(2, obj.getObservacionOrigen().getId());
            else cs.setNull(2, Types.INTEGER);
            cs.setString(3, obj.getMedioNotificacion().name());
            cs.setString(4, obj.getTipoNotificacion().name());
            cs.setString(5, obj.getDestinatario());
            cs.setString(6, obj.getAsunto());
            cs.setString(7, obj.getMensaje());
            cs.setDate(8, obj.getFechaProgramada() != null ? Date.valueOf(obj.getFechaProgramada()) : null);
            cs.setDate(9, obj.getFechaEnvio() != null ? Date.valueOf(obj.getFechaEnvio()) : null);
            cs.setString(10, obj.getEstadoEnvio().name());
            cs.registerOutParameter(11, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(11)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Notificacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Notificacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_notificacion_actualizar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setDate(2, obj.getFechaEnvio() != null ? Date.valueOf(obj.getFechaEnvio()) : null);
            cs.setString(3, obj.getEstadoEnvio().name());
            cs.setBoolean(4, obj.isLeida());
            cs.setDate(5, obj.getFechaLectura() != null ? Date.valueOf(obj.getFechaLectura()) : null);
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Notificacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_notificacion_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Notificacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Notificacion buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_notificacion_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Notificacion: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Notificacion> listar() { throw new UnsupportedOperationException("Usar listarPorPostulacion(id)"); }
    @Override
    public List<Notificacion> listarPorPostulacion(int idPostulacion) {
        List<Notificacion> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_notificacion_listar_por_postulacion(?)}")) {
            cs.setInt(1, idPostulacion);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar Notificacion: " + ex.getMessage()); }
        return lista;
    }
    private Notificacion mapear(ResultSet rs) throws SQLException {
        Postulacion postulacion = postulacionDAO.buscarPorId(rs.getInt("id_postulacion"));
        Integer idObs = (Integer) rs.getObject("id_observacion_origen");
        DocumentoObservacion origen = idObs != null ? observacionDAO.buscarPorId(idObs) : null;
        Date fp = rs.getDate("fecha_programada"), fe = rs.getDate("fecha_envio"), fl = rs.getDate("fecha_lectura");
        Notificacion n = new Notificacion(postulacion, origen, MedioNotificacion.valueOf(rs.getString("medio_notificacion")),
                TipoNotificacion.valueOf(rs.getString("tipo_notificacion")), rs.getString("destinatario"),
                rs.getString("asunto"), rs.getString("mensaje"), fp != null ? fp.toLocalDate() : null,
                fe != null ? fe.toLocalDate() : null, EstadoEnvio.valueOf(rs.getString("estado_envio")));
        n.setId(rs.getInt("id"));
        n.setLeida(rs.getBoolean("leida"));
        n.setFechaLectura(fl != null ? fl.toLocalDate() : null);
        return n;
    }
}