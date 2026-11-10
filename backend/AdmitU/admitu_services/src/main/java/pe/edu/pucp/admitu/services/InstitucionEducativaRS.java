package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.persona.InstitucionEducativa;
import pe.edu.pucp.admitu.persona.bo.InstitucionEducativaBOImpl;
import pe.edu.pucp.admitu.persona.boi.IInstitucionEducativaBO;

@Path("InstitucionEducativaRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InstitucionEducativaRS {

    private final IInstitucionEducativaBO boInstitucion;

    public InstitucionEducativaRS() {
        boInstitucion = new InstitucionEducativaBOImpl();
    }

    @GET
    @Path("listarTodasInstitucionesEducativas")
    public List<InstitucionEducativa> listarTodasInstitucionesEducativas() {
        return boInstitucion.listarTodos();
    }

    @POST
    @Path("insertarInstitucionEducativa")
    public int insertarInstitucionEducativa(InstitucionEducativa institucion) {
        return boInstitucion.insertar(institucion);
    }

    @GET
    @Path("buscarInstitucionEducativaPorId/{id}")
    public InstitucionEducativa buscarInstitucionEducativaPorId(@PathParam("id") int idInstitucion) {
        return boInstitucion.buscarPorId(idInstitucion);
    }

    @PUT
    @Path("modificarInstitucionEducativa")
    public int modificarInstitucionEducativa(InstitucionEducativa institucion) {
        return boInstitucion.modificar(institucion);
    }

    @DELETE
    @Path("eliminarInstitucionEducativa/{id}")
    public int eliminarInstitucionEducativa(@PathParam("id") int idInstitucion) {
        return boInstitucion.eliminar(idInstitucion);
    }
}
