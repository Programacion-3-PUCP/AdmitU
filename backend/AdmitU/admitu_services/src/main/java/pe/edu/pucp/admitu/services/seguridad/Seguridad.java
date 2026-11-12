package pe.edu.pucp.admitu.services.seguridad;
import jakarta.ws.rs.*;import jakarta.ws.rs.core.SecurityContext;import java.util.Arrays;import pe.edu.pucp.admitu.seguridad.SesionUsuario;
public final class Seguridad {
 private Seguridad(){}private static SesionUsuario usuario(SecurityContext ctx){if(ctx==null||!(ctx.getUserPrincipal() instanceof SesionUsuario u))throw new NotAuthorizedException("Inicia sesión","Bearer");return u;}
 public static int id(SecurityContext ctx){return usuario(ctx).getPersonaId();}public static String rol(SecurityContext ctx){return usuario(ctx).getRol();}
 public static void exigirRoles(SecurityContext ctx,String...roles){if(Arrays.stream(roles).noneMatch(rol(ctx)::equals))throw new ForbiddenException("Tu perfil no autoriza esta operación");}
 public static void exigirPropietario(SecurityContext ctx,int id){if(id<=0)throw new ForbiddenException("Propietario no válido");if(!rol(ctx).equals("admin")&&!rol(ctx).equals("evaluador")&&id(ctx)!=id)throw new ForbiddenException("El recurso pertenece a otro postulante");}
}
