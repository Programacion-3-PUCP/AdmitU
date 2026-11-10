package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.CarnePostulante;
import pe.edu.pucp.admitu.postulacion.bo.CarnePostulanteBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.ICarnePostulanteBO;

@Path("CarnePostulanteRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarnePostulanteRS {

    private final ICarnePostulanteBO boCarne;

    public CarnePostulanteRS() {
        boCarne = new CarnePostulanteBOImpl();
    }

    @GET
    @Path("listarTodosCarnesPostulante")
    public List<CarnePostulante> listarTodosCarnesPostulante() {
        return boCarne.listarTodos();
    }

    @POST
    @Path("insertarCarnePostulante")
    public int insertarCarnePostulante(CarnePostulante carne) {
        return boCarne.insertar(carne);
    }

    @GET
    @Path("buscarCarnePostulantePorId/{id}")
    public CarnePostulante buscarCarnePostulantePorId(@PathParam("id") int idCarne) {
        return boCarne.buscarPorId(idCarne);
    }

    @PUT
    @Path("modificarCarnePostulante")
    public int modificarCarnePostulante(CarnePostulante carne) {
        return boCarne.modificar(carne);
    }

    @DELETE
    @Path("eliminarCarnePostulante/{id}")
    public int eliminarCarnePostulante(@PathParam("id") int idCarne) {
        return boCarne.eliminar(idCarne);
    }
}
