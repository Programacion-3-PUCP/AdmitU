package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.configuracion.dao.SedeDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SedeImpl implements SedeDAO {

    @Override
    public int insertar(Sede sede) {
        String sql = "{call INSERTAR_SEDE(?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo", sede.getCodigo());
                cs.setString("_nombre", sede.getNombre());
                cs.setString("_direccion", sede.getDireccion());
                cs.executeUpdate();
                sede.setId(cs.getInt("_id"));
                return sede.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR SEDE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Sede sede) {
        String sql = "{call MODIFICAR_SEDE(?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", sede.getId());
            cs.setString("_codigo", sede.getCodigo());
            cs.setString("_nombre", sede.getNombre());
            cs.setString("_direccion", sede.getDireccion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR SEDE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idSede) {
        String sql = "{call ELIMINAR_SEDE(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idSede);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR SEDE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Sede mapear(ResultSet rs) throws Exception {
        Sede sede = new Sede(rs.getString("codigo"), rs.getString("nombre"), rs.getString("direccion"));
        sede.setId(rs.getInt("id"));
        sede.setActivo(rs.getBoolean("activo"));
        return sede;
    }

    @Override
    public Sede buscarPorId(int idSede) {
        Sede sede = null;
        String sql = "{call LISTAR_SEDE_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idSede);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) sede = mapear(rs);
            }
            return sede;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR SEDE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Sede> listarTodos() {
        List<Sede> sedes = null;
        String sql = "{call LISTAR_SEDES_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (sedes == null) sedes = new ArrayList<>();
                sedes.add(mapear(rs));
            }
            return sedes;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR SEDES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
