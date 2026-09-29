package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoObservacionBO;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoObservacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.DocumentoObservacionImpl;

import java.time.LocalDate;
import java.util.List;

public class DocumentoObservacionBOImpl implements IDocumentoObservacionBO {

    private DocumentoObservacionDAO daoObservacion;

    public DocumentoObservacionBOImpl() {
        daoObservacion = new DocumentoObservacionImpl();
    }

    private void validar(DocumentoObservacion o) {
        if (o == null) throw new RuntimeException("La observacion es null");
        if (o.getDocumento() == null || o.getDocumento().getId() <= 0)
            throw new RuntimeException("La observacion debe pertenecer a un documento valido");
        if (o.getEvaluador() == null || o.getEvaluador().getId() <= 0)
            throw new RuntimeException("La observacion debe ser emitida por un evaluador valido");
        if (o.getTipoObservacion() == null)
            throw new RuntimeException("La observacion no tiene tipo");
        if (o.getDescripcion() == null || o.getDescripcion().trim().isEmpty())
            throw new RuntimeException("La descripcion de la observacion no puede estar vacia");
        if (o.getDescripcion().length() > 500)
            throw new RuntimeException("La descripcion no debe exceder 500 caracteres");
        if (o.getFechaObservacion() == null)
            throw new RuntimeException("La observacion necesita fecha de observacion");
        if (o.getFechaObservacion().isAfter(LocalDate.now()))
            throw new RuntimeException("La fecha de observacion no puede ser futura");
        if (o.getEstadoObservacion() == null)
            throw new RuntimeException("La observacion no tiene estado");
        if (o.getFechaSubsanacion() != null && o.getFechaObservacion() != null
            && o.getFechaSubsanacion().isBefore(o.getFechaObservacion()))
            throw new RuntimeException("La fecha de subsanacion no puede ser anterior a la observacion");
        if (o.getComentarioSubsanacion() != null && o.getComentarioSubsanacion().length() > 500)
            throw new RuntimeException("El comentario de subsanacion no debe exceder 500 caracteres");
    }

    @Override
    public int insertar(DocumentoObservacion o) {
        validar(o);
        try {
            int r = daoObservacion.insertar(o);
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
    public int modificar(DocumentoObservacion o) {
        validar(o);
        if (o.getId() <= 0) throw new RuntimeException("Id de observacion no valido");
        return daoObservacion.modificar(o);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de observacion no valido");
        return daoObservacion.eliminar(id);
    }

    @Override
    public List<DocumentoObservacion> listarTodos() {
        return daoObservacion.listarTodos();
    }

    @Override
    public DocumentoObservacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de observacion no valido");
        return daoObservacion.buscarPorId(id);
    }
}
