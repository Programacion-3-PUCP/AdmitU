package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.configuracion.dao.SedeDAO;
import java.sql.*;
import java.util.*;

public class SedeDAOImpl implements SedeDAO {
    @Override
    public boolean insertar(Sede obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_sede_insertar(?,?,?,?)}")) {
            cs.setString(1, obj.getCodigo()); cs.setString(2, obj.getNombre()); cs.setString(3, obj.getDireccion());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(4)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Sede: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Sede obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_sede_actualizar(?,?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setString(2, obj.getCodigo()); cs.setString(3, obj.getNombre()); cs.setString(4, obj.getDireccion());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Sede: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_sede_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Sede: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Sede buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_sede_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Sede: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Sede> listar() {
        List<Sede> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_sede_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar Sede: " + ex.getMessage()); }
        return lista;
    }
    private Sede mapear(ResultSet rs) throws SQLException {
        Sede s = new Sede(rs.getString("codigo"), rs.getString("nombre"), rs.getString("direccion"));
        s.setId(rs.getInt("id"));
        return s;
    }
}
