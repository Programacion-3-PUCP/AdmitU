package pe.edu.pucp.admitu.notificacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.notificacion.EstadoEnvio;
import pe.edu.pucp.admitu.notificacion.MedioNotificacion;
import pe.edu.pucp.admitu.notificacion.Notificacion;
import pe.edu.pucp.admitu.notificacion.TipoNotificacion;
import pe.edu.pucp.admitu.notificacion.dao.NotificacionDAO;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionImpl implements NotificacionDAO {

    @Override
    public int insertar(Notificacion notificacion) {
        String sql = "{call INSERTAR_NOTIFICACION(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulacion_id", notificacion.getPostulacion().getId());
                if (notificacion.getObservacionOrigen() == null) cs.setNull("_observacion_origen_id", Types.INTEGER);
                else cs.setInt("_observacion_origen_id", notificacion.getObservacionOrigen().getId());
                cs.setString("_medio_notificacion", notificacion.getMedioNotificacion() != null ? notificacion.getMedioNotificacion().name() : null);
                cs.setString("_tipo_notificacion", notificacion.getTipoNotificacion() != null ? notificacion.getTipoNotificacion().name() : null);
                cs.setString("_destinatario", notificacion.getDestinatario());
                cs.setString("_asunto", notificacion.getAsunto());
                cs.setString("_mensaje", notificacion.getMensaje());
                cs.setDate("_fecha_programada", notificacion.getFechaProgramada() != null ? Date.valueOf(notificacion.getFechaProgramada()) : null);
                cs.setDate("_fecha_envio", notificacion.getFechaEnvio() != null ? Date.valueOf(notificacion.getFechaEnvio()) : null);
                cs.setString("_estado_envio", notificacion.getEstadoEnvio() != null ? notificacion.getEstadoEnvio().name() : null);
                cs.setBoolean("_leida", notificacion.isLeida());
                cs.setDate("_fecha_lectura", notificacion.getFechaLectura() != null ? Date.valueOf(notificacion.getFechaLectura()) : null);
                cs.executeUpdate();
                notificacion.setId(cs.getInt("_id"));
                return notificacion.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR NOTIFICACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Notificacion notificacion) {
        String sql = "{call MODIFICAR_NOTIFICACION(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", notificacion.getId());
            cs.setInt("_postulacion_id", notificacion.getPostulacion().getId());
            if (notificacion.getObservacionOrigen() == null) cs.setNull("_observacion_origen_id", Types.INTEGER);
            else cs.setInt("_observacion_origen_id", notificacion.getObservacionOrigen().getId());
            cs.setString("_medio_notificacion", notificacion.getMedioNotificacion() != null ? notificacion.getMedioNotificacion().name() : null);
            cs.setString("_tipo_notificacion", notificacion.getTipoNotificacion() != null ? notificacion.getTipoNotificacion().name() : null);
            cs.setString("_destinatario", notificacion.getDestinatario());
            cs.setString("_asunto", notificacion.getAsunto());
            cs.setString("_mensaje", notificacion.getMensaje());
            cs.setDate("_fecha_programada", notificacion.getFechaProgramada() != null ? Date.valueOf(notificacion.getFechaProgramada()) : null);
            cs.setDate("_fecha_envio", notificacion.getFechaEnvio() != null ? Date.valueOf(notificacion.getFechaEnvio()) : null);
            cs.setString("_estado_envio", notificacion.getEstadoEnvio() != null ? notificacion.getEstadoEnvio().name() : null);
            cs.setBoolean("_leida", notificacion.isLeida());
            cs.setDate("_fecha_lectura", notificacion.getFechaLectura() != null ? Date.valueOf(notificacion.getFechaLectura()) : null);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR NOTIFICACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idNotificacion) {
        String sql = "{call ELIMINAR_NOTIFICACION(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idNotificacion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR NOTIFICACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Notificacion mapear(ResultSet rs) throws Exception {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(rs.getInt("postulacion_id"));
        DocumentoObservacion observacion = null;
        Object origen = rs.getObject("observacion_origen_id");
        if (origen != null) {
            observacion = new DocumentoObservacion(null, null, null, null, null, null, null, null);
            observacion.setId(rs.getInt("observacion_origen_id"));
        }
        Notificacion notificacion = new Notificacion(postulacion, observacion,
            rs.getString("medio_notificacion") != null ? MedioNotificacion.valueOf(rs.getString("medio_notificacion")) : null,
            rs.getString("tipo_notificacion") != null ? TipoNotificacion.valueOf(rs.getString("tipo_notificacion")) : null,
            rs.getString("destinatario"), rs.getString("asunto"), rs.getString("mensaje"),
            rs.getDate("fecha_programada") != null ? rs.getDate("fecha_programada").toLocalDate() : null,
            rs.getDate("fecha_envio") != null ? rs.getDate("fecha_envio").toLocalDate() : null,
            rs.getString("estado_envio") != null ? EstadoEnvio.valueOf(rs.getString("estado_envio")) : null);
        notificacion.setId(rs.getInt("id"));
        notificacion.setLeida(rs.getBoolean("leida"));
        notificacion.setFechaLectura(rs.getDate("fecha_lectura") != null ? rs.getDate("fecha_lectura").toLocalDate() : null);
        return notificacion;
    }

    @Override
    public Notificacion buscarPorId(int idNotificacion) {
        Notificacion notificacion = null;
        String sql = "{call LISTAR_NOTIFICACION_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idNotificacion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) notificacion = mapear(rs);
            }
            return notificacion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR NOTIFICACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Notificacion> listarTodos() {
        List<Notificacion> notificaciones = null;
        String sql = "{call LISTAR_NOTIFICACIONES_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (notificaciones == null) notificaciones = new ArrayList<>();
                notificaciones.add(mapear(rs));
            }
            return notificaciones;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR NOTIFICACIONES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
