package pe.edu.pucp.admitu.configuracion.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad;
import java.util.List;
public interface RequisitoConvocatoriaModalidadDAO extends IDAO<RequisitoConvocatoriaModalidad> {
    List<RequisitoConvocatoriaModalidad> listarPorConvocatoriaModalidad(int idConvocatoriaModalidad);
}