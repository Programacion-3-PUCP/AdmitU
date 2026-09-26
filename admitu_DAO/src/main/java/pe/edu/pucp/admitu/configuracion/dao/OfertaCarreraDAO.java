package pe.edu.pucp.admitu.configuracion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.configuracion.OfertaCarrera;
import java.util.List;
public interface OfertaCarreraDAO extends IDAO<OfertaCarrera> {
    List<OfertaCarrera> listarPorConvocatoria(int idConvocatoria);
}