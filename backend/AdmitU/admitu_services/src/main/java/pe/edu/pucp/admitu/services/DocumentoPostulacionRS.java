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
        return boDocumento.modificar(documento);
    }

    @DELETE
    @Path("eliminarDocumentoPostulacion/{id}")
    public int eliminarDocumentoPostulacion(@PathParam("id") int idDocumento) {
        return boDocumento.eliminar(idDocumento);
    }
}
