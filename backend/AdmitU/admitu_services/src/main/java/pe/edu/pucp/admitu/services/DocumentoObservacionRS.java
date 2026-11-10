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
        return boObservacion.modificar(observacion);
    }

    @DELETE
    @Path("eliminarDocumentoObservacion/{id}")
    public int eliminarDocumentoObservacion(@PathParam("id") int idObservacion) {
        return boObservacion.eliminar(idObservacion);
    }
}
