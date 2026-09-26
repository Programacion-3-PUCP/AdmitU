package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Facultad;
import pe.edu.pucp.admitu.configuracion.dao.FacultadDAO;
import java.sql.*;
import java.util.*;

public class FacultadDAOImpl implements FacultadDAO {
    @Override
    public boolean insertar(Facultad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_facultad_insertar(?,?,?)}")) {
            cs.setString(1, obj.getCodigo());
            cs.setString(2, obj.getNombre());
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(3));
            return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Facultad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Facultad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_facultad_actualizar(?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setString(2, obj.getCodigo()); cs.setString(3, obj.getNombre());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Facultad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_facultad_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Facultad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Facultad buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_facultad_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Facultad: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Facultad> listar() {
        List<Facultad> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_facultad_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar Facultad: " + ex.getMessage()); }
        return lista;
    }
    private Facultad mapear(ResultSet rs) throws SQLException {
        Facultad f = new Facultad(rs.getString("codigo"), rs.getString("nombre"));
        f.setId(rs.getInt("id"));
        return f;
    }
}
