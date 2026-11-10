package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.Carrera;
import pe.edu.pucp.admitu.configuracion.bo.CarreraBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.ICarreraBO;

@Path("CarreraRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarreraRS {

    private final ICarreraBO boCarrera;

    public CarreraRS() {
        boCarrera = new CarreraBOImpl();
    }

    @GET
    @Path("listarTodasCarreras")
    public List<Carrera> listarTodasCarreras() {
        return boCarrera.listarTodos();
    }

    @POST
    @Path("insertarCarrera")
    public int insertarCarrera(Carrera carrera) {
        return boCarrera.insertar(carrera);
    }

    @GET
    @Path("buscarCarreraPorId/{id}")
    public Carrera buscarCarreraPorId(@PathParam("id") int idCarrera) {
        return boCarrera.buscarPorId(idCarrera);
    }

    @PUT
    @Path("modificarCarrera")
    public int modificarCarrera(Carrera carrera) {
        return boCarrera.modificar(carrera);
    }

    @DELETE
    @Path("eliminarCarrera/{id}")
    public int eliminarCarrera(@PathParam("id") int idCarrera) {
        return boCarrera.eliminar(idCarrera);
    }
}
