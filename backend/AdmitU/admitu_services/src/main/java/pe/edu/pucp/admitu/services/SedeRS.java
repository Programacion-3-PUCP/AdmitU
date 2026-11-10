package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.configuracion.bo.SedeBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.ISedeBO;

@Path("SedeRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SedeRS {

    private final ISedeBO boSede;

    public SedeRS() {
        boSede = new SedeBOImpl();
    }

    @GET
    @Path("listarTodasSedes")
    public List<Sede> listarTodasSedes() {
        return boSede.listarTodos();
    }

    @POST
    @Path("insertarSede")
    public int insertarSede(Sede sede) {
        return boSede.insertar(sede);
    }

    @GET
    @Path("buscarSedePorId/{id}")
    public Sede buscarSedePorId(@PathParam("id") int idSede) {
        return boSede.buscarPorId(idSede);
    }

    @PUT
    @Path("modificarSede")
    public int modificarSede(Sede sede) {
        return boSede.modificar(sede);
    }

    @DELETE
    @Path("eliminarSede/{id}")
    public int eliminarSede(@PathParam("id") int idSede) {
        return boSede.eliminar(idSede);
    }
}
