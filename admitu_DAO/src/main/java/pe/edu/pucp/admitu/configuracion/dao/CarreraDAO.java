package pe.edu.pucp.admitu.configuracion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.configuracion.Carrera;
import java.util.List;
public interface CarreraDAO extends IDAO<Carrera> {
    List<Carrera> listarPorFacultad(int idFacultad);
}