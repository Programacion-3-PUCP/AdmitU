package pe.edu.pucp.admitu.configuracion.bo;
import pe.edu.pucp.admitu.configuracion.boi.ICatalogoBO;
import pe.edu.pucp.admitu.configuracion.dao.CatalogoDAO;
import pe.edu.pucp.admitu.configuracion.impl.CatalogoImpl;
public class CatalogoBOImpl implements ICatalogoBO {
    private final CatalogoDAO dao = new CatalogoImpl();
    public int reactivar(String entidad,int id) {
        if(id<=0) throw new IllegalArgumentException("Identificador no válido");
        int result = dao.reactivar(entidad,id);
        if(result<=0) throw new IllegalArgumentException("Registro inexistente");
        return result;
    }
}
