package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.dao.PaisDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaisImpl implements PaisDAO {

    @Override
    public int insertar(Pais pais) {
        String sql = "{call INSERTAR_PAIS(?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo_iso2", pais.getCodigoIso2());
                cs.setString("_nombre", pais.getNombre());
                cs.executeUpdate();
                pais.setId(cs.getInt("_id"));
                return pais.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR PAIS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Pais pais) {
        String sql = "{call MODIFICAR_PAIS(?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", pais.getId());
            cs.setString("_codigo_iso2", pais.getCodigoIso2());
            cs.setString("_nombre", pais.getNombre());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR PAIS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idPais) {
        String sql = "{call ELIMINAR_PAIS(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPais);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR PAIS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Pais mapear(ResultSet rs) throws Exception {
        Pais pais = new Pais(rs.getString("codigo_iso2"), rs.getString("nombre"));
        pais.setId(rs.getInt("id"));
        pais.setActivo(rs.getBoolean("activo"));
        return pais;
    }

    @Override
    public Pais buscarPorId(int idPais) {
        Pais pais = null;
        String sql = "{call LISTAR_PAIS_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPais);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) pais = mapear(rs);
            }
            return pais;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR PAIS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Pais> listarTodos() {
        List<Pais> paises = null;
        String sql = "{call LISTAR_PAISES_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (paises == null) paises = new ArrayList<>();
                paises.add(mapear(rs));
            }
            return paises;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR PAISES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
