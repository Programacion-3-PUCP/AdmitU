package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionHistorialBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionHistorialBO;

@Path("PostulacionHistorialRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostulacionHistorialRS {

    private final IPostulacionHistorialBO boHistorial;

    public PostulacionHistorialRS() {
        boHistorial = new PostulacionHistorialBOImpl();
    }

    @GET
    @Path("listarTodosPostulacionHistorial")
    public List<PostulacionHistorial> listarTodosPostulacionHistorial() {
        return boHistorial.listarTodos();
    }

    @POST
    @Path("insertarPostulacionHistorial")
    public int insertarPostulacionHistorial(PostulacionHistorial historial) {
        throw new ForbiddenException("El historial es de solo lectura");
    }

    @GET
    @Path("buscarPostulacionHistorialPorId/{id}")
    public PostulacionHistorial buscarPostulacionHistorialPorId(@PathParam("id") int idHistorial) {
        return boHistorial.buscarPorId(idHistorial);
    }

    @PUT
    @Path("modificarPostulacionHistorial")
    public int modificarPostulacionHistorial(PostulacionHistorial historial) {
        throw new ForbiddenException("El historial es de solo lectura");
    }

    @DELETE
    @Path("eliminarPostulacionHistorial/{id}")
    public int eliminarPostulacionHistorial(@PathParam("id") int idHistorial) {
        throw new ForbiddenException("El historial es de solo lectura");
    }
}
