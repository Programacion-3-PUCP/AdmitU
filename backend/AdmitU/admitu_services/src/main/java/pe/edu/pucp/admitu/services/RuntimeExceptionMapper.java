package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import pe.edu.pucp.admitu.config.Diagnostico;

/**
 * Componente Jakarta REST (@Provider) que traduce excepciones de negocio y JDBC
 * en respuestas JSON controladas sin exponer sentencias SQL ni trazas internas.
 */
@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = Logger.getLogger(RuntimeExceptionMapper.class.getName());

    @Override
    public Response toResponse(RuntimeException error) {
        if (error instanceof WebApplicationException http) {
            return http.getResponse();
        }

        int status = 500;
        String mensaje = "No fue posible completar la operación.";

        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof IllegalArgumentException) {
                status = 400;
                mensaje = causa.getMessage();
                break;
            }
            if (causa instanceof SQLException sql) {
                if ("45000".equals(sql.getSQLState()) && sql.getMessage() != null && !sql.getMessage().isBlank()) {
                    status = 409;
                    mensaje = sql.getMessage();
                    break;
                }
                if (sql.getErrorCode() == 1062 || sql.getErrorCode() == 1451 || sql.getErrorCode() == 1452) {
                    status = 409;
                    mensaje = "La operación es incompatible con los registros o las reglas de admisión.";
                }
            }
        }

        if (status == 500 && error.getMessage() != null && error.getMessage().contains("Duplicate entry")) {
            status = 409;
            mensaje = "Ya existe un registro con esos datos.";
        }

        String referencia = UUID.randomUUID().toString();
        LOG.log(
                status == 500 ? Level.SEVERE : Level.WARNING,
                "Referencia {0}; {1}",
                new Object[]{referencia, Diagnostico.resumen(error)}
        );

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "mensaje", mensaje == null ? "Datos no válidos" : mensaje,
                        "referencia", referencia
                ))
                .build();
    }
}
