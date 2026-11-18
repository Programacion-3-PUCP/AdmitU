package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.EnvioPostulacion;
import pe.edu.pucp.admitu.postulacion.ExpedienteResumen;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.ResumenExpedienteBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.postulacion.boi.IResumenExpedienteBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("PostulacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostulacionRS {

    private IPostulacionBO boPostulacion;
    private IResumenExpedienteBO boResumen;

    @Context
    private SecurityContext seguridad;

    public PostulacionRS() {
        boPostulacion = new PostulacionBOImpl();
        boResumen = new ResumenExpedienteBOImpl();
    }

    @GET
    @Path("listarTodasPostulaciones")
    public List<Postulacion> listarTodasPostulaciones() {
        return boPostulacion.listarTodos();
    }

    @GET
    @Path("listarResumenesGlobales")
    public List<ExpedienteResumen> listarResumenesGlobales() {
        Seguridad.exigirRoles(seguridad, "admin", "evaluador");
        return boResumen.listarGlobales();
    }

    @POST
    @Path("insertarPostulacion")
    public int insertarPostulacion(Postulacion postulacion) {
        Seguridad.exigirRoles(seguridad, "admin");
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
        if (postulacion == null || postulacion.getEstadoActual() == null) {
            throw new BadRequestException("Estado y postulación requeridos");
        }
        Postulacion actual = Seguridad.exigirRevision(seguridad, postulacion.getId());
        actual.setEstadoActual(postulacion.getEstadoActual());
        actual.setObservacionGeneral(postulacion.getObservacionGeneral());
        return boPostulacion.modificar(actual);
    }

    @DELETE
    @Path("eliminarPostulacion/{id}")
    public int eliminarPostulacion(@PathParam("id") int idPostulacion) {
        Seguridad.exigirRoles(seguridad, "admin");
        return boPostulacion.eliminar(idPostulacion);
    }

    @POST
    @Path("enviarPostulacion")
    public int enviar(EnvioPostulacion envio) {
        Seguridad.exigirRoles(seguridad, "postulante");
        if (envio == null || envio.getPostulacion() == null || envio.getPostulacion().getPostulante() == null) {
            throw new BadRequestException("Postulante requerido");
        }
        Seguridad.exigirPropietario(seguridad, envio.getPostulacion().getPostulante().getId());
        return boPostulacion.enviar(envio);
    }

    @PUT
    @Path("asignarEvaluador/{id}/{evaluadorId}")
    public int asignar(@PathParam("id") int id, @PathParam("evaluadorId") int evaluador) {
        Seguridad.exigirRoles(seguridad, "admin");
        return boPostulacion.asignarEvaluador(id, evaluador);
    }
}
