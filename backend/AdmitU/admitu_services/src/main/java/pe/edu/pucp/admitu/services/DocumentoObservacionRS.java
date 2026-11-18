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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.EstadoObservacion;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoObservacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoObservacionBO;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoPostulacionBO;
import pe.edu.pucp.admitu.services.dto.DocumentoObservacionLectura;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("DocumentoObservacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentoObservacionRS {

    private IDocumentoObservacionBO boObservacion;
    private IDocumentoPostulacionBO boDocumento;

    @Context
    private SecurityContext seguridad;

    public DocumentoObservacionRS() {
        boObservacion = new DocumentoObservacionBOImpl();
        boDocumento = new DocumentoPostulacionBOImpl();
    }

    @GET
    @Path("listarTodasDocumentoObservaciones")
    public List<?> listarTodasDocumentoObservaciones() {
        List<DocumentoObservacion> filas = boObservacion.listarTodos();
        if (!"admin".equals(Seguridad.rol(seguridad))) {
            List<DocumentoObservacionLectura> lectura = new ArrayList<>();
            for (DocumentoObservacion fila : filas) {
                lectura.add(new DocumentoObservacionLectura(fila));
            }
            return lectura;
        }
        return filas;
    }

    @POST
    @Path("insertarDocumentoObservacion")
    public int insertarDocumentoObservacion(DocumentoObservacion observacion) {
        Seguridad.exigirRoles(seguridad, "evaluador");
        if (observacion == null || observacion.getDocumento() == null) {
            throw new BadRequestException("Documento requerido");
        }
        DocumentoPostulacion documento = boDocumento.buscarPorId(observacion.getDocumento().getId());
        if (documento == null) {
            throw new NotFoundException("Documento no encontrado");
        }
        Seguridad.exigirRevision(seguridad, documento.getPostulacion().getId());
        int autor = Seguridad.id(seguridad);
        if (observacion.getEvaluador() != null && observacion.getEvaluador().getId() != autor) {
            throw new ForbiddenException("No puedes firmar con la identidad de otro evaluador");
        }
        Evaluador evaluador = new Evaluador();
        evaluador.setId(autor);
        observacion.setEvaluador(evaluador);
        observacion.setDocumento(documento);
        observacion.setFechaObservacion(LocalDate.now(ZoneId.of("America/Lima")));
        observacion.setEstadoObservacion(EstadoObservacion.PENDIENTE);
        observacion.setFechaSubsanacion(null);
        observacion.setComentarioSubsanacion(null);
        return boObservacion.insertar(observacion);
    }

    @GET
    @Path("buscarDocumentoObservacionPorId/{id}")
    public Object buscarDocumentoObservacionPorId(@PathParam("id") int idObservacion) {
        DocumentoObservacion observacion = boObservacion.buscarPorId(idObservacion);
        if (observacion != null && !"admin".equals(Seguridad.rol(seguridad))) {
            return new DocumentoObservacionLectura(observacion);
        }
        return observacion;
    }

    @PUT
    @Path("modificarDocumentoObservacion")
    public int modificarDocumentoObservacion(DocumentoObservacion observacion) {
        Seguridad.exigirRoles(seguridad, "admin", "evaluador");
        if (observacion == null) {
            throw new BadRequestException("Observación requerida");
        }
        DocumentoObservacion actual = boObservacion.buscarPorId(observacion.getId());
        if (actual == null) {
            throw new NotFoundException("Observación no encontrada");
        }
        DocumentoPostulacion documento = boDocumento.buscarPorId(actual.getDocumento().getId());
        Seguridad.exigirRevision(seguridad, documento.getPostulacion().getId());
        if (observacion.getDocumento() != null && observacion.getDocumento().getId() != actual.getDocumento().getId()) {
            throw new ForbiddenException("La observación no puede cambiar de documento");
        }
        if (observacion.getEvaluador() != null && observacion.getEvaluador().getId() != actual.getEvaluador().getId()) {
            throw new ForbiddenException("El autor original es inmutable");
        }
        boolean cambiaTexto = !Objects.equals(actual.getDescripcion(), observacion.getDescripcion())
                || actual.getTipoObservacion() != observacion.getTipoObservacion();
        if (cambiaTexto && "evaluador".equals(Seguridad.rol(seguridad))
                && actual.getEvaluador().getId() != Seguridad.id(seguridad)) {
            throw new ForbiddenException("Solo el autor puede editar el texto de su observación");
        }
        if (observacion.getEstadoObservacion() == null) {
            throw new BadRequestException("Estado de observación requerido");
        }
        if (actual.getEstadoObservacion() == EstadoObservacion.SUBSANADA
                && observacion.getEstadoObservacion() != actual.getEstadoObservacion()) {
            throw new ForbiddenException("La subsanación es irreversible; registra otra observación");
        }
        if (actual.getEstadoObservacion() != observacion.getEstadoObservacion()) {
            actual.setFechaSubsanacion(LocalDate.now(ZoneId.of("America/Lima")));
            actual.setComentarioSubsanacion(observacion.getComentarioSubsanacion());
        }
        actual.setEstadoObservacion(observacion.getEstadoObservacion());
        actual.setTipoObservacion(observacion.getTipoObservacion());
        actual.setDescripcion(observacion.getDescripcion());
        return boObservacion.modificar(actual);
    }

    @DELETE
    @Path("eliminarDocumentoObservacion/{id}")
    public int eliminarDocumentoObservacion(@PathParam("id") int idObservacion) {
        Seguridad.exigirRoles(seguridad, "admin");
        return boObservacion.eliminar(idObservacion);
    }
}
