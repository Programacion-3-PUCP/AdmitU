package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaBO;

@Path("ConvocatoriaRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConvocatoriaRS {

    private final IConvocatoriaBO boConvocatoria;

    public ConvocatoriaRS() {
        boConvocatoria = new ConvocatoriaBOImpl();
    }

    @GET
    @Path("listarTodasConvocatorias")
    public List<Convocatoria> listarTodasConvocatorias() {
        return boConvocatoria.listarTodos();
    }

    @POST
    @Path("insertarConvocatoria")
    public int insertarConvocatoria(Convocatoria convocatoria) {
        return boConvocatoria.insertar(convocatoria);
    }

    @GET
    @Path("buscarConvocatoriaPorId/{id}")
    public Convocatoria buscarConvocatoriaPorId(@PathParam("id") int idConvocatoria) {
        return boConvocatoria.buscarPorId(idConvocatoria);
    }

    @PUT
    @Path("modificarConvocatoria")
    public int modificarConvocatoria(Convocatoria convocatoria) {
        return boConvocatoria.modificar(convocatoria);
    }

    @DELETE
    @Path("eliminarConvocatoria/{id}")
    public int eliminarConvocatoria(@PathParam("id") int idConvocatoria) {
        return boConvocatoria.eliminar(idConvocatoria);
    }
}
