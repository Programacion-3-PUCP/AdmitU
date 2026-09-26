package pe.edu.pucp.admitu.postulacion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import java.util.List;
public interface PostulacionDAO extends IDAO<Postulacion> {
    List<Postulacion> listarPorPostulante(int idPostulante);
}