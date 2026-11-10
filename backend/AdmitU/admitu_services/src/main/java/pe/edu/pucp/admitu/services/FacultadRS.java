package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.Facultad;
import pe.edu.pucp.admitu.configuracion.bo.FacultadBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IFacultadBO;

@Path("FacultadRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FacultadRS {

    private final IFacultadBO boFacultad;

    public FacultadRS() {
        boFacultad = new FacultadBOImpl();
    }

    @GET
    @Path("listarTodasFacultades")
    public List<Facultad> listarTodasFacultades() {
        return boFacultad.listarTodos();
    }

    @POST
    @Path("insertarFacultad")
    public int insertarFacultad(Facultad facultad) {
        return boFacultad.insertar(facultad);
    }

    @GET
    @Path("buscarFacultadPorId/{id}")
    public Facultad buscarFacultadPorId(@PathParam("id") int idFacultad) {
        return boFacultad.buscarPorId(idFacultad);
    }

    @PUT
    @Path("modificarFacultad")
    public int modificarFacultad(Facultad facultad) {
        return boFacultad.modificar(facultad);
    }

    @DELETE
    @Path("eliminarFacultad/{id}")
    public int eliminarFacultad(@PathParam("id") int idFacultad) {
        return boFacultad.eliminar(idFacultad);
    }
}
