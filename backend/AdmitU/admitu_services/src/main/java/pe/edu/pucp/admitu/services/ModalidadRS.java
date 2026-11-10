package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.bo.ModalidadBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IModalidadBO;

@Path("ModalidadRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ModalidadRS {

    private final IModalidadBO boModalidad;

    public ModalidadRS() {
        boModalidad = new ModalidadBOImpl();
    }

    @GET
    @Path("listarTodasModalidades")
    public List<Modalidad> listarTodasModalidades() {
        return boModalidad.listarTodos();
    }

    @POST
    @Path("insertarModalidad")
    public int insertarModalidad(Modalidad modalidad) {
        return boModalidad.insertar(modalidad);
    }

    @GET
    @Path("buscarModalidadPorId/{id}")
    public Modalidad buscarModalidadPorId(@PathParam("id") int idModalidad) {
        return boModalidad.buscarPorId(idModalidad);
    }

    @PUT
    @Path("modificarModalidad")
    public int modificarModalidad(Modalidad modalidad) {
        return boModalidad.modificar(modalidad);
    }

    @DELETE
    @Path("eliminarModalidad/{id}")
    public int eliminarModalidad(@PathParam("id") int idModalidad) {
        return boModalidad.eliminar(idModalidad);
    }
}
