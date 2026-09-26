package pe.edu.pucp.admitu.configuracion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import java.util.List;
public interface ConvocatoriaModalidadDAO extends IDAO<ConvocatoriaModalidad> {
    List<ConvocatoriaModalidad> listarPorConvocatoria(int idConvocatoria);
}