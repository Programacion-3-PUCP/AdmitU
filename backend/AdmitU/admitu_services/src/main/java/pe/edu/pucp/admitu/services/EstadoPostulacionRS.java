package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.bo.EstadoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IEstadoPostulacionBO;

@Path("EstadoPostulacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EstadoPostulacionRS {

    private final IEstadoPostulacionBO boEstado;

    public EstadoPostulacionRS() {
        boEstado = new EstadoPostulacionBOImpl();
    }

    @GET
    @Path("listarTodosEstadosPostulacion")
    public List<EstadoPostulacion> listarTodosEstadosPostulacion() {
        return boEstado.listarTodos();
    }

    @POST
    @Path("insertarEstadoPostulacion")
    public int insertarEstadoPostulacion(EstadoPostulacion estado) {
        return boEstado.insertar(estado);
    }

    @GET
    @Path("buscarEstadoPostulacionPorId/{id}")
    public EstadoPostulacion buscarEstadoPostulacionPorId(@PathParam("id") int idEstado) {
        return boEstado.buscarPorId(idEstado);
    }

    @PUT
    @Path("modificarEstadoPostulacion")
    public int modificarEstadoPostulacion(EstadoPostulacion estado) {
        return boEstado.modificar(estado);
    }

    @DELETE
    @Path("eliminarEstadoPostulacion/{id}")
    public int eliminarEstadoPostulacion(@PathParam("id") int idEstado) {
        return boEstado.eliminar(idEstado);
    }
}
