package pe.edu.pucp.admitu.services.seguridad;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Arrays;
import pe.edu.pucp.admitu.seguridad.SesionUsuario;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;

public final class Seguridad {
 private Seguridad(){}
 private static SesionUsuario usuario(SecurityContext ctx){
  if(ctx==null||!(ctx.getUserPrincipal() instanceof SesionUsuario u))throw new NotAuthorizedException("Inicia sesión","Bearer");
  return u;
 }
 public static int id(SecurityContext ctx){return usuario(ctx).getPersonaId();}
 public static String rol(SecurityContext ctx){return usuario(ctx).getRol();}
 public static String actor(SecurityContext ctx){var u=usuario(ctx);return u.getRol()+":"+u.getPersonaId()+" cuenta:"+u.getCuentaId();}
 public static void exigirRoles(SecurityContext ctx,String...roles){if(Arrays.stream(roles).noneMatch(rol(ctx)::equals))throw new ForbiddenException("Tu perfil no autoriza esta operación");}
 public static void exigirPropietario(SecurityContext ctx,int propietario){
  if(propietario<=0)throw new ForbiddenException("Propietario no válido");
  if(!rol(ctx).equals("admin")&&(!rol(ctx).equals("postulante")||id(ctx)!=propietario))throw new ForbiddenException("El recurso pertenece a otro postulante");
 }
 public static void exigirRevision(SecurityContext ctx,Postulacion postulacion){
  exigirRoles(ctx,"admin","evaluador");
  if(postulacion==null)throw new NotFoundException("Postulación no encontrada");
  if(rol(ctx).equals("evaluador")&&!Integer.valueOf(id(ctx)).equals(postulacion.getEvaluadorId()))throw new ForbiddenException("Solo puedes revisar expedientes asignados");
 }
 public static Postulacion exigirRevision(SecurityContext ctx,int postulacionId){
  var p=new PostulacionBOImpl().buscarPorId(postulacionId);exigirRevision(ctx,p);return p;
 }
 public static boolean tieneExpedienteAsignado(SecurityContext ctx,int postulanteId){
  return new PostulacionBOImpl().listarTodos().stream().anyMatch(p->p.getPostulante()!=null&&p.getPostulante().getId()==postulanteId&&Integer.valueOf(id(ctx)).equals(p.getEvaluadorId()));
 }
}
