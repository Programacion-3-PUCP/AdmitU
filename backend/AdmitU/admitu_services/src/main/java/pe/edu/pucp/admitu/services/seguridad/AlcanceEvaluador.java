package pe.edu.pucp.admitu.services.seguridad;

import jakarta.ws.rs.ForbiddenException;
import java.util.*;
import java.util.stream.Collectors;
import pe.edu.pucp.admitu.postulacion.bo.*;
import pe.edu.pucp.admitu.persona.bo.AntecedenteAcademicoBOImpl;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;

/** Alcance de lectura calculado por petición a partir de la asignación actual. */
public final class AlcanceEvaluador {
    private AlcanceEvaluador() { }

    public static Set<Integer> idsAutorizados(String recurso, int evaluadorId) {
        if (recurso.equals("EvaluadorRS")) return Set.of(evaluadorId);
        var asignadas = new PostulacionBOImpl().listarTodos().stream()
            .filter(p -> Integer.valueOf(evaluadorId).equals(p.getEvaluadorId())).toList();
        Set<Integer> expedientes = asignadas.stream().map(p -> p.getId()).collect(Collectors.toSet());
        if (recurso.equals("PostulacionRS")) return expedientes;
        Set<Integer> propietarios = asignadas.stream().filter(p -> p.getPostulante() != null)
            .map(p -> p.getPostulante().getId()).collect(Collectors.toSet());
        if (recurso.equals("PostulanteRS")) return propietarios;
        if (expedientes.isEmpty()) return Set.of();
        return switch (recurso) {
            case "AntecedenteAcademicoRS" -> new AntecedenteAcademicoBOImpl().listarTodos().stream()
                .filter(a -> a.getPostulante() != null && propietarios.contains(a.getPostulante().getId()))
                .map(a -> a.getId()).collect(Collectors.toSet());
            case "PagoRS" -> new PagoBOImpl().listarTodos().stream()
                .filter(p -> p.getPostulacion() != null && expedientes.contains(p.getPostulacion().getId()))
                .map(p -> p.getId()).collect(Collectors.toSet());
            case "DocumentoPostulacionRS" -> documentos(expedientes);
            case "DocumentoObservacionRS" -> {
                var documentos = documentos(expedientes);
                yield new DocumentoObservacionBOImpl().listarTodos().stream()
                    .filter(o -> o.getDocumento() != null && documentos.contains(o.getDocumento().getId()))
                    .map(o -> o.getId()).collect(Collectors.toSet());
            }
            case "PostulacionHistorialRS" -> new PostulacionHistorialBOImpl().listarTodos().stream()
                .filter(h -> h.getPostulacion() != null && expedientes.contains(h.getPostulacion().getId()))
                .map(h -> h.getId()).collect(Collectors.toSet());
            case "CarnePostulanteRS" -> new CarnePostulanteBOImpl().listarTodos().stream()
                .filter(c -> c.getPostulacion() != null && expedientes.contains(c.getPostulacion().getId()))
                .map(c -> c.getId()).collect(Collectors.toSet());
            default -> throw new ForbiddenException("Recurso fuera del ámbito de evaluación");
        };
    }

    private static Set<Integer> documentos(Set<Integer> expedientes) {
        return new DocumentoPostulacionBOImpl().listarTodos().stream()
            .filter(d -> d.getPostulacion() != null && expedientes.contains(d.getPostulacion().getId()))
            .map(d -> d.getId()).collect(Collectors.toSet());
    }
}
