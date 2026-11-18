package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.seguridad.SesionUsuario;
import pe.edu.pucp.admitu.seguridad.bo.AutenticacionBOImpl;
import pe.edu.pucp.admitu.seguridad.boi.IAutenticacionBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("AutenticacionRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AutenticacionRS {

    private IAutenticacionBO boAutenticacion;

    @Context
    private SecurityContext seguridad;

    public AutenticacionRS() {
        boAutenticacion = new AutenticacionBOImpl();
    }

    public static class Credenciales {
        public String correo;
        public String clave;
    }

    public static class Registro {
        public Postulante postulante;
        public String clave;
    }

    public static class RegistroEvaluador {
        public Evaluador evaluador;
        public String clave;
    }

    @POST
    @Path("ingresar")
    public SesionUsuario ingresar(Credenciales credenciales) {
        if (credenciales == null) {
            throw new BadRequestException();
        }
        try {
            return boAutenticacion.ingresar(credenciales.correo, credenciales.clave);
        } catch (SecurityException ex) {
            throw new NotAuthorizedException(ex.getMessage(), "Bearer");
        }
    }

    @POST
    @Path("registrar")
    public int registrar(Registro registro) {
        if (registro == null) {
            throw new BadRequestException();
        }
        return boAutenticacion.registrar(registro.postulante, registro.clave);
    }

    @POST
    @Path("registrarEvaluador")
    public int registrarEvaluador(RegistroEvaluador registro) {
        Seguridad.exigirRoles(seguridad, "admin");
        if (registro == null) {
            throw new BadRequestException();
        }
        return boAutenticacion.registrarEvaluador(registro.evaluador, registro.clave);
    }

    @POST
    @Path("salir")
    public Response salir(@HeaderParam("Authorization") String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            boAutenticacion.salir(auth.substring(7));
        }
        return Response.noContent().build();
    }
}
