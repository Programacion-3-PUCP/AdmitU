package pe.edu.pucp.admitu.notificacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.notificacion.Notificacion;
import pe.edu.pucp.admitu.notificacion.boi.INotificacionBO;
import pe.edu.pucp.admitu.notificacion.dao.NotificacionDAO;
import pe.edu.pucp.admitu.notificacion.impl.NotificacionImpl;

import java.time.LocalDate;
import java.util.List;

public class NotificacionBOImpl implements INotificacionBO {

    private NotificacionDAO daoNotificacion;

    public NotificacionBOImpl() {
        daoNotificacion = new NotificacionImpl();
    }

    private void validar(Notificacion n) {
        if (n == null) throw new RuntimeException("La notificacion es null");
        if (n.getPostulacion() == null || n.getPostulacion().getId() <= 0)
            throw new RuntimeException("La notificacion debe pertenecer a una postulacion valida");
        if (n.getMedioNotificacion() == null)
            throw new RuntimeException("La notificacion no tiene medio de envio");
        if (n.getTipoNotificacion() == null)
            throw new RuntimeException("La notificacion no tiene tipo");
        if (n.getDestinatario() == null || n.getDestinatario().trim().isEmpty())
            throw new RuntimeException("El destinatario no puede estar vacio");
        if (n.getDestinatario().length() > 150)
            throw new RuntimeException("El destinatario no debe exceder 150 caracteres");
        if (n.getAsunto() == null || n.getAsunto().trim().isEmpty())
            throw new RuntimeException("El asunto no puede estar vacio");
        if (n.getAsunto().length() > 200)
            throw new RuntimeException("El asunto no debe exceder 200 caracteres");
        if (n.getMensaje() == null || n.getMensaje().trim().isEmpty())
            throw new RuntimeException("El mensaje no puede estar vacio");
        if (n.getFechaProgramada() != null && n.getFechaProgramada().isBefore(LocalDate.now()))
            throw new RuntimeException("La fecha programada no puede ser pasada");
        if (n.getFechaEnvio() != null && n.getFechaProgramada() != null
            && n.getFechaEnvio().isBefore(n.getFechaProgramada()))
            throw new RuntimeException("La fecha de envio no puede ser anterior a la fecha programada");
        if (n.getEstadoEnvio() == null)
            throw new RuntimeException("La notificacion no tiene estado de envio");
        if (n.isLeida() && n.getFechaLectura() == null)
            throw new RuntimeException("Una notificacion leida necesita fecha de lectura");
    }

    @Override
    public int insertar(Notificacion n) {
        validar(n);
        try {
            int r = daoNotificacion.insertar(n);
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
    public int modificar(Notificacion n) {
        validar(n);
        if (n.getId() <= 0) throw new RuntimeException("Id de notificacion no valido");
        return daoNotificacion.modificar(n);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de notificacion no valido");
        return daoNotificacion.eliminar(id);
    }

    @Override
    public List<Notificacion> listarTodos() {
        return daoNotificacion.listarTodos();
    }

    @Override
    public Notificacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de notificacion no valido");
        return daoNotificacion.buscarPorId(id);
    }
}
