package pe.edu.pucp.admitu.postulacion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import java.util.List;
public interface DocumentoObservacionDAO extends IDAO<DocumentoObservacion> {
    List<DocumentoObservacion> listarPorDocumento(int idDocumento);
}