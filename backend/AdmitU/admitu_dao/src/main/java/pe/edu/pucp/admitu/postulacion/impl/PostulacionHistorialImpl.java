package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
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
        throw new UnsupportedOperationException("El historial es inmutable; se genera únicamente por transiciones del servidor");
    }

    @Override
    public int modificar(PostulacionHistorial historial) {
        throw new UnsupportedOperationException("El historial es inmutable; se genera únicamente por transiciones del servidor");
    }

    @Override
    public int eliminar(int idHistorial) {
        throw new UnsupportedOperationException("El historial es inmutable; se genera únicamente por transiciones del servidor");
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
            System.err.println("ERROR AL BUSCAR POSTULACION HISTORIAL: " + pe.edu.pucp.admitu.config.Diagnostico.resumen(ex));
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<PostulacionHistorial> listarTodos() {
        List<PostulacionHistorial> historial = new ArrayList<>();
        String sql = "{call LISTAR_POSTULACIONES_HISTORIAL_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                historial.add(mapear(rs));
            }
            return historial;
        } catch (Exception ex) {
            System.err.println("ERROR AL LISTAR POSTULACIONES HISTORIAL: " + pe.edu.pucp.admitu.config.Diagnostico.resumen(ex));
            throw new RuntimeException(ex);
        }
    }
}
