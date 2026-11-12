package pe.edu.pucp.admitu.services;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pe.edu.pucp.admitu.configuracion.bo.CatalogoBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.ICatalogoBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;
@Path("CatalogoRS") @Produces(MediaType.APPLICATION_JSON)
public class CatalogoRS {
    private final ICatalogoBO bo = new CatalogoBOImpl();
    @Context private SecurityContext contexto;
    @PUT @Path("reactivar/{entidad}/{id}")
    public int reactivar(@PathParam("entidad") String entidad,@PathParam("id") int id) {
        Seguridad.exigirRoles(contexto,"admin");
        return bo.reactivar(entidad,id);
    }
}
