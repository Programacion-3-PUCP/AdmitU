package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.OfertaCarrera;
import pe.edu.pucp.admitu.configuracion.bo.OfertaCarreraBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IOfertaCarreraBO;

@Path("OfertaCarreraRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OfertaCarreraRS {

    private final IOfertaCarreraBO boOferta;

    public OfertaCarreraRS() {
        boOferta = new OfertaCarreraBOImpl();
    }

    @GET
    @Path("listarTodasOfertasCarrera")
    public List<OfertaCarrera> listarTodasOfertasCarrera() {
        return boOferta.listarTodos();
    }

    @POST
    @Path("insertarOfertaCarrera")
    public int insertarOfertaCarrera(OfertaCarrera oferta) {
        return boOferta.insertar(oferta);
    }

    @GET
    @Path("buscarOfertaCarreraPorId/{id}")
    public OfertaCarrera buscarOfertaCarreraPorId(@PathParam("id") int idOferta) {
        return boOferta.buscarPorId(idOferta);
    }

    @PUT
    @Path("modificarOfertaCarrera")
    public int modificarOfertaCarrera(OfertaCarrera oferta) {
        return boOferta.modificar(oferta);
    }

    @DELETE
    @Path("eliminarOfertaCarrera/{id}")
    public int eliminarOfertaCarrera(@PathParam("id") int idOferta) {
        return boOferta.eliminar(idOferta);
    }
}
