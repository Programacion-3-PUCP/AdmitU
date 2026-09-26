package pe.edu.pucp.admitu.postulacion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import java.util.List;
public interface PostulacionHistorialDAO extends IDAO<PostulacionHistorial> {
    List<PostulacionHistorial> listarPorPostulacion(int idPostulacion);
}
