package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.persona.*;
import pe.edu.pucp.admitu.persona.dao.InstitucionEducativaDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstitucionEducativaDAOImpl implements InstitucionEducativaDAO {

    private final PaisDAOImpl paisDAO = new PaisDAOImpl();

    @Override
    public boolean insertar(InstitucionEducativa obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_institucion_insertar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPais().getId());
            cs.setString(2, obj.getCodigoExterno());
            cs.setString(3, obj.getNombre());
            cs.setString(4, obj.getTipoInstitucion().name());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(5));
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al insertar InstitucionEducativa: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizar(InstitucionEducativa obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_institucion_actualizar(?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setInt(2, obj.getPais().getId());
            cs.setString(3, obj.getCodigoExterno());
            cs.setString(4, obj.getNombre());
            cs.setString(5, obj.getTipoInstitucion().name());
            cs.setBoolean(6, obj.isActivo());
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al actualizar InstitucionEducativa: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_institucion_eliminar(?)}")) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al eliminar InstitucionEducativa: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public InstitucionEducativa buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_institucion_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al buscar InstitucionEducativa: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public List<InstitucionEducativa> listar() {
        List<InstitucionEducativa> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_institucion_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) {
            System.out.println("ERROR al listar InstitucionEducativa: " + ex.getMessage());
        }
        return lista;
    }

    @Override
    public List<InstitucionEducativa> listarPorPais(int idPais) {
        List<InstitucionEducativa> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_institucion_listar_por_pais(?)}")) {
            cs.setInt(1, idPais);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al listar InstitucionEducativa por pais: " + ex.getMessage());
        }
        return lista;
    }

    private InstitucionEducativa mapear(ResultSet rs) throws SQLException {
        Pais pais = paisDAO.buscarPorId(rs.getInt("id_pais"));
        InstitucionEducativa ie = new InstitucionEducativa(
                pais,
                rs.getString("codigo_externo"),
                rs.getString("nombre"),
                TipoInstitucion.valueOf(rs.getString("tipo_institucion"))
        );
        ie.setId(rs.getInt("id"));
        ie.setActivo(rs.getBoolean("activo"));
        return ie;
    }
}