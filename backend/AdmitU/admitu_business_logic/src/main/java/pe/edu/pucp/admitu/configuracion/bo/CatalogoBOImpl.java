package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.configuracion.boi.ICatalogoBO;
import pe.edu.pucp.admitu.configuracion.dao.CatalogoDAO;
import pe.edu.pucp.admitu.configuracion.impl.CatalogoImpl;

public class CatalogoBOImpl implements ICatalogoBO {

    private CatalogoDAO daoCatalogo;

    public CatalogoBOImpl() {
        daoCatalogo = new CatalogoImpl();
    }

    @Override
    public int reactivar(String entidad, int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Identificador inválido");
        }
        return daoCatalogo.reactivar(entidad, id);
    }
}
