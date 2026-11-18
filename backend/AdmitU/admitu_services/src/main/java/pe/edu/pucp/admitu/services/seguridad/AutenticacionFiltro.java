package pe.edu.pucp.admitu.services.seguridad;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import pe.edu.pucp.admitu.config.ActorContext;
import pe.edu.pucp.admitu.seguridad.SesionUsuario;
import pe.edu.pucp.admitu.seguridad.bo.AutenticacionBOImpl;

/**
 * Filtro Jakarta REST (@Provider) que autentica el token Bearer, establece el
 * SecurityContext y ActorContext por petición, y aplica autorización por rol y propietario.
 */
@Provider
@Priority(1000)
public class AutenticacionFiltro implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Set<String> CATALOGOS = Set.of(
            "CarreraRS", "ConvocatoriaRS", "OfertaCarreraRS", "ConvocatoriaModalidadRS",
            "FacultadRS", "SedeRS", "ModalidadRS", "RequisitoRS",
            "RequisitoConvocatoriaModalidadRS", "PaisRS", "InstitucionEducativaRS", "EstadoPostulacionRS"
    );

    private static final Set<String> PERSONALES = Set.of(
            "PostulanteRS", "AntecedenteAcademicoRS", "PostulacionRS", "PagoRS",
            "DocumentoPostulacionRS", "DocumentoObservacionRS", "PostulacionHistorialRS", "CarnePostulanteRS"
    );

    private final AutenticacionBOImpl bo = new AutenticacionBOImpl();

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        ActorContext.clear();

        String path = req.getUriInfo().getPath();
        String recurso = path.split("/")[0];
        boolean get = req.getMethod().equals("GET");
        boolean publico = (get && CATALOGOS.contains(recurso))
                || path.equals("AutenticacionRS/ingresar")
                || path.equals("AutenticacionRS/registrar");

        String authHeader = req.getHeaderString("Authorization");
        SesionUsuario usuario = authHeader != null && authHeader.startsWith("Bearer ")
                ? bo.autenticar(authHeader.substring(7))
                : null;

        if (usuario == null) {
            if (publico) {
                return;
            }
            throw new NotAuthorizedException("Inicia sesión", "Bearer");
        }

        ActorContext.set(usuario.getCuentaId(), usuario.getPersonaId(), usuario.getRol());
        final SesionUsuario actual = usuario;
        final boolean secure = req.getSecurityContext().isSecure();

        req.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return actual;
            }

            @Override
            public boolean isUserInRole(String role) {
                return actual.getRol().equals(role);
            }

            @Override
            public boolean isSecure() {
                return secure;
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });

        if (publico || recurso.equals("AutenticacionRS")) {
            return;
        }

        // Ningún cliente, ni siquiera administración, escribe el historial de auditoría.
        if (recurso.equals("PostulacionHistorialRS") && !get) {
            throw new ForbiddenException("El historial se genera exclusivamente en transiciones del servidor");
        }
        if (recurso.equals("ReporteRS")) {
            if (path.contains("Observados")) {
                Seguridad.exigirRoles(req.getSecurityContext(), "admin", "evaluador");
            } else {
                Seguridad.exigirRoles(req.getSecurityContext(), "admin");
            }
            return;
        }
        if (recurso.equals("ArchivoDigitalRS")) {
            return;
        }
        if (get && path.equals("PostulacionRS/listarResumenesGlobales")) {
            Seguridad.exigirRoles(req.getSecurityContext(), "admin", "evaluador");
            return;
        }
        if (usuario.getRol().equals("admin")) {
            return;
        }
        if (CATALOGOS.contains(recurso)) {
            throw new ForbiddenException("Solo administración modifica catálogos");
        }

        if (usuario.getRol().equals("evaluador")) {
            // La vista «Todos» tiene un DTO aparte. Las entidades completas requieren asignación.
            if (get && (PERSONALES.contains(recurso) || recurso.equals("EvaluadorRS"))) {
                Set<Integer> ids = AlcanceEvaluador.idsAutorizados(recurso, usuario.getPersonaId());
                req.setProperty("admitu.ids", ids);
                String ultimo = path.substring(path.lastIndexOf('/') + 1);
                if (ultimo.matches("[0-9]+") && !ids.contains(Integer.parseInt(ultimo))) {
                    throw new ForbiddenException("El recurso no pertenece a un expediente asignado");
                }
                return;
            }
            if (operacionEvaluador(req.getMethod(), path)) {
                return;
            }
            throw new ForbiddenException("Operación reservada");
        }

        if (!PERSONALES.contains(recurso)) {
            throw new ForbiddenException("Tu perfil no autoriza este recurso");
        }
        if (!get
                && !recurso.equals("PostulanteRS")
                && !recurso.equals("AntecedenteAcademicoRS")
                && !path.equals("PostulacionRS/enviarPostulacion")) {
            throw new ForbiddenException("La validación corresponde a evaluación");
        }
        if (recurso.equals("PostulanteRS") && !get && !req.getMethod().equals("PUT")) {
            throw new ForbiddenException("Solo puedes actualizar tu propio perfil");
        }

        Set<Integer> ids = bo.idsAutorizados(recurso, usuario.getPersonaId());
        req.setProperty("admitu.ids", ids);

        String ultimo = path.substring(path.lastIndexOf('/') + 1);
        if (ultimo.matches("[0-9]+") && !ids.contains(Integer.parseInt(ultimo))) {
            throw new ForbiddenException("El recurso pertenece a otro postulante");
        }

        if (!get && req.hasEntity()) {
            int maxPayload = 48 * 1024 * 1024;
            byte[] bytes = req.getEntityStream().readNBytes(maxPayload + 1);
            req.setEntityStream(new ByteArrayInputStream(bytes));
            if (bytes.length > maxPayload) {
                throw new BadRequestException("Solicitud demasiado grande");
            }

            JsonObject body;
            try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
                body = reader.readObject();
            } catch (JsonException e) {
                throw new BadRequestException("JSON no válido");
            }

            JsonObject entidad = path.endsWith("enviarPostulacion") ? body.getJsonObject("postulacion") : body;
            if (entidad == null) {
                throw new BadRequestException("Postulación requerida");
            }

            int id = entidad.getInt("id", 0);
            if (id > 0 && !ids.contains(id)) {
                throw new ForbiddenException("El recurso pertenece a otro postulante");
            }

            if (recurso.equals("PostulanteRS")) {
                if (!req.getMethod().equals("PUT") || id != usuario.getPersonaId()) {
                    throw new ForbiddenException("Usa el registro de cuenta");
                }
            } else if (asociacion(entidad, "postulante") != usuario.getPersonaId()) {
                throw new ForbiddenException("Postulante no autorizado");
            }
        }
    }

    public static boolean operacionEvaluador(String metodo, String ruta) {
        Set<String> rutasPut = Set.of(
                "PostulacionRS/modificarPostulacion",
                "PagoRS/modificarPago",
                "DocumentoPostulacionRS/modificarDocumentoPostulacion",
                "DocumentoObservacionRS/modificarDocumentoObservacion"
        );
        return (metodo.equals("PUT") && rutasPut.contains(ruta))
                || (metodo.equals("POST") && ruta.equals("DocumentoObservacionRS/insertarDocumentoObservacion"));
    }

    private int asociacion(JsonObject json, String clave) {
        JsonObject objeto = json.getJsonObject(clave);
        return objeto == null ? 0 : objeto.getInt("id", 0);
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        ActorContext.clear();
        res.getHeaders().putSingle("Cache-Control", "no-store");
        res.getHeaders().putSingle("X-Content-Type-Options", "nosniff");

        if (!req.getMethod().equals("GET")
                || !(req.getProperty("admitu.ids") instanceof Set<?> ids)
                || res.getStatus() != 200) {
            return;
        }

        Object entity = res.getEntity();
        if (entity instanceof List<?> lista) {
            res.setEntity(lista.stream().filter(o -> permitido(o, ids)).toList());
        } else if (entity != null && !permitido(entity, ids)) {
            throw new ForbiddenException();
        }
    }

    private boolean permitido(Object objeto, Set<?> ids) {
        try {
            return ids.contains(objeto.getClass().getMethod("getId").invoke(objeto));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
