package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.dao.ModalidadDAO;
import java.sql.*;
import java.util.*;

public class ModalidadDAOImpl implements ModalidadDAO {
    @Override
    public boolean insertar(Modalidad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_modalidad_insertar(?,?,?,?,?,?)}")) {
            cs.setString(1, obj.getCodigoModalidad()); cs.setString(2, obj.getNombre()); cs.setString(3, obj.getDescripcion());
            cs.setBoolean(4, obj.isRequiereColegio()); cs.setBoolean(5, obj.isRequiereUniversidad());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(6)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Modalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Modalidad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_modalidad_actualizar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setString(2, obj.getCodigoModalidad()); cs.setString(3, obj.getNombre());
            cs.setString(4, obj.getDescripcion()); cs.setBoolean(5, obj.isRequiereColegio());
            cs.setBoolean(6, obj.isRequiereUniversidad()); cs.setBoolean(7, obj.isActivo());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Modalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_modalidad_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Modalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Modalidad buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_modalidad_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Modalidad: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Modalidad> listar() {
        List<Modalidad> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_modalidad_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar Modalidad: " + ex.getMessage()); }
        return lista;
    }
    private Modalidad mapear(ResultSet rs) throws SQLException {
        Modalidad m = new Modalidad(rs.getString("codigo_modalidad"), rs.getString("nombre"), rs.getString("descripcion"),
                rs.getBoolean("requiere_colegio"), rs.getBoolean("requiere_universidad"));
        m.setId(rs.getInt("id"));
        m.setActivo(rs.getBoolean("activo"));
        return m;
    }
}