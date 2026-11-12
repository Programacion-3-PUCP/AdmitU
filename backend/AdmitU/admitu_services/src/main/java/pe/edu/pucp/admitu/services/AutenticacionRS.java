package pe.edu.pucp.admitu.services;
import jakarta.ws.rs.*;import jakarta.ws.rs.core.*;import pe.edu.pucp.admitu.persona.Postulante;import pe.edu.pucp.admitu.seguridad.*;import pe.edu.pucp.admitu.seguridad.bo.AutenticacionBOImpl;import pe.edu.pucp.admitu.seguridad.boi.IAutenticacionBO;
@Path("AutenticacionRS") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class AutenticacionRS {
 private final IAutenticacionBO bo=new AutenticacionBOImpl();public static class Credenciales{public String correo,clave;}public static class Registro{public Postulante postulante;public String clave;}
 @POST @Path("ingresar") public SesionUsuario ingresar(Credenciales c){if(c==null)throw new BadRequestException();try{return bo.ingresar(c.correo,c.clave);}catch(SecurityException e){throw new NotAuthorizedException(e.getMessage(),"Bearer");}}
 @POST @Path("registrar") public int registrar(Registro r){if(r==null)throw new BadRequestException();return bo.registrar(r.postulante,r.clave);}
 @POST @Path("salir") public Response salir(@HeaderParam("Authorization")String auth){if(auth!=null&&auth.startsWith("Bearer "))bo.salir(auth.substring(7));return Response.noContent().build();}
}
