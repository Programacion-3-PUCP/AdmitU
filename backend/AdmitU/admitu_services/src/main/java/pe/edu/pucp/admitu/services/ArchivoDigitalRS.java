package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.bo.ArchivoBOImpl;
import pe.edu.pucp.admitu.archivo.boi.IArchivoBO;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.pago.boi.IPagoBO;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;
import pe.edu.pucp.admitu.persona.bo.AntecedenteAcademicoBOImpl;
import pe.edu.pucp.admitu.persona.boi.IAntecedenteAcademicoBO;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoPostulacionBO;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("ArchivoDigitalRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ArchivoDigitalRS {

    private IArchivoBO boArchivo;
    private IDocumentoPostulacionBO boDocumento;
    private IPostulacionBO boPostulacion;
    private IPagoBO boPago;
    private IAntecedenteAcademicoBO boAntecedente;

    @Context
    private SecurityContext seguridad;

    public ArchivoDigitalRS() {
        boArchivo = new ArchivoBOImpl();
        boDocumento = new DocumentoPostulacionBOImpl();
        boPostulacion = new PostulacionBOImpl();
        boPago = new PagoBOImpl();
        boAntecedente = new AntecedenteAcademicoBOImpl();
    }

    private void validarEscritura(ArchivoDigital archivo) {
        Seguridad.exigirRoles(seguridad, "admin", "postulante");
        if (archivo == null) {
            throw new BadRequestException("Adjunta un archivo");
        }
        Seguridad.exigirPropietario(seguridad, archivo.getPostulanteId());

        if (archivo.getReferenciaId() > 0 && "DOCUMENTO".equals(archivo.getTipo())) {
            DocumentoPostulacion doc = boDocumento.buscarPorId(archivo.getReferenciaId());
            if (doc == null) {
                throw new NotFoundException("Documento no encontrado");
            }
            Postulacion postulacion = boPostulacion.buscarPorId(doc.getPostulacion().getId());
            if (postulacion.getPostulante().getId() != archivo.getPostulanteId()) {
                throw new ForbiddenException("El documento pertenece a otro postulante");
            }
        }

        if (archivo.getReferenciaId() > 0 && "VOUCHER".equals(archivo.getTipo())) {
            Pago pago = boPago.buscarPorId(archivo.getReferenciaId());
            if (pago == null) {
                throw new NotFoundException("Pago no encontrado");
            }
            Postulacion postulacion = boPostulacion.buscarPorId(pago.getPostulacion().getId());
            if (postulacion.getPostulante().getId() != archivo.getPostulanteId()) {
                throw new ForbiddenException("El pago pertenece a otro postulante");
            }
        }

        if (archivo.getReferenciaId() > 0 && "CERTIFICADO".equals(archivo.getTipo())) {
            AntecedenteAcademico antecedente = boAntecedente.buscarPorId(archivo.getReferenciaId());
            if (antecedente == null) {
                throw new NotFoundException("Antecedente no encontrado");
            }
            if (antecedente.getPostulante().getId() != archivo.getPostulanteId()) {
                throw new ForbiddenException("El certificado pertenece a otro postulante");
            }
        }
    }

    private ArchivoDigital buscar(int id) {
        ArchivoDigital archivo = boArchivo.buscarPorId(id);
        if (archivo == null) {
            throw new NotFoundException("Archivo no encontrado");
        }
        exigirLectura(archivo);
        return archivo;
    }

    private ArchivoDigital limpiarContenido(ArchivoDigital archivo) {
        archivo.setContenidoBase64(null);
        return archivo;
    }

    @POST
    @Path("guardarArchivo")
    public ArchivoDigital guardar(ArchivoDigital archivo) {
        validarEscritura(archivo);
        return limpiarContenido(boArchivo.guardar(archivo));
    }

    @POST
    @Path("guardarCertificado")
    public ArchivoDigital certificado(ArchivoDigital archivo) {
        validarEscritura(archivo);
        return limpiarContenido(boArchivo.guardarCertificado(archivo));
    }

    @POST
    @Path("guardarVoucher")
    public ArchivoDigital voucher(ArchivoDigital archivo) {
        validarEscritura(archivo);
        return limpiarContenido(boArchivo.guardarVoucher(archivo));
    }

    private boolean puedeLeer(ArchivoDigital archivo) {
        if (!"evaluador".equals(Seguridad.rol(seguridad))) {
            Seguridad.exigirPropietario(seguridad, archivo.getPostulanteId());
            return true;
        }
        if ("DOCUMENTO".equals(archivo.getTipo())) {
            DocumentoPostulacion doc = boDocumento.buscarPorId(archivo.getReferenciaId());
            if (doc == null) {
                return false;
            }
            Postulacion postulacion = boPostulacion.buscarPorId(doc.getPostulacion().getId());
            return postulacion != null && Integer.valueOf(Seguridad.id(seguridad)).equals(postulacion.getEvaluadorId());
        }
        if ("VOUCHER".equals(archivo.getTipo())) {
            Pago pago = boPago.buscarPorId(archivo.getReferenciaId());
            if (pago == null) {
                return false;
            }
            Postulacion postulacion = boPostulacion.buscarPorId(pago.getPostulacion().getId());
            return postulacion != null && Integer.valueOf(Seguridad.id(seguridad)).equals(postulacion.getEvaluadorId());
        }
        return Seguridad.tieneExpedienteAsignado(seguridad, archivo.getPostulanteId());
    }

    private void exigirLectura(ArchivoDigital archivo) {
        if (!puedeLeer(archivo)) {
            throw new ForbiddenException("El archivo requiere un expediente asignado");
        }
    }

    @GET
    @Path("listarArchivosPorPostulante/{id}")
    public List<ArchivoDigital> listar(@PathParam("id") int id) {
        List<ArchivoDigital> resultado = new ArrayList<>();
        if ("evaluador".equals(Seguridad.rol(seguridad))) {
            if (!Seguridad.tieneExpedienteAsignado(seguridad, id)) {
                throw new ForbiddenException("El postulante no tiene expedientes asignados a tu cuenta");
            }
            for (ArchivoDigital a : boArchivo.listarPorPostulante(id)) {
                if (puedeLeer(a)) {
                    resultado.add(limpiarContenido(a));
                }
            }
            return resultado;
        }
        Seguridad.exigirPropietario(seguridad, id);
        for (ArchivoDigital a : boArchivo.listarPorPostulante(id)) {
            resultado.add(limpiarContenido(a));
        }
        return resultado;
    }

    @GET
    @Path("descargarArchivo/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response descargar(@PathParam("id") int id) {
        ArchivoDigital archivo = buscar(id);
        byte[] contenido = Base64.getDecoder().decode(archivo.getContenidoBase64());
        String nombreSeguro = archivo.getNombre().replace("\"", "_");
        return Response.ok(contenido, archivo.getMime())
                .header("Content-Disposition", "inline; filename=\"" + nombreSeguro + "\"")
                .header("X-Archivo-SHA256", archivo.getSha256())
                .header("Cache-Control", "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

    @DELETE
    @Path("eliminarArchivo/{id}")
    public int eliminar(@PathParam("id") int id) {
        Seguridad.exigirRoles(seguridad, "admin", "postulante");
        buscar(id);
        return boArchivo.eliminar(id);
    }
}
