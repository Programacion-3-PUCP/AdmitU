package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoPostulacionBO;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoPostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.DocumentoPostulacionImpl;

import java.time.LocalDate;
import java.util.List;

public class DocumentoPostulacionBOImpl implements IDocumentoPostulacionBO {

    private DocumentoPostulacionDAO daoDocumento;

    public DocumentoPostulacionBOImpl() {
        daoDocumento = new DocumentoPostulacionImpl();
    }

    private void validar(DocumentoPostulacion d) {
        if (d == null) throw new RuntimeException("El documento de postulacion es null");
        if (d.getPostulacion() == null || d.getPostulacion().getId() <= 0)
            throw new RuntimeException("El documento debe pertenecer a una postulacion valida");
        if (d.getRequisitoAplicable() == null || d.getRequisitoAplicable().getId() <= 0)
            throw new RuntimeException("El documento debe corresponder a un requisito valido");
        if (d.getNumeroVersion() <= 0)
            throw new RuntimeException("El numero de version debe ser mayor que cero");
        if (d.getNombreArchivo() == null || d.getNombreArchivo().trim().isEmpty())
            throw new RuntimeException("El nombre del archivo no puede estar vacio");
        if (d.getNombreArchivo().length() > 255)
            throw new RuntimeException("El nombre del archivo no debe exceder 255 caracteres");
        if (d.getTipoArchivo() == null)
            throw new RuntimeException("El documento no tiene tipo de archivo");
        if (d.getTamanioArchivo() <= 0)
            throw new RuntimeException("El tamanio del archivo debe ser mayor que cero");
        if (d.getRutaArchivo() == null || d.getRutaArchivo().trim().isEmpty())
            throw new RuntimeException("La ruta del archivo no puede estar vacia");
        if (d.getRutaArchivo().length() > 500)
            throw new RuntimeException("La ruta del archivo no debe exceder 500 caracteres");
        if (d.getFechaCarga() != null && d.getFechaCarga().isAfter(LocalDate.now()))
            throw new RuntimeException("La fecha de carga no puede ser futura");
        if (d.getEstadoDocumento() == null)
            throw new RuntimeException("El documento no tiene estado");
        if (d.getComentarioEvaluacion() != null && d.getComentarioEvaluacion().length() > 500)
            throw new RuntimeException("El comentario no debe exceder 500 caracteres");
    }

    @Override
    public int insertar(DocumentoPostulacion d) {
        validar(d);
        try {
            int r = daoDocumento.insertar(d);
            TransactionContext.commit();
            return r;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("Error: " + ex.getMessage());
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int modificar(DocumentoPostulacion d) {
        validar(d);
        if (d.getId() <= 0) throw new RuntimeException("Id de documento no valido");
        return daoDocumento.modificar(d);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de documento no valido");
        return daoDocumento.eliminar(id);
    }

    @Override
    public List<DocumentoPostulacion> listarTodos() {
        return daoDocumento.listarTodos();
    }

    @Override
    public DocumentoPostulacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de documento no valido");
        return daoDocumento.buscarPorId(id);
    }
}
