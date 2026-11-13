package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoObservacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoObservacionBO;

@Path("DocumentoObservacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentoObservacionRS {

    @jakarta.ws.rs.core.Context private jakarta.ws.rs.core.SecurityContext seguridad;
    private final IDocumentoObservacionBO boObservacion;

    public DocumentoObservacionRS() {
        boObservacion = new DocumentoObservacionBOImpl();
    }

    @GET
    @Path("listarTodasDocumentoObservaciones")
    public List<DocumentoObservacion> listarTodasDocumentoObservaciones() {
        return boObservacion.listarTodos();
    }

    @POST
    @Path("insertarDocumentoObservacion")
    public int insertarDocumentoObservacion(DocumentoObservacion observacion) {
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"evaluador");
        if(observacion==null||observacion.getDocumento()==null)throw new BadRequestException("Documento requerido");
        var documento=new pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl().buscarPorId(observacion.getDocumento().getId());
        if(documento==null)throw new NotFoundException("Documento no encontrado");
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRevision(seguridad,documento.getPostulacion().getId());
        int autor=pe.edu.pucp.admitu.services.seguridad.Seguridad.id(seguridad);
        if(observacion.getEvaluador()!=null&&observacion.getEvaluador().getId()!=autor)throw new ForbiddenException("No puedes firmar con la identidad de otro evaluador");
        var evaluador=new pe.edu.pucp.admitu.persona.Evaluador();evaluador.setId(autor);
        observacion.setEvaluador(evaluador);
        observacion.setDocumento(documento);
        observacion.setFechaObservacion(java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")));
        observacion.setEstadoObservacion(pe.edu.pucp.admitu.postulacion.EstadoObservacion.PENDIENTE);
        observacion.setFechaSubsanacion(null);observacion.setComentarioSubsanacion(null);
        return boObservacion.insertar(observacion);
    }

    @GET
    @Path("buscarDocumentoObservacionPorId/{id}")
    public DocumentoObservacion buscarDocumentoObservacionPorId(@PathParam("id") int idObservacion) {
        return boObservacion.buscarPorId(idObservacion);
    }

    @PUT
    @Path("modificarDocumentoObservacion")
    public int modificarDocumentoObservacion(DocumentoObservacion observacion) {
        if(observacion==null)throw new BadRequestException("Observación requerida");
        var actual=boObservacion.buscarPorId(observacion.getId());
        if(actual==null)throw new NotFoundException("Observación no encontrada");
        var documento=new pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl().buscarPorId(actual.getDocumento().getId());
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRevision(seguridad,documento.getPostulacion().getId());
        if(observacion.getDocumento()!=null&&observacion.getDocumento().getId()!=actual.getDocumento().getId())throw new ForbiddenException("La observación no puede cambiar de documento");
        if(observacion.getEvaluador()!=null&&observacion.getEvaluador().getId()!=actual.getEvaluador().getId())throw new ForbiddenException("El autor original es inmutable");
        if(pe.edu.pucp.admitu.services.seguridad.Seguridad.rol(seguridad).equals("evaluador")&&actual.getEvaluador().getId()!=pe.edu.pucp.admitu.services.seguridad.Seguridad.id(seguridad)
            &&(!java.util.Objects.equals(actual.getDescripcion(),observacion.getDescripcion())||actual.getTipoObservacion()!=observacion.getTipoObservacion()))throw new ForbiddenException("Solo el autor puede editar el texto de su observación");
        actual.setTipoObservacion(observacion.getTipoObservacion());actual.setDescripcion(observacion.getDescripcion());
        actual.setEstadoObservacion(observacion.getEstadoObservacion());
        actual.setFechaSubsanacion(observacion.getEstadoObservacion()==pe.edu.pucp.admitu.postulacion.EstadoObservacion.SUBSANADA?java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")):null);
        actual.setComentarioSubsanacion(observacion.getComentarioSubsanacion());
        return boObservacion.modificar(actual);
    }

    @DELETE
    @Path("eliminarDocumentoObservacion/{id}")
    public int eliminarDocumentoObservacion(@PathParam("id") int idObservacion) {
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"admin");
        return boObservacion.eliminar(idObservacion);
    }
}
