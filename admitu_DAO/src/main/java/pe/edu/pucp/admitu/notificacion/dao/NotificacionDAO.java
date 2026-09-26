package pe.edu.pucp.admitu.notificacion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.notificacion.Notificacion;
import java.util.List;
public interface NotificacionDAO extends IDAO<Notificacion> {
    List<Notificacion> listarPorPostulacion(int idPostulacion);
}