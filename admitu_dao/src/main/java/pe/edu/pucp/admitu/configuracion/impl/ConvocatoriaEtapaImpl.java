package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaEtapa;
import pe.edu.pucp.admitu.configuracion.Etapa;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaEtapaDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConvocatoriaEtapaImpl implements ConvocatoriaEtapaDAO {

    @Override
    public int insertar(ConvocatoriaEtapa relacion) {
        String sql = "{call INSERTAR_CONVOCATORIA_ETAPA(?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_convocatoria_id", relacion.getConvocatoria().getId());
                cs.setInt("_etapa_id", relacion.getEtapa().getId());
                cs.setDate("_fecha_inicio", relacion.getFechaInicio() != null ? Date.valueOf(relacion.getFechaInicio()) : null);
                cs.setDate("_fecha_fin", relacion.getFechaFin() != null ? Date.valueOf(relacion.getFechaFin()) : null);
                cs.executeUpdate();
                relacion.setId(cs.getInt("_id"));
                return relacion.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR CONVOCATORIA ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(ConvocatoriaEtapa relacion) {
        String sql = "{call MODIFICAR_CONVOCATORIA_ETAPA(?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", relacion.getId());
            cs.setInt("_convocatoria_id", relacion.getConvocatoria().getId());
            cs.setInt("_etapa_id", relacion.getEtapa().getId());
            cs.setDate("_fecha_inicio", relacion.getFechaInicio() != null ? Date.valueOf(relacion.getFechaInicio()) : null);
            cs.setDate("_fecha_fin", relacion.getFechaFin() != null ? Date.valueOf(relacion.getFechaFin()) : null);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR CONVOCATORIA ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idRelacion) {
        String sql = "{call ELIMINAR_CONVOCATORIA_ETAPA(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRelacion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR CONVOCATORIA ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private ConvocatoriaEtapa mapear(ResultSet rs) throws Exception {
        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setId(rs.getInt("convocatoria_id"));
        Etapa etapa = new Etapa(
            rs.getDate("fecha_etapa_inicio") != null ? rs.getDate("fecha_etapa_inicio").toLocalDate() : null,
            rs.getDate("fecha_etapa_fin") != null ? rs.getDate("fecha_etapa_fin").toLocalDate() : null,
            rs.getString("codigo_etapa"), rs.getString("nombre_etapa"), rs.getString("descripcion_etapa"));
        etapa.setId(rs.getInt("etapa_id"));
        ConvocatoriaEtapa relacion = new ConvocatoriaEtapa(convocatoria, etapa,
            rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toLocalDate() : null,
            rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toLocalDate() : null);
        relacion.setId(rs.getInt("id"));
        return relacion;
    }

    @Override
    public ConvocatoriaEtapa buscarPorId(int idRelacion) {
        ConvocatoriaEtapa relacion = null;
        String sql = "{call LISTAR_CONVOCATORIA_ETAPA_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRelacion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) relacion = mapear(rs);
            }
            return relacion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR CONVOCATORIA ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<ConvocatoriaEtapa> listarTodos() {
        List<ConvocatoriaEtapa> relaciones = null;
        String sql = "{call LISTAR_CONVOCATORIAS_ETAPA_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (relaciones == null) relaciones = new ArrayList<>();
                relaciones.add(mapear(rs));
            }
            return relaciones;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR CONVOCATORIAS ETAPA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
