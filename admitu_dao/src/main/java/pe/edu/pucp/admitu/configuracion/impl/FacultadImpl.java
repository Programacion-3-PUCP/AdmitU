package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.dao.FacultadDAO;
import pe.edu.pucp.admitu.configuracion.Facultad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacultadImpl implements FacultadDAO {

    @Override
    public int insertar(Facultad facultad) {
        String sql = "{call INSERTAR_FACULTAD(?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo", facultad.getCodigo());
                cs.setString("_nombre", facultad.getNombre());
                cs.executeUpdate();
                facultad.setId(cs.getInt("_id"));
                return facultad.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR FACULTAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Facultad facultad) {
        String sql = "{call MODIFICAR_FACULTAD(?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", facultad.getId());
            cs.setString("_codigo", facultad.getCodigo());
            cs.setString("_nombre", facultad.getNombre());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR FACULTAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idFacultad) {
        String sql = "{call ELIMINAR_FACULTAD(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idFacultad);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR FACULTAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Facultad buscarPorId(int idFacultad) {
        Facultad facultad = null;
        String sql = "{call LISTAR_FACULTAD_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idFacultad);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    facultad = new Facultad(rs.getString("codigo"), rs.getString("nombre"));
                    facultad.setId(rs.getInt("id"));
                    facultad.setActivo(rs.getBoolean("activo"));
                }
            }
            return facultad;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR FACULTAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Facultad> listarTodos() {
        List<Facultad> facultades = null;
        String sql = "{call LISTAR_FACULTADES_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (facultades == null) facultades = new ArrayList<>();
                Facultad f = new Facultad(rs.getString("codigo"), rs.getString("nombre"));
                f.setId(rs.getInt("id"));
                f.setActivo(rs.getBoolean("activo"));
                facultades.add(f);
            }
            return facultades;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR FACULTADES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
