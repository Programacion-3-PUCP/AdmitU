package pe.edu.pucp.admitu.services.seguridad;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

/**
 * Componente Jakarta REST (@Provider) que formatea errores HTTP de autorización
 * (401/403/404) como JSON uniforme.
 */
@Provider
public class SeguridadMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        String mensaje;
        if (status == 401) {
            mensaje = "Inicia sesión con credenciales válidas";
        } else if (status == 403) {
            mensaje = "Tu perfil no autoriza esta operación";
        } else {
            mensaje = "No fue posible completar la solicitud";
        }
        return Response.fromResponse(exception.getResponse())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }
}
