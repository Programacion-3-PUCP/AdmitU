package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Etapa;
import pe.edu.pucp.admitu.configuracion.dao.EtapaDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtapaImpl implements EtapaDAO {

    @Override
    public int insertar(Etapa etapa) {
        String sql = "{call INSERTAR_ETAPA(?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setDate("_fecha_inicio", etapa.getFechaInicio() != null ? Date.valueOf(etapa.getFechaInicio()) : null);
                cs.setDate("_fecha_fin", etapa.getFechaFin() != null ? Date.valueOf(etapa.getFechaFin()) : null);
                cs.setString("_codigo_etapa", etapa.getCodigoEtapa());
                cs.setString("_nombre", etapa.getNombre());
                cs.setString("_descripcion", etapa.getDescripcion());
                cs.executeUpdate();
                etapa.setId(cs.getInt("_id"));
                return etapa.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Etapa etapa) {
        String sql = "{call MODIFICAR_ETAPA(?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", etapa.getId());
            cs.setDate("_fecha_inicio", etapa.getFechaInicio() != null ? Date.valueOf(etapa.getFechaInicio()) : null);
            cs.setDate("_fecha_fin", etapa.getFechaFin() != null ? Date.valueOf(etapa.getFechaFin()) : null);
            cs.setString("_codigo_etapa", etapa.getCodigoEtapa());
            cs.setString("_nombre", etapa.getNombre());
            cs.setString("_descripcion", etapa.getDescripcion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idEtapa) {
        String sql = "{call ELIMINAR_ETAPA(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idEtapa);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Etapa mapear(ResultSet rs) throws Exception {
        Etapa etapa = new Etapa(
            rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null,
            rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null,
            rs.getString("codigo_etapa"), rs.getString("nombre"), rs.getString("descripcion"));
        etapa.setId(rs.getInt("id"));
        etapa.setActivo(rs.getBoolean("activo"));
        return etapa;
    }

    @Override
    public Etapa buscarPorId(int idEtapa) {
        Etapa etapa = null;
        String sql = "{call LISTAR_ETAPA_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idEtapa);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) etapa = mapear(rs);
            }
            return etapa;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Etapa> listarTodos() {
        List<Etapa> etapas = null;
        String sql = "{call LISTAR_ETAPAS_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (etapas == null) etapas = new ArrayList<>();
                etapas.add(mapear(rs));
            }
            return etapas;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR ETAPAS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
