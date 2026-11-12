package pe.edu.pucp.admitu.services;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.bo.ArchivoBOImpl;
import pe.edu.pucp.admitu.archivo.boi.IArchivoBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;
@Path("ArchivoDigitalRS") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class ArchivoDigitalRS {
 private final IArchivoBO bo=new ArchivoBOImpl();@Context private SecurityContext seguridad;
 private void escritura(ArchivoDigital a){Seguridad.exigirRoles(seguridad,"admin","postulante");if(a==null)throw new BadRequestException("Adjunta un archivo");Seguridad.exigirPropietario(seguridad,a.getPostulanteId());}
 private ArchivoDigital buscar(int id){ArchivoDigital a=bo.buscarPorId(id);if(a==null)throw new NotFoundException("Archivo no encontrado");Seguridad.exigirPropietario(seguridad,a.getPostulanteId());return a;}
 private ArchivoDigital metadata(ArchivoDigital a){a.setContenidoBase64(null);return a;}
 @POST @Path("guardarArchivo") public ArchivoDigital guardar(ArchivoDigital a){escritura(a);return metadata(bo.guardar(a));}
 @POST @Path("guardarCertificado") public ArchivoDigital certificado(ArchivoDigital a){escritura(a);return metadata(bo.guardarCertificado(a));}
 @POST @Path("guardarVoucher") public ArchivoDigital voucher(ArchivoDigital a){escritura(a);return metadata(bo.guardarVoucher(a));}
 @GET @Path("listarArchivosPorPostulante/{id}") public List<ArchivoDigital> listar(@PathParam("id")int id){Seguridad.exigirPropietario(seguridad,id);return bo.listarPorPostulante(id);}
 @GET @Path("descargarArchivo/{id}") @Produces(MediaType.APPLICATION_OCTET_STREAM) public Response descargar(@PathParam("id")int id){ArchivoDigital a=buscar(id);return Response.ok(Base64.getDecoder().decode(a.getContenidoBase64()),a.getMime()).header("Content-Disposition","inline; filename=\""+a.getNombre().replace("\"","_")+"\"").header("X-Archivo-SHA256",a.getSha256()).header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").build();}
 @DELETE @Path("eliminarArchivo/{id}") public int eliminar(@PathParam("id")int id){Seguridad.exigirRoles(seguridad,"admin","postulante");buscar(id);return bo.eliminar(id);}
}
