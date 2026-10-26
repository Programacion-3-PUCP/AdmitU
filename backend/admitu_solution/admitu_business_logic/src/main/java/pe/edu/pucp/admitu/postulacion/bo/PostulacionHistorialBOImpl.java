package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
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
        if (h == null) throw new IllegalArgumentException("El historial de postulacion es null");
        if (h.getPostulacion() == null || h.getPostulacion().getId() <= 0)
            throw new IllegalArgumentException("El historial debe pertenecer a una postulacion valida");
        if (h.getEstadoActual() == null || h.getEstadoActual().getId() <= 0)
            throw new IllegalArgumentException("El historial necesita un estado actual valido");
        if (h.getFechaCambio() == null)
            throw new IllegalArgumentException("El historial necesita una fecha de cambio");
        Validaciones.noFutura(h.getFechaCambio(), "La fecha de cambio");
        Validaciones.texto(h.getResponsableCambio(), "Responsable del cambio", 100);
        Validaciones.texto(h.getMotivoCambio(), "Motivo del cambio", 255);
    }

    @Override
    public int insertar(PostulacionHistorial h) {
        validar(h);
        try {
            int r = daoHistorial.insertar(h);
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
    public int modificar(PostulacionHistorial h) {
        validar(h);
        if (h.getId() <= 0) throw new RuntimeException("Id de historial no valido");
        return daoHistorial.modificar(h);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de historial no valido");
        return daoHistorial.eliminar(id);
    }

    @Override
    public List<PostulacionHistorial> listarTodos() {
        return daoHistorial.listarTodos();
    }

    @Override
    public PostulacionHistorial buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de historial no valido");
        return daoHistorial.buscarPorId(id);
    }
}
