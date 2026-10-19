package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionHistorialBO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionHistorialDAO;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionHistorialImpl;

import java.util.List;

public class PostulacionHistorialBOImpl implements IPostulacionHistorialBO {

    private PostulacionHistorialDAO daoHistorial;

    public PostulacionHistorialBOImpl() {
        daoHistorial = new PostulacionHistorialImpl();
    }

    private void validar(PostulacionHistorial h) {
        if (h == null) throw new RuntimeException("El historial de postulacion es null");
        if (h.getPostulacion() == null || h.getPostulacion().getId() <= 0)
            throw new RuntimeException("El historial debe pertenecer a una postulacion valida");
        if (h.getEstadoActual() == null || h.getEstadoActual().getId() <= 0)
            throw new RuntimeException("El historial necesita un estado actual valido");
        if (h.getFechaCambio() == null)
            throw new RuntimeException("El historial necesita una fecha de cambio");
        if (h.getResponsableCambio() == null || h.getResponsableCambio().trim().isEmpty())
            throw new RuntimeException("El responsable del cambio no puede estar vacio");
        if (h.getResponsableCambio().length() > 100)
            throw new RuntimeException("El responsable no debe exceder 100 caracteres");
        if (h.getMotivoCambio() == null || h.getMotivoCambio().trim().isEmpty())
            throw new RuntimeException("El motivo del cambio no puede estar vacio");
        if (h.getMotivoCambio().length() > 255)
            throw new RuntimeException("El motivo no debe exceder 255 caracteres");
        Validaciones.texto(h.getResponsableCambio(), "Responsable", 150);
        Validaciones.noFutura(h.getFechaCambio(), "Fecha de cambio");
        if (h.getEstadoAnterior() != null && h.getEstadoAnterior().getId() == h.getEstadoActual().getId())
            throw new IllegalArgumentException("El estado anterior y actual son iguales");
    }

    @Override
    public int insertar(PostulacionHistorial h) {
        validar(h);
        return TransactionContext.ejecutar(() -> daoHistorial.insertar(h));
    }

    @Override
    public int modificar(PostulacionHistorial h) {
        validar(h);
        if (h.getId() <= 0) throw new RuntimeException("Id de historial no valido");
        return TransactionContext.ejecutar(() -> daoHistorial.modificar(h));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de historial no valido");
        return TransactionContext.ejecutar(() -> daoHistorial.eliminar(id));
    }

    @Override
    public List<PostulacionHistorial> listarTodos() {
        return TransactionContext.ejecutar(() -> daoHistorial.listarTodos());
    }

    @Override
    public PostulacionHistorial buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de historial no valido");
        return TransactionContext.ejecutar(() -> daoHistorial.buscarPorId(id));
    }
}
