package pe.edu.pucp.admitu.services.seguridad;
import jakarta.annotation.Priority;import jakarta.json.*;import jakarta.ws.rs.*;import jakarta.ws.rs.container.*;import jakarta.ws.rs.core.*;import jakarta.ws.rs.ext.Provider;import java.io.*;import java.security.Principal;import java.util.*;import pe.edu.pucp.admitu.seguridad.SesionUsuario;import pe.edu.pucp.admitu.seguridad.bo.AutenticacionBOImpl;
@Provider @Priority(1000)
public class AutenticacionFiltro implements ContainerRequestFilter,ContainerResponseFilter {
 private static final Set<String> CATALOGOS=Set.of("CarreraRS","ConvocatoriaRS","OfertaCarreraRS","ConvocatoriaModalidadRS","FacultadRS","SedeRS","ModalidadRS","RequisitoRS","RequisitoConvocatoriaModalidadRS","PaisRS","InstitucionEducativaRS","EstadoPostulacionRS");
 private static final Set<String> PERSONALES=Set.of("PostulanteRS","AntecedenteAcademicoRS","PostulacionRS","PagoRS","DocumentoPostulacionRS","DocumentoObservacionRS","PostulacionHistorialRS","CarnePostulanteRS");
 private final AutenticacionBOImpl bo=new AutenticacionBOImpl();
 public void filter(ContainerRequestContext req)throws IOException {
  String path=req.getUriInfo().getPath(),recurso=path.split("/")[0];boolean get=req.getMethod().equals("GET");boolean publico=(get&&CATALOGOS.contains(recurso))||path.equals("AutenticacionRS/ingresar")||path.equals("AutenticacionRS/registrar");
  String h=req.getHeaderString("Authorization");SesionUsuario u=h!=null&&h.startsWith("Bearer ")?bo.autenticar(h.substring(7)):null;
  if(u==null){if(publico)return;throw new NotAuthorizedException("Inicia sesión","Bearer");}final SesionUsuario actual=u;final boolean secure=req.getSecurityContext().isSecure();req.setSecurityContext(new SecurityContext(){public Principal getUserPrincipal(){return actual;}public boolean isUserInRole(String role){return actual.getRol().equals(role);}public boolean isSecure(){return secure;}public String getAuthenticationScheme(){return "Bearer";}});
  if(publico||recurso.equals("AutenticacionRS"))return;
  if(recurso.equals("ReporteRS")){if(path.contains("Observados"))Seguridad.exigirRoles(req.getSecurityContext(),"admin","evaluador");else Seguridad.exigirRoles(req.getSecurityContext(),"admin");return;}
  if(recurso.equals("ArchivoDigitalRS"))return;
  if(u.getRol().equals("admin"))return;
  if(CATALOGOS.contains(recurso))throw new ForbiddenException("Solo administración modifica catálogos");
  if(u.getRol().equals("evaluador")){if(get&&(PERSONALES.contains(recurso)||recurso.equals("EvaluadorRS")))return;if(!get&&!req.getMethod().equals("DELETE")&&Set.of("DocumentoObservacionRS","DocumentoPostulacionRS","PagoRS","PostulacionRS","PostulacionHistorialRS").contains(recurso)&&!path.contains("asignarEvaluador"))return;throw new ForbiddenException("Operación reservada");}
  if(!PERSONALES.contains(recurso))throw new ForbiddenException("Tu perfil no autoriza este recurso");
  if(!get&&!recurso.equals("PostulanteRS")&&!recurso.equals("AntecedenteAcademicoRS")&&!path.equals("PostulacionRS/enviarPostulacion"))throw new ForbiddenException("La validación corresponde a evaluación");
  if(recurso.equals("PostulanteRS")&&!get&&!req.getMethod().equals("PUT"))throw new ForbiddenException("Solo puedes actualizar tu propio perfil");
  Set<Integer> ids=bo.idsAutorizados(recurso,u.getPersonaId());req.setProperty("admitu.ids",ids);
  String ultimo=path.substring(path.lastIndexOf('/')+1);if(ultimo.matches("[0-9]+")&&!ids.contains(Integer.parseInt(ultimo)))throw new ForbiddenException("El recurso pertenece a otro postulante");
  if(!get&&req.hasEntity()){
   byte[] bytes=req.getEntityStream().readNBytes(48*1024*1024+1);req.setEntityStream(new ByteArrayInputStream(bytes));if(bytes.length>48*1024*1024)throw new BadRequestException("Solicitud demasiado grande");
   JsonObject body;try(JsonReader reader=Json.createReader(new ByteArrayInputStream(bytes))){body=reader.readObject();}catch(JsonException e){throw new BadRequestException("JSON no válido");}
   JsonObject entidad=path.endsWith("enviarPostulacion")?body.getJsonObject("postulacion"):body;if(entidad==null)throw new BadRequestException("Postulación requerida");
   int id=entidad.getInt("id",0);if(id>0&&!ids.contains(id))throw new ForbiddenException("El recurso pertenece a otro postulante");
   if(recurso.equals("PostulanteRS")){if(!req.getMethod().equals("PUT")||id!=u.getPersonaId())throw new ForbiddenException("Usa el registro de cuenta");}
   else if(asociacion(entidad,"postulante")!=u.getPersonaId())throw new ForbiddenException("Postulante no autorizado");
  }
 }
 private int asociacion(JsonObject j,String k){JsonObject o=j.getJsonObject(k);return o==null?0:o.getInt("id",0);}
 public void filter(ContainerRequestContext req,ContainerResponseContext res){res.getHeaders().putSingle("Cache-Control","no-store");res.getHeaders().putSingle("X-Content-Type-Options","nosniff");if(!req.getMethod().equals("GET")||!(req.getProperty("admitu.ids") instanceof Set<?> ids)||res.getStatus()!=200)return;Object e=res.getEntity();if(e instanceof List<?> lista)res.setEntity(lista.stream().filter(o->permitido(o,ids)).toList());else if(e!=null&&!permitido(e,ids))throw new ForbiddenException();}
 private boolean permitido(Object o,Set<?> ids){try{return ids.contains(o.getClass().getMethod("getId").invoke(o));}catch(ReflectiveOperationException e){return false;}}
}
