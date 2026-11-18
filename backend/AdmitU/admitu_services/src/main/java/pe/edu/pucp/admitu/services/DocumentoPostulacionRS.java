package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoPostulacionBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("DocumentoPostulacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentoPostulacionRS {

    private IDocumentoPostulacionBO boDocumento;

    @Context
    private SecurityContext seguridad;

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
        Seguridad.exigirRoles(seguridad, "admin");
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
        if (documento == null || documento.getEstadoDocumento() == null) {
            throw new BadRequestException("Documento y estado requeridos");
        }
        DocumentoPostulacion actual = boDocumento.buscarPorId(documento.getId());
        if (actual == null) {
            throw new NotFoundException("Documento no encontrado");
        }
        Seguridad.exigirRevision(seguridad, actual.getPostulacion().getId());
        if (documento.getPostulacion() != null
                && documento.getPostulacion().getId() != actual.getPostulacion().getId()) {
            throw new ForbiddenException("No puedes cambiar el expediente del documento");
        }
        actual.setEstadoDocumento(documento.getEstadoDocumento());
        actual.setComentarioEvaluacion(documento.getComentarioEvaluacion());
        actual.setFechaEvaluacion(LocalDate.now(ZoneId.of("America/Lima")));
        return boDocumento.modificar(actual);
    }

    @DELETE
    @Path("eliminarDocumentoPostulacion/{id}")
    public int eliminarDocumentoPostulacion(@PathParam("id") int idDocumento) {
        Seguridad.exigirRoles(seguridad, "admin");
        return boDocumento.eliminar(idDocumento);
    }
}
