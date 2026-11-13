package pe.edu.pucp.admitu.services.seguridad;
import jakarta.ws.rs.core.*;import jakarta.ws.rs.ext.*;import java.util.Map;
@Provider public class ValidacionMapper implements ExceptionMapper<IllegalArgumentException>{public Response toResponse(IllegalArgumentException e){return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE).entity(Map.of("mensaje",e.getMessage()==null?"Datos no válidos":e.getMessage())).build();}}
