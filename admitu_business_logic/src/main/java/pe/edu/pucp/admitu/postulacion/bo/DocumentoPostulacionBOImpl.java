package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
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
                if (d.getRequisitoAplicable().getRequisito() == null)
            throw new IllegalArgumentException("Debe definirse el requisito documental aplicable");
        Validaciones.texto(d.getNombreArchivo(), "Nombre de archivo", 150);
        Validaciones.texto(d.getRutaArchivo(), "Ruta del archivo", 255);
        Validaciones.longitud(d.getComentarioEvaluacion(), "Comentario de evaluación", 255);
        if (d.getFechaEvaluacion() != null && d.getFechaCarga() != null
                && d.getFechaEvaluacion().isBefore(d.getFechaCarga()))
            throw new IllegalArgumentException("La evaluación no puede ser anterior a la carga");
        Validaciones.noFutura(d.getFechaEvaluacion(), "Fecha de evaluación");
        if (d.getTipoArchivo().name().equals("PDF") && !d.getNombreArchivo().toLowerCase().endsWith(".pdf"))
            throw new IllegalArgumentException("El documento PDF debe tener extensión .pdf");
        if (d.getTipoArchivo().name().equals("IMAGEN")
                && !(d.getNombreArchivo().toLowerCase().endsWith(".jpg")
                  || d.getNombreArchivo().toLowerCase().endsWith(".jpeg")
                  || d.getNombreArchivo().toLowerCase().endsWith(".png")))
            throw new IllegalArgumentException("La imagen debe ser JPG o PNG");
        if (d.getRequisitoAplicable().getRequisito().getTamanioMaximoBytes() > 0
                && d.getTamanioArchivo() > d.getRequisitoAplicable().getRequisito().getTamanioMaximoBytes())
            throw new IllegalArgumentException("El archivo supera el límite del requisito");
        if (d.getRequisitoAplicable().getRequisito().getTipoArchivoRequerido() != null
                && !d.getRequisitoAplicable().getRequisito().getTipoArchivoRequerido().name().equals("CUALQUIERA")
                && d.getTipoArchivo() != d.getRequisitoAplicable().getRequisito().getTipoArchivoRequerido())
            throw new IllegalArgumentException("Formato no admitido para el requisito");
    }

    @Override
    public int insertar(DocumentoPostulacion d) {
        validar(d);
        return TransactionContext.ejecutar(() -> daoDocumento.insertar(d));
    }

    @Override
    public int modificar(DocumentoPostulacion d) {
        validar(d);
        if (d.getId() <= 0) throw new RuntimeException("Id de documento no valido");
        return TransactionContext.ejecutar(() -> daoDocumento.modificar(d));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de documento no valido");
        return TransactionContext.ejecutar(() -> daoDocumento.eliminar(id));
    }

    @Override
    public List<DocumentoPostulacion> listarTodos() {
        return TransactionContext.ejecutar(() -> daoDocumento.listarTodos());
    }

    @Override
    public DocumentoPostulacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de documento no valido");
        return TransactionContext.ejecutar(() -> daoDocumento.buscarPorId(id));
    }
}
