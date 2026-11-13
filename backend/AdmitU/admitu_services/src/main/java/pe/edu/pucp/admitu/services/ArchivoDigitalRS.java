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
 private ArchivoDigital buscar(int id){ArchivoDigital a=bo.buscarPorId(id);if(a==null)throw new NotFoundException("Archivo no encontrado");exigirLectura(a);return a;}
 private ArchivoDigital metadata(ArchivoDigital a){a.setContenidoBase64(null);return a;}
 @POST @Path("guardarArchivo") public ArchivoDigital guardar(ArchivoDigital a){escritura(a);return metadata(bo.guardar(a));}
 @POST @Path("guardarCertificado") public ArchivoDigital certificado(ArchivoDigital a){escritura(a);return metadata(bo.guardarCertificado(a));}
 @POST @Path("guardarVoucher") public ArchivoDigital voucher(ArchivoDigital a){escritura(a);return metadata(bo.guardarVoucher(a));}
 private boolean puedeLeer(ArchivoDigital a){
  if(!Seguridad.rol(seguridad).equals("evaluador")){Seguridad.exigirPropietario(seguridad,a.getPostulanteId());return true;}
  if(a.getTipo().equals("DOCUMENTO")){
   var d=new pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl().buscarPorId(a.getReferenciaId());
   if(d==null)return false;
   var p=new pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl().buscarPorId(d.getPostulacion().getId());
   return p!=null&&Integer.valueOf(Seguridad.id(seguridad)).equals(p.getEvaluadorId());
  }
  if(a.getTipo().equals("VOUCHER")){
   var pago=new pe.edu.pucp.admitu.pago.bo.PagoBOImpl().buscarPorId(a.getReferenciaId());
   if(pago==null)return false;
   var p=new pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl().buscarPorId(pago.getPostulacion().getId());
   return p!=null&&Integer.valueOf(Seguridad.id(seguridad)).equals(p.getEvaluadorId());
  }
  return Seguridad.tieneExpedienteAsignado(seguridad,a.getPostulanteId());
 }
 private void exigirLectura(ArchivoDigital a){if(!puedeLeer(a))throw new ForbiddenException("El archivo requiere un expediente asignado");}
 @GET @Path("listarArchivosPorPostulante/{id}") public List<ArchivoDigital> listar(@PathParam("id")int id){
  if(Seguridad.rol(seguridad).equals("evaluador")){
   if(!Seguridad.tieneExpedienteAsignado(seguridad,id))throw new ForbiddenException("El postulante no tiene expedientes asignados a tu cuenta");
   return bo.listarPorPostulante(id).stream().filter(this::puedeLeer).map(this::metadata).toList();
  }
  Seguridad.exigirPropietario(seguridad,id);return bo.listarPorPostulante(id).stream().map(this::metadata).toList();
 }
 @GET @Path("descargarArchivo/{id}") @Produces(MediaType.APPLICATION_OCTET_STREAM) public Response descargar(@PathParam("id")int id){ArchivoDigital a=buscar(id);return Response.ok(Base64.getDecoder().decode(a.getContenidoBase64()),a.getMime()).header("Content-Disposition","inline; filename=\""+a.getNombre().replace("\"","_")+"\"").header("X-Archivo-SHA256",a.getSha256()).header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").build();}
 @DELETE @Path("eliminarArchivo/{id}") public int eliminar(@PathParam("id")int id){Seguridad.exigirRoles(seguridad,"admin","postulante");buscar(id);return bo.eliminar(id);}
}
