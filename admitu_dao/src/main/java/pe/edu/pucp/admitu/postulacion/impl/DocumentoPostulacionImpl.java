package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.TipoArchivo;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.EstadoDocumento;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoPostulacionDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentoPostulacionImpl implements DocumentoPostulacionDAO {

    @Override
    public int insertar(DocumentoPostulacion documento) {
        String sql = "{call INSERTAR_DOCUMENTO_POSTULACION(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulacion_id", documento.getPostulacion().getId());
                cs.setInt("_requisito_id", documento.getRequisitoAplicable().getId());
                cs.setInt("_numero_version", documento.getNumeroVersion());
                cs.setString("_nombre_archivo", documento.getNombreArchivo());
                cs.setString("_tipo_archivo", documento.getTipoArchivo() != null ? documento.getTipoArchivo().name() : null);
                cs.setLong("_tamanio_archivo", documento.getTamanioArchivo());
                cs.setString("_ruta_archivo", documento.getRutaArchivo());
                cs.setDate("_fecha_carga", documento.getFechaCarga() != null ? Date.valueOf(documento.getFechaCarga()) : null);
                cs.setString("_estado_documento", documento.getEstadoDocumento() != null ? documento.getEstadoDocumento().name() : null);
                cs.setDate("_fecha_evaluacion", documento.getFechaEvaluacion() != null ? Date.valueOf(documento.getFechaEvaluacion()) : null);
                cs.setString("_comentario_evaluacion", documento.getComentarioEvaluacion());
                cs.executeUpdate();
                documento.setId(cs.getInt("_id"));
                return documento.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR DOCUMENTO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(DocumentoPostulacion documento) {
        String sql = "{call MODIFICAR_DOCUMENTO_POSTULACION(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", documento.getId());
            cs.setInt("_postulacion_id", documento.getPostulacion().getId());
            cs.setInt("_requisito_id", documento.getRequisitoAplicable().getId());
            cs.setInt("_numero_version", documento.getNumeroVersion());
            cs.setString("_nombre_archivo", documento.getNombreArchivo());
            cs.setString("_tipo_archivo", documento.getTipoArchivo() != null ? documento.getTipoArchivo().name() : null);
            cs.setLong("_tamanio_archivo", documento.getTamanioArchivo());
            cs.setString("_ruta_archivo", documento.getRutaArchivo());
            cs.setDate("_fecha_carga", documento.getFechaCarga() != null ? Date.valueOf(documento.getFechaCarga()) : null);
            cs.setString("_estado_documento", documento.getEstadoDocumento() != null ? documento.getEstadoDocumento().name() : null);
            cs.setDate("_fecha_evaluacion", documento.getFechaEvaluacion() != null ? Date.valueOf(documento.getFechaEvaluacion()) : null);
            cs.setString("_comentario_evaluacion", documento.getComentarioEvaluacion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR DOCUMENTO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idDocumento) {
        String sql = "{call ELIMINAR_DOCUMENTO_POSTULACION(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idDocumento);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR DOCUMENTO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private DocumentoPostulacion mapear(ResultSet rs) throws Exception {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(rs.getInt("postulacion_id"));
        Requisito requisito = new Requisito(rs.getString("codigo_requisito"), rs.getString("nombre_requisito"),
            rs.getString("descripcion_requisito"), rs.getString("tipo_archivo_requisito") != null ? TipoArchivo.valueOf(rs.getString("tipo_archivo_requisito")) : null,
            rs.getInt("tamanio_maximo_requisito"));
        requisito.setId(rs.getInt("requisito_id"));
        DocumentoPostulacion documento = new DocumentoPostulacion(postulacion, requisito, rs.getInt("numero_version"),
            rs.getString("nombre_archivo"), rs.getString("tipo_archivo") != null ? TipoArchivo.valueOf(rs.getString("tipo_archivo")) : null,
            rs.getLong("tamanio_archivo"), rs.getString("ruta_archivo"),
            rs.getDate("fecha_carga") != null ? rs.getDate("fecha_carga").toLocalDate() : null,
            rs.getString("estado_documento") != null ? EstadoDocumento.valueOf(rs.getString("estado_documento")) : null,
            rs.getDate("fecha_evaluacion") != null ? rs.getDate("fecha_evaluacion").toLocalDate() : null,
            rs.getString("comentario_evaluacion"), null);
        documento.setId(rs.getInt("id"));
        return documento;
    }

    @Override
    public DocumentoPostulacion buscarPorId(int idDocumento) {
        DocumentoPostulacion documento = null;
        String sql = "{call LISTAR_DOCUMENTO_POSTULACION_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idDocumento);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) documento = mapear(rs);
            }
            return documento;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR DOCUMENTO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<DocumentoPostulacion> listarTodos() {
        List<DocumentoPostulacion> documentos = null;
        String sql = "{call LISTAR_DOCUMENTOS_POSTULACION_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (documentos == null) documentos = new ArrayList<>();
                documentos.add(mapear(rs));
            }
            return documentos;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR DOCUMENTOS POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
