package pe.edu.pucp.admitu.postulacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.dao.EstadoPostulacionDAO;
import java.sql.*;
import java.util.*;

public class EstadoPostulacionDAOImpl implements EstadoPostulacionDAO {
    @Override
    public boolean insertar(EstadoPostulacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_estadopostulacion_insertar(?,?,?,?)}")) {
            cs.setString(1, obj.getCodigo()); cs.setString(2, obj.getNombre()); cs.setString(3, obj.getDescripcion());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(4)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar EstadoPostulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(EstadoPostulacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_estadopostulacion_actualizar(?,?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setString(2, obj.getCodigo()); cs.setString(3, obj.getNombre()); cs.setString(4, obj.getDescripcion());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar EstadoPostulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_estadopostulacion_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar EstadoPostulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public EstadoPostulacion buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_estadopostulacion_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar EstadoPostulacion: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<EstadoPostulacion> listar() {
        List<EstadoPostulacion> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_estadopostulacion_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar EstadoPostulacion: " + ex.getMessage()); }
        return lista;
    }
    private EstadoPostulacion mapear(ResultSet rs) throws SQLException {
        EstadoPostulacion e = new EstadoPostulacion(rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"));
        e.setId(rs.getInt("id"));
        return e;
    }
}
