package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;

@Path("PostulacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostulacionRS {

    private final IPostulacionBO boPostulacion;

    public PostulacionRS() {
        boPostulacion = new PostulacionBOImpl();
    }

    @GET
    @Path("listarTodasPostulaciones")
    public List<Postulacion> listarTodasPostulaciones() {
        return boPostulacion.listarTodos();
    }

    @POST
    @Path("insertarPostulacion")
    public int insertarPostulacion(Postulacion postulacion) {
        return boPostulacion.insertar(postulacion);
    }

    @GET
    @Path("buscarPostulacionPorId/{id}")
    public Postulacion buscarPostulacionPorId(@PathParam("id") int idPostulacion) {
        return boPostulacion.buscarPorId(idPostulacion);
    }

    @PUT
    @Path("modificarPostulacion")
    public int modificarPostulacion(Postulacion postulacion) {
        return boPostulacion.modificar(postulacion);
    }

    @DELETE
    @Path("eliminarPostulacion/{id}")
    public int eliminarPostulacion(@PathParam("id") int idPostulacion) {
        return boPostulacion.eliminar(idPostulacion);
    }

    @POST @Path("enviarPostulacion")
    public int enviar(pe.edu.pucp.admitu.postulacion.EnvioPostulacion envio){
        if(envio==null||envio.getPostulacion()==null||envio.getPostulacion().getPostulante()==null)throw new BadRequestException("Postulante requerido");
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirPropietario(seguridad,envio.getPostulacion().getPostulante().getId());
        return boPostulacion.enviar(envio);
    }
    @jakarta.ws.rs.core.Context private jakarta.ws.rs.core.SecurityContext seguridad;
    @PUT @Path("asignarEvaluador/{id}/{evaluadorId}")
    public int asignar(@PathParam("id")int id,@PathParam("evaluadorId")int evaluador){
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"admin");return boPostulacion.asignarEvaluador(id,evaluador);
    }
}
