package pe.edu.pucp.admitu.postulacion.bo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.bo.ArchivoBOImpl;
import pe.edu.pucp.admitu.archivo.boi.IArchivoBO;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.TipoArchivo;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.EnvioPostulacion;
import pe.edu.pucp.admitu.postulacion.EstadoDocumento;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoPostulacionDAO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.DocumentoPostulacionImpl;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionImpl;

public class PostulacionBOImpl implements IPostulacionBO {

    private PostulacionDAO daoPostulacion;
    private DocumentoPostulacionDAO daoDocumento;
    private IArchivoBO boArchivo;

    public PostulacionBOImpl() {
        daoPostulacion = new PostulacionImpl();
        daoDocumento = new DocumentoPostulacionImpl();
        boArchivo = new ArchivoBOImpl();
    }

    private void validar(Postulacion p) {
        if (p == null) {
            throw new IllegalArgumentException("La postulacion es null");
        }
        if (p.getPostulante() == null || p.getPostulante().getId() <= 0) {
            throw new IllegalArgumentException("La postulacion no tiene postulante valido");
        }
        if (p.getConvocatoria() == null || p.getConvocatoria().getId() <= 0) {
            throw new IllegalArgumentException("La postulacion no tiene convocatoria valida");
        }
        if (p.getModalidadElegida() == null || p.getModalidadElegida().getId() <= 0) {
            throw new IllegalArgumentException("La postulacion no tiene modalidad valida");
        }
        if (p.getCarreraElegida() == null || p.getCarreraElegida().getId() <= 0) {
            throw new IllegalArgumentException("La postulacion no tiene oferta de carrera valida");
        }
        if (p.getEstadoActual() == null || p.getEstadoActual().getId() <= 0) {
            throw new IllegalArgumentException("La postulacion no tiene estado valido");
        }
        Validaciones.codigo(p.getCodigoInscripcion(), "Codigo de inscripcion", 20);
    }

    @Override
    public int insertar(Postulacion p) {
        validar(p);
        try {
            int resultado = daoPostulacion.insertar(p);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("No fue posible registrar la postulación: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int modificar(Postulacion p) {
        if (p == null) {
            throw new IllegalArgumentException("La postulacion es null");
        }
        Validaciones.positivo(p.getId(), "Id de postulacion");
        validar(p);
        try {
            int resultado = daoPostulacion.modificar(p);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("No se pudo modificar la postulación: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) {
            throw new RuntimeException("Id de postulacion no valido");
        }
        return daoPostulacion.eliminar(id);
    }

    @Override
    public List<Postulacion> listarTodos() {
        return daoPostulacion.listarTodos();
    }

    @Override
    public Postulacion buscarPorId(int id) {
        if (id <= 0) {
            throw new RuntimeException("Id de postulacion no valido");
        }
        return daoPostulacion.buscarPorId(id);
    }

    @Override
    public int enviar(EnvioPostulacion envio) {
        if (envio == null || envio.getPostulacion() == null) {
            throw new IllegalArgumentException("Adjunta la postulación");
        }
        Postulacion p = envio.getPostulacion();
        EstadoPostulacion estado = new EstadoPostulacion();
        estado.setId(2);
        p.setEstadoActual(estado);
        p.setCodigoInscripcion("ADM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 15).toUpperCase());
        validar(p);

        List<ArchivoDigital> documentos = envio.getDocumentos() == null ? new ArrayList<>() : envio.getDocumentos();
        if (documentos.size() > 20) {
            throw new IllegalArgumentException("Máximo 20 archivos por postulación");
        }

        long total = 0;
        for (ArchivoDigital a : documentos) {
            if (a == null || a.getContenidoBase64() == null) {
                throw new IllegalArgumentException("Adjunta los bytes de cada requisito");
            }
            total += a.getContenidoBase64().length();
        }
        if (total > 36L * 1024 * 1024) {
            throw new IllegalArgumentException("La suma de archivos supera el límite de envío");
        }

        try {
            int id = daoPostulacion.insertar(p);
            for (ArchivoDigital a : documentos) {
                DocumentoPostulacion d = new DocumentoPostulacion();
                d.setPostulacion(p);
                RequisitoConvocatoriaModalidad req = new RequisitoConvocatoriaModalidad();
                req.setId(a.getRequisitoAplicableId());
                d.setRequisitoAplicable(req);
                d.setNombreArchivo(a.getNombre());
                d.setNumeroVersion(1);
                d.setTipoArchivo("application/pdf".equals(a.getMime()) ? TipoArchivo.PDF : TipoArchivo.IMAGEN);
                d.setTamanioArchivo(Base64.getDecoder().decode(a.getContenidoBase64()).length);
                d.setRutaArchivo("archivo:pendiente");
                d.setFechaCarga(LocalDate.now(ZoneId.of("America/Lima")));
                d.setEstadoDocumento(EstadoDocumento.PENDIENTE);

                int docId = daoDocumento.insertar(d);
                a.setPostulanteId(p.getPostulante().getId());
                a.setTipo("DOCUMENTO");
                a.setReferenciaId(docId);
                boArchivo.guardarEnTransaccion(a);
            }
            int sede = envio.getSedeId() > 0 ? envio.getSedeId() : (p.getSedeId() == null ? 0 : p.getSedeId());
            daoPostulacion.validarEnvio(id, sede);
            TransactionContext.commit();
            return id;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) ex;
            }
            throw new RuntimeException("No fue posible enviar la postulación: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int asignarEvaluador(int id, int evaluadorId) {
        if (id <= 0 || evaluadorId <= 0) {
            throw new IllegalArgumentException("Postulación y evaluador requeridos");
        }
        try {
            int resultado = daoPostulacion.asignarEvaluador(id, evaluadorId);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("No fue posible asignar evaluador: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }
}
