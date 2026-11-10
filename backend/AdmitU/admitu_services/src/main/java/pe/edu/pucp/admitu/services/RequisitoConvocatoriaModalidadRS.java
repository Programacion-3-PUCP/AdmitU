package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.bo.RequisitoConvocatoriaModalidadBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IRequisitoConvocatoriaModalidadBO;

@Path("RequisitoConvocatoriaModalidadRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RequisitoConvocatoriaModalidadRS {

    private final IRequisitoConvocatoriaModalidadBO boRelacion;

    public RequisitoConvocatoriaModalidadRS() {
        boRelacion = new RequisitoConvocatoriaModalidadBOImpl();
    }

    @GET
    @Path("listarTodosRequisitosConvocatoriaModalidad")
    public List<RequisitoConvocatoriaModalidad> listarTodosRequisitosConvocatoriaModalidad() {
        return boRelacion.listarTodos();
    }

    @POST
    @Path("insertarRequisitoConvocatoriaModalidad")
    public int insertarRequisitoConvocatoriaModalidad(RequisitoConvocatoriaModalidad requisitoConvocatoriaModalidad) {
        return boRelacion.insertar(requisitoConvocatoriaModalidad);
    }

    @GET
    @Path("buscarRequisitoConvocatoriaModalidadPorId/{id}")
    public RequisitoConvocatoriaModalidad buscarRequisitoConvocatoriaModalidadPorId(@PathParam("id") int idRelacion) {
        return boRelacion.buscarPorId(idRelacion);
    }

    @PUT
    @Path("modificarRequisitoConvocatoriaModalidad")
    public int modificarRequisitoConvocatoriaModalidad(RequisitoConvocatoriaModalidad requisitoConvocatoriaModalidad) {
        return boRelacion.modificar(requisitoConvocatoriaModalidad);
    }

    @DELETE
    @Path("eliminarRequisitoConvocatoriaModalidad/{id}")
    public int eliminarRequisitoConvocatoriaModalidad(@PathParam("id") int idRelacion) {
        return boRelacion.eliminar(idRelacion);
    }
}
