package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import pe.edu.pucp.admitu.configuracion.bo.CatalogoBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.ICatalogoBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("CatalogoRS")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoRS {

    private ICatalogoBO boCatalogo;

    @Context
    private SecurityContext contexto;

    public CatalogoRS() {
        boCatalogo = new CatalogoBOImpl();
    }

    @PUT
    @Path("reactivar/{entidad}/{id}")
    public int reactivar(@PathParam("entidad") String entidad, @PathParam("id") int id) {
        Seguridad.exigirRoles(contexto, "admin");
        return boCatalogo.reactivar(entidad, id);
    }
}
