package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionHistorialDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulacionHistorialImpl implements PostulacionHistorialDAO {

    @Override
    public int insertar(PostulacionHistorial historial) {
        String sql = "{call INSERTAR_POSTULACION_HISTORIAL(?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulacion_id", historial.getPostulacion().getId());
                if (historial.getEstadoAnterior() == null) cs.setNull("_estado_anterior_id", Types.INTEGER);
                else cs.setInt("_estado_anterior_id", historial.getEstadoAnterior().getId());
                cs.setInt("_estado_actual_id", historial.getEstadoActual().getId());
                cs.setDate("_fecha_cambio", historial.getFechaCambio() != null ? Date.valueOf(historial.getFechaCambio()) : null);
                cs.setString("_responsable_cambio", historial.getResponsableCambio());
                cs.setString("_motivo_cambio", historial.getMotivoCambio());
                cs.executeUpdate();
                historial.setId(cs.getInt("_id"));
                return historial.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR POSTULACION HISTORIAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(PostulacionHistorial historial) {
        String sql = "{call MODIFICAR_POSTULACION_HISTORIAL(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", historial.getId());
            cs.setInt("_postulacion_id", historial.getPostulacion().getId());
            if (historial.getEstadoAnterior() == null) cs.setNull("_estado_anterior_id", Types.INTEGER);
            else cs.setInt("_estado_anterior_id", historial.getEstadoAnterior().getId());
            cs.setInt("_estado_actual_id", historial.getEstadoActual().getId());
            cs.setDate("_fecha_cambio", historial.getFechaCambio() != null ? Date.valueOf(historial.getFechaCambio()) : null);
            cs.setString("_responsable_cambio", historial.getResponsableCambio());
            cs.setString("_motivo_cambio", historial.getMotivoCambio());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR POSTULACION HISTORIAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idHistorial) {
        String sql = "{call ELIMINAR_POSTULACION_HISTORIAL(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idHistorial);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR POSTULACION HISTORIAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private PostulacionHistorial mapear(ResultSet rs) throws Exception {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(rs.getInt("postulacion_id"));
        EstadoPostulacion estadoAnterior = null;
        Object anterior = rs.getObject("estado_anterior_id");
        if (anterior != null) {
            estadoAnterior = new EstadoPostulacion(rs.getString("codigo_estado_anterior"), rs.getString("nombre_estado_anterior"), null);
            estadoAnterior.setId(rs.getInt("estado_anterior_id"));
        }
        EstadoPostulacion estadoActual = new EstadoPostulacion(rs.getString("codigo_estado_actual"), rs.getString("nombre_estado_actual"), null);
        estadoActual.setId(rs.getInt("estado_actual_id"));
        PostulacionHistorial historial = new PostulacionHistorial(postulacion, estadoAnterior, estadoActual,
            rs.getDate("fecha_cambio") != null ? rs.getDate("fecha_cambio").toLocalDate() : null,
            rs.getString("responsable_cambio"), rs.getString("motivo_cambio"));
        historial.setId(rs.getInt("id"));
        return historial;
    }

    @Override
    public PostulacionHistorial buscarPorId(int idHistorial) {
        PostulacionHistorial historial = null;
        String sql = "{call LISTAR_POSTULACION_HISTORIAL_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idHistorial);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) historial = mapear(rs);
            }
            return historial;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR POSTULACION HISTORIAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<PostulacionHistorial> listarTodos() {
        List<PostulacionHistorial> historial = null;
        String sql = "{call LISTAR_POSTULACIONES_HISTORIAL_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (historial == null) historial = new ArrayList<>();
                historial.add(mapear(rs));
            }
            return historial;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR POSTULACIONES HISTORIAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
