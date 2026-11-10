package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
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
        if (d == null) throw new IllegalArgumentException("El documento de postulacion es null");
        if (d.getPostulacion() == null || d.getPostulacion().getId() <= 0)
            throw new IllegalArgumentException("El documento debe pertenecer a una postulacion valida");
        if (d.getRequisitoAplicable() == null || d.getRequisitoAplicable().getId() <= 0)
            throw new IllegalArgumentException("El documento debe corresponder a un requisito valido");
        if (d.getTipoArchivo() == null)
            throw new IllegalArgumentException("El documento no tiene tipo de archivo");
        if (d.getEstadoDocumento() == null)
            throw new IllegalArgumentException("El documento no tiene estado");
        Validaciones.positivo(d.getNumeroVersion(), "El numero de version");
        Validaciones.texto(d.getNombreArchivo(), "Nombre del archivo", 255);
        if (d.getTamanioArchivo() <= 0)
            throw new IllegalArgumentException("El tamanio del archivo debe ser mayor que cero");
        Validaciones.texto(d.getRutaArchivo(), "Ruta del archivo", 500);
        Validaciones.noFutura(d.getFechaCarga(), "La fecha de carga");
        Validaciones.longitud(d.getComentarioEvaluacion(), "Comentario de evaluacion", 500);
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
