package pe.edu.pucp.admitu.services;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;
@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger LOG=Logger.getLogger(RuntimeExceptionMapper.class.getName());
    public Response toResponse(RuntimeException error) {
        if(error instanceof WebApplicationException http) return http.getResponse();
        int status=500; String mensaje="No fue posible completar la operación.";
        for(Throwable e=error;e!=null;e=e.getCause()) {
            if(e instanceof IllegalArgumentException){status=400;mensaje=e.getMessage();break;}
            if(e instanceof SQLException sql && (sql.getErrorCode()==1062 || sql.getErrorCode()==1451 || sql.getErrorCode()==1452 || "45000".equals(sql.getSQLState()))) {status=409;mensaje="La operación es incompatible con los registros o las reglas de admisión.";}
        }
        if(status==500 && error.getMessage()!=null && error.getMessage().contains("Duplicate entry")){status=409;mensaje="Ya existe un registro con esos datos.";}
        String referencia=UUID.randomUUID().toString();
        LOG.log(status==500?Level.SEVERE:Level.WARNING,"Referencia {0}; {1}",new Object[]{referencia,pe.edu.pucp.admitu.config.Diagnostico.resumen(error)});
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(Map.of("mensaje",mensaje==null?"Datos no válidos":mensaje,"referencia",referencia)).build();
    }
}
