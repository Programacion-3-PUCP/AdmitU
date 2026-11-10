package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaModalidadBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaModalidadBO;

@Path("ConvocatoriaModalidadRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConvocatoriaModalidadRS {

    private final IConvocatoriaModalidadBO boConvMod;

    public ConvocatoriaModalidadRS() {
        boConvMod = new ConvocatoriaModalidadBOImpl();
    }

    @GET
    @Path("listarTodasConvocatoriasModalidad")
    public List<ConvocatoriaModalidad> listarTodasConvocatoriasModalidad() {
        return boConvMod.listarTodos();
    }

    @POST
    @Path("insertarConvocatoriaModalidad")
    public int insertarConvocatoriaModalidad(ConvocatoriaModalidad convocatoriaModalidad) {
        return boConvMod.insertar(convocatoriaModalidad);
    }

    @GET
    @Path("buscarConvocatoriaModalidadPorId/{id}")
    public ConvocatoriaModalidad buscarConvocatoriaModalidadPorId(@PathParam("id") int idConvocatoriaModalidad) {
        return boConvMod.buscarPorId(idConvocatoriaModalidad);
    }

    @PUT
    @Path("modificarConvocatoriaModalidad")
    public int modificarConvocatoriaModalidad(ConvocatoriaModalidad convocatoriaModalidad) {
        return boConvMod.modificar(convocatoriaModalidad);
    }

    @DELETE
    @Path("eliminarConvocatoriaModalidad/{id}")
    public int eliminarConvocatoriaModalidad(@PathParam("id") int idConvocatoriaModalidad) {
        return boConvMod.eliminar(idConvocatoriaModalidad);
    }
}
