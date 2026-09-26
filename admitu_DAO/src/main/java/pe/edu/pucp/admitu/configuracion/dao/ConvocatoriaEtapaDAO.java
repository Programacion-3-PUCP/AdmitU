package pe.edu.pucp.admitu.configuracion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaEtapa;
import java.util.List;
public interface ConvocatoriaEtapaDAO extends IDAO<ConvocatoriaEtapa> {
    List<ConvocatoriaEtapa> listarPorConvocatoria(int idConvocatoria);
}