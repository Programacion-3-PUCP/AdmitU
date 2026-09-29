package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.EstadoObservacion;
import pe.edu.pucp.admitu.postulacion.TipoObservacion;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoObservacionDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentoObservacionImpl implements DocumentoObservacionDAO {

    @Override
    public int insertar(DocumentoObservacion observacion) {
        String sql = "{call INSERTAR_DOCUMENTO_OBSERVACION(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_documento_id", observacion.getDocumento().getId());
                cs.setInt("_evaluador_id", observacion.getEvaluador().getId());
                cs.setString("_tipo_observacion", observacion.getTipoObservacion() != null ? observacion.getTipoObservacion().name() : null);
                cs.setString("_descripcion", observacion.getDescripcion());
                cs.setDate("_fecha_observacion", observacion.getFechaObservacion() != null ? Date.valueOf(observacion.getFechaObservacion()) : null);
                cs.setString("_estado_observacion", observacion.getEstadoObservacion() != null ? observacion.getEstadoObservacion().name() : null);
                cs.setDate("_fecha_subsanacion", observacion.getFechaSubsanacion() != null ? Date.valueOf(observacion.getFechaSubsanacion()) : null);
                cs.setString("_comentario_subsanacion", observacion.getComentarioSubsanacion());
                cs.executeUpdate();
                observacion.setId(cs.getInt("_id"));
                return observacion.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR DOCUMENTO OBSERVACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(DocumentoObservacion observacion) {
        String sql = "{call MODIFICAR_DOCUMENTO_OBSERVACION(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", observacion.getId());
            cs.setInt("_documento_id", observacion.getDocumento().getId());
            cs.setInt("_evaluador_id", observacion.getEvaluador().getId());
            cs.setString("_tipo_observacion", observacion.getTipoObservacion() != null ? observacion.getTipoObservacion().name() : null);
            cs.setString("_descripcion", observacion.getDescripcion());
            cs.setDate("_fecha_observacion", observacion.getFechaObservacion() != null ? Date.valueOf(observacion.getFechaObservacion()) : null);
            cs.setString("_estado_observacion", observacion.getEstadoObservacion() != null ? observacion.getEstadoObservacion().name() : null);
            cs.setDate("_fecha_subsanacion", observacion.getFechaSubsanacion() != null ? Date.valueOf(observacion.getFechaSubsanacion()) : null);
            cs.setString("_comentario_subsanacion", observacion.getComentarioSubsanacion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR DOCUMENTO OBSERVACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idObservacion) {
        String sql = "{call ELIMINAR_DOCUMENTO_OBSERVACION(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idObservacion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR DOCUMENTO OBSERVACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private DocumentoObservacion mapear(ResultSet rs) throws Exception {
        DocumentoPostulacion documento = new DocumentoPostulacion(null, null, rs.getInt("numero_version"),
            rs.getString("nombre_archivo"), null, rs.getLong("tamanio_archivo"), rs.getString("ruta_archivo"), null,
            rs.getString("estado_documento") != null ? pe.edu.pucp.admitu.postulacion.EstadoDocumento.valueOf(rs.getString("estado_documento")) : null,
            null, null, null);
        documento.setId(rs.getInt("documento_postulacion_id"));
        Evaluador evaluador = new Evaluador(rs.getString("nombres_evaluador"), rs.getString("apellido_paterno_evaluador"),
            rs.getString("apellido_materno_evaluador"), rs.getString("correo_evaluador"),
            TipoDocumento.valueOf(rs.getString("tipo_documento_evaluador")), rs.getString("numero_documento_evaluador"),
            rs.getString("telefono_evaluador"), rs.getString("cargo_evaluador"));
        evaluador.setId(rs.getInt("evaluador_id"));
        DocumentoObservacion observacion = new DocumentoObservacion(documento, evaluador,
            rs.getString("tipo_observacion") != null ? TipoObservacion.valueOf(rs.getString("tipo_observacion")) : null,
            rs.getString("descripcion"),
            rs.getDate("fecha_observacion") != null ? rs.getDate("fecha_observacion").toLocalDate() : null,
            rs.getString("estado_observacion") != null ? EstadoObservacion.valueOf(rs.getString("estado_observacion")) : null,
            rs.getDate("fecha_subsanacion") != null ? rs.getDate("fecha_subsanacion").toLocalDate() : null,
            rs.getString("comentario_subsanacion"));
        observacion.setId(rs.getInt("id"));
        return observacion;
    }

    @Override
    public DocumentoObservacion buscarPorId(int idObservacion) {
        DocumentoObservacion observacion = null;
        String sql = "{call LISTAR_DOCUMENTO_OBSERVACION_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idObservacion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) observacion = mapear(rs);
            }
            return observacion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR DOCUMENTO OBSERVACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<DocumentoObservacion> listarTodos() {
        List<DocumentoObservacion> observaciones = null;
        String sql = "{call LISTAR_DOCUMENTOS_OBSERVACION_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (observaciones == null) observaciones = new ArrayList<>();
                observaciones.add(mapear(rs));
            }
            return observaciones;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR DOCUMENTOS OBSERVACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
