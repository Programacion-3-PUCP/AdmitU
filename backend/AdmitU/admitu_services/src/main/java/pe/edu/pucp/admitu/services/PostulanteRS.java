package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.bo.PostulanteBOImpl;
import pe.edu.pucp.admitu.persona.boi.IPostulanteBO;

@Path("PostulanteRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostulanteRS {

    private final IPostulanteBO boPostulante;

    public PostulanteRS() {
        boPostulante = new PostulanteBOImpl();
    }

    @GET
    @Path("listarTodosPostulantes")
    public List<Postulante> listarTodosPostulantes() {
        return boPostulante.listarTodos();
    }

    @POST
    @Path("insertarPostulante")
    public int insertarPostulante(Postulante postulante) {
        return boPostulante.insertar(postulante);
    }

    @GET
    @Path("buscarPostulantePorId/{id}")
    public Postulante buscarPostulantePorId(@PathParam("id") int idPostulante) {
        return boPostulante.buscarPorId(idPostulante);
    }

    @PUT
    @Path("modificarPostulante")
    public int modificarPostulante(Postulante postulante) {
        return boPostulante.modificar(postulante);
    }

    @DELETE
    @Path("eliminarPostulante/{id}")
    public int eliminarPostulante(@PathParam("id") int idPostulante) {
        return boPostulante.eliminar(idPostulante);
    }
}
