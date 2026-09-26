package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.dao.PaisDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaisDAOImpl implements PaisDAO {

    @Override
    public boolean insertar(Pais obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pais_insertar(?,?,?)}")) {
            cs.setString(1, obj.getCodigoIso2());
            cs.setString(2, obj.getNombre());
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(3));
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al insertar Pais: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizar(Pais obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pais_actualizar(?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setString(2, obj.getCodigoIso2());
            cs.setString(3, obj.getNombre());
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al actualizar Pais: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pais_eliminar(?)}")) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al eliminar Pais: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public Pais buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pais_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al buscar Pais: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public Pais buscarPorCodigoIso2(String codigoIso2) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pais_buscar_por_codigo(?)}")) {
            cs.setString(1, codigoIso2);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al buscar Pais por codigo: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public List<Pais> listar() {
        List<Pais> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pais_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) {
            System.out.println("ERROR al listar Pais: " + ex.getMessage());
        }
        return lista;
    }

    private Pais mapear(ResultSet rs) throws SQLException {
        Pais p = new Pais(rs.getString("codigo_iso2"), rs.getString("nombre"));
        p.setId(rs.getInt("id"));
        return p;
    }
}