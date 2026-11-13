package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoPostulacionBO;

@Path("DocumentoPostulacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentoPostulacionRS {

    @jakarta.ws.rs.core.Context private jakarta.ws.rs.core.SecurityContext seguridad;
    private final IDocumentoPostulacionBO boDocumento;

    public DocumentoPostulacionRS() {
        boDocumento = new DocumentoPostulacionBOImpl();
    }

    @GET
    @Path("listarTodosDocumentosPostulacion")
    public List<DocumentoPostulacion> listarTodosDocumentosPostulacion() {
        return boDocumento.listarTodos();
    }

    @POST
    @Path("insertarDocumentoPostulacion")
    public int insertarDocumentoPostulacion(DocumentoPostulacion documento) {
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"admin");
        return boDocumento.insertar(documento);
    }

    @GET
    @Path("buscarDocumentoPostulacionPorId/{id}")
    public DocumentoPostulacion buscarDocumentoPostulacionPorId(@PathParam("id") int idDocumento) {
        return boDocumento.buscarPorId(idDocumento);
    }

    @PUT
    @Path("modificarDocumentoPostulacion")
    public int modificarDocumentoPostulacion(DocumentoPostulacion documento) {
        if(documento==null||documento.getEstadoDocumento()==null)throw new BadRequestException("Documento y estado requeridos");
        DocumentoPostulacion actual=boDocumento.buscarPorId(documento.getId());
        if(actual==null)throw new NotFoundException("Documento no encontrado");
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRevision(seguridad,actual.getPostulacion().getId());
        if(documento.getPostulacion()!=null&&documento.getPostulacion().getId()!=actual.getPostulacion().getId())throw new ForbiddenException("No puedes cambiar el expediente del documento");
        actual.setEstadoDocumento(documento.getEstadoDocumento());
        actual.setComentarioEvaluacion(documento.getComentarioEvaluacion());
        actual.setFechaEvaluacion(java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")));
        return boDocumento.modificar(actual);
    }

    @DELETE
    @Path("eliminarDocumentoPostulacion/{id}")
    public int eliminarDocumentoPostulacion(@PathParam("id") int idDocumento) {
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"admin");
        return boDocumento.eliminar(idDocumento);
    }
}
