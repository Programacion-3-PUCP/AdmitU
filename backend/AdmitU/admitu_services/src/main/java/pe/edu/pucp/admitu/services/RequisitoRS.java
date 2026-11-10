package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.bo.RequisitoBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IRequisitoBO;

@Path("RequisitoRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RequisitoRS {

    private final IRequisitoBO boRequisito;

    public RequisitoRS() {
        boRequisito = new RequisitoBOImpl();
    }

    @GET
    @Path("listarTodosRequisitos")
    public List<Requisito> listarTodosRequisitos() {
        return boRequisito.listarTodos();
    }

    @POST
    @Path("insertarRequisito")
    public int insertarRequisito(Requisito requisito) {
        return boRequisito.insertar(requisito);
    }

    @GET
    @Path("buscarRequisitoPorId/{id}")
    public Requisito buscarRequisitoPorId(@PathParam("id") int idRequisito) {
        return boRequisito.buscarPorId(idRequisito);
    }

    @PUT
    @Path("modificarRequisito")
    public int modificarRequisito(Requisito requisito) {
        return boRequisito.modificar(requisito);
    }

    @DELETE
    @Path("eliminarRequisito/{id}")
    public int eliminarRequisito(@PathParam("id") int idRequisito) {
        return boRequisito.eliminar(idRequisito);
    }
}
