package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.bo.PaisBOImpl;
import pe.edu.pucp.admitu.persona.boi.IPaisBO;

@Path("PaisRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaisRS {

    private final IPaisBO boPais;

    public PaisRS() {
        boPais = new PaisBOImpl();
    }

    @GET
    @Path("listarTodosPaises")
    public List<Pais> listarTodosPaises() {
        return boPais.listarTodos();
    }

    @POST
    @Path("insertarPais")
    public int insertarPais(Pais pais) {
        return boPais.insertar(pais);
    }

    @GET
    @Path("buscarPaisPorId/{id}")
    public Pais buscarPaisPorId(@PathParam("id") int idPais) {
        return boPais.buscarPorId(idPais);
    }

    @PUT
    @Path("modificarPais")
    public int modificarPais(Pais pais) {
        return boPais.modificar(pais);
    }

    @DELETE
    @Path("eliminarPais/{id}")
    public int eliminarPais(@PathParam("id") int idPais) {
        return boPais.eliminar(idPais);
    }
}
