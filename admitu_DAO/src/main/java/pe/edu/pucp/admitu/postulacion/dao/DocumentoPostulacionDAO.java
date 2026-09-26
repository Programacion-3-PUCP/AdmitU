package pe.edu.pucp.admitu.postulacion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import java.util.List;
public interface DocumentoPostulacionDAO extends IDAO<DocumentoPostulacion> {
    List<DocumentoPostulacion> listarPorPostulacion(int idPostulacion);
}
