package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.bo.EvaluadorBOImpl;
import pe.edu.pucp.admitu.persona.boi.IEvaluadorBO;

@Path("EvaluadorRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EvaluadorRS {

    private final IEvaluadorBO boEvaluador;

    public EvaluadorRS() {
        boEvaluador = new EvaluadorBOImpl();
    }

    @GET
    @Path("listarTodosEvaluadores")
    public List<Evaluador> listarTodosEvaluadores() {
        return boEvaluador.listarTodos();
    }

    @POST
    @Path("insertarEvaluador")
    public int insertarEvaluador(Evaluador evaluador) {
        return boEvaluador.insertar(evaluador);
    }

    @GET
    @Path("buscarEvaluadorPorId/{id}")
    public Evaluador buscarEvaluadorPorId(@PathParam("id") int idEvaluador) {
        return boEvaluador.buscarPorId(idEvaluador);
    }

    @PUT
    @Path("modificarEvaluador")
    public int modificarEvaluador(Evaluador evaluador) {
        return boEvaluador.modificar(evaluador);
    }

    @DELETE
    @Path("eliminarEvaluador/{id}")
    public int eliminarEvaluador(@PathParam("id") int idEvaluador) {
        return boEvaluador.eliminar(idEvaluador);
    }
}
