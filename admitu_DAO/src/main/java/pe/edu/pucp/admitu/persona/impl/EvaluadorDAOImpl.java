package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.persona.*;
import pe.edu.pucp.admitu.persona.dao.EvaluadorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluadorDAOImpl implements EvaluadorDAO {

    @Override
    public boolean insertar(Evaluador obj) {
        String sql = "{call sp_evaluador_insertar(?,?,?,?,?,?,?,?,?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, obj.getNombres());
            cs.setString(2, obj.getApellidoPaterno());
            cs.setString(3, obj.getApellidoMaterno());
            cs.setString(4, obj.getCorreo());
            cs.setString(5, obj.getTipoDocumento().name());
            cs.setString(6, obj.getNumeroDocumento());
            cs.setString(7, obj.getTelefono());
            cs.setString(8, obj.getCargo());
            cs.setBoolean(9, obj.isActivo());
            cs.registerOutParameter(10, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(10));
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al insertar Evaluador: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizar(Evaluador obj) {
        String sql = "{call sp_evaluador_actualizar(?,?,?,?,?,?,?,?,?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, obj.getId());
            cs.setString(2, obj.getNombres());
            cs.setString(3, obj.getApellidoPaterno());
            cs.setString(4, obj.getApellidoMaterno());
            cs.setString(5, obj.getCorreo());
            cs.setString(6, obj.getTipoDocumento().name());
            cs.setString(7, obj.getNumeroDocumento());
            cs.setString(8, obj.getTelefono());
            cs.setString(9, obj.getCargo());
            cs.setBoolean(10, obj.isActivo());
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al actualizar Evaluador: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "{call sp_evaluador_eliminar(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al eliminar Evaluador: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public Evaluador buscarPorId(int id) {
        String sql = "{call sp_evaluador_buscar_por_id(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al buscar Evaluador: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public List<Evaluador> listar() {
        List<Evaluador> lista = new ArrayList<>();
        String sql = "{call sp_evaluador_listar()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) {
            System.out.println("ERROR al listar Evaluador: " + ex.getMessage());
        }
        return lista;
    }

    private Evaluador mapear(ResultSet rs) throws SQLException {
        Evaluador e = new Evaluador(
                rs.getString("nombres"), rs.getString("apellido_paterno"), rs.getString("apellido_materno"),
                rs.getString("correo"), TipoDocumento.valueOf(rs.getString("tipo_documento")),
                rs.getString("numero_documento"), rs.getString("telefono"), rs.getString("cargo")
        );
        e.setId(rs.getInt("id"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }
}
