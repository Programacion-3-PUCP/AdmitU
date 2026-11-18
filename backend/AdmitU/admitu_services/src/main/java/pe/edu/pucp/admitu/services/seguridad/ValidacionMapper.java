package pe.edu.pucp.admitu.services.seguridad;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

/**
 * Componente Jakarta REST (@Provider) que traduce errores de validación
 * (IllegalArgumentException) en respuestas HTTP 400 con mensaje claro.
 */
@Provider
public class ValidacionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        String mensaje = exception.getMessage() == null ? "Datos no válidos" : exception.getMessage();
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }
}
