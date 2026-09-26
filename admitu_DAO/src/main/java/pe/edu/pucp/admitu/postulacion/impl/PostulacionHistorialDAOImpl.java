package pe.edu.pucp.admitu.postulacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionHistorialDAO;
import pe.edu.pucp.admitu.postulacion.dao.EstadoPostulacionDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class PostulacionHistorialDAOImpl implements PostulacionHistorialDAO {
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final EstadoPostulacionDAO estadoDAO = new EstadoPostulacionDAOImpl();

    @Override
    public boolean insertar(PostulacionHistorial obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacionhistorial_insertar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPostulacion().getId());
            cs.setInt(2, obj.getEstadoAnterior().getId());
            cs.setInt(3, obj.getEstadoActual().getId());
            cs.setDate(4, obj.getFechaCambio() != null ? Date.valueOf(obj.getFechaCambio()) : null);
            cs.setString(5, obj.getResponsableCambio());
            cs.setString(6, obj.getMotivoCambio());
            cs.registerOutParameter(7, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(7)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar PostulacionHistorial: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(PostulacionHistorial obj) {
        throw new UnsupportedOperationException("El historial es solo de lectura/insercion (bitacora)");
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacionhistorial_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar PostulacionHistorial: " + ex.getMessage()); }
        return false;
    }
    @Override
    public PostulacionHistorial buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacionhistorial_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar PostulacionHistorial: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<PostulacionHistorial> listar() { throw new UnsupportedOperationException("Usar listarPorPostulacion(id)"); }
    @Override
    public List<PostulacionHistorial> listarPorPostulacion(int idPostulacion) {
        List<PostulacionHistorial> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_postulacionhistorial_listar_por_postulacion(?)}")) {
            cs.setInt(1, idPostulacion);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar PostulacionHistorial: " + ex.getMessage()); }
        return lista;
    }
    private PostulacionHistorial mapear(ResultSet rs) throws SQLException {
        Postulacion postulacion = postulacionDAO.buscarPorId(rs.getInt("id_postulacion"));
        EstadoPostulacion anterior = estadoDAO.buscarPorId(rs.getInt("id_estado_anterior"));
        EstadoPostulacion actual = estadoDAO.buscarPorId(rs.getInt("id_estado_actual"));
        Date fc = rs.getDate("fecha_cambio");
        PostulacionHistorial h = new PostulacionHistorial(postulacion, anterior, actual,
                fc != null ? fc.toLocalDate() : null, rs.getString("responsable_cambio"), rs.getString("motivo_cambio"));
        h.setId(rs.getInt("id"));
        return h;
    }
}
