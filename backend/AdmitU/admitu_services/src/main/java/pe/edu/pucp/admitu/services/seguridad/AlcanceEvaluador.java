package pe.edu.pucp.admitu.services.seguridad;

import jakarta.ws.rs.ForbiddenException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;
import pe.edu.pucp.admitu.persona.bo.AntecedenteAcademicoBOImpl;
import pe.edu.pucp.admitu.postulacion.CarnePostulante;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import pe.edu.pucp.admitu.postulacion.bo.CarnePostulanteBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoObservacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.DocumentoPostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionHistorialBOImpl;

public class AlcanceEvaluador {

    private AlcanceEvaluador() {
    }

    public static Set<Integer> idsAutorizados(String recurso, int evaluadorId) {
        Set<Integer> resultado = new HashSet<>();
        if ("EvaluadorRS".equals(recurso)) {
            resultado.add(evaluadorId);
            return resultado;
        }

        List<Postulacion> asignadas = new ArrayList<>();
        Set<Integer> expedientes = new HashSet<>();
        Set<Integer> propietarios = new HashSet<>();
        for (Postulacion p : new PostulacionBOImpl().listarTodos()) {
            if (Integer.valueOf(evaluadorId).equals(p.getEvaluadorId())) {
                asignadas.add(p);
                expedientes.add(p.getId());
                if (p.getPostulante() != null) {
                    propietarios.add(p.getPostulante().getId());
                }
            }
        }

        if ("PostulacionRS".equals(recurso)) {
            return expedientes;
        }
        if ("PostulanteRS".equals(recurso)) {
            return propietarios;
        }
        if (expedientes.isEmpty()) {
            return resultado;
        }

        if ("AntecedenteAcademicoRS".equals(recurso)) {
            for (AntecedenteAcademico a : new AntecedenteAcademicoBOImpl().listarTodos()) {
                if (a.getPostulante() != null && propietarios.contains(a.getPostulante().getId())) {
                    resultado.add(a.getId());
                }
            }
            return resultado;
        }
        if ("PagoRS".equals(recurso)) {
            for (Pago p : new PagoBOImpl().listarTodos()) {
                if (p.getPostulacion() != null && expedientes.contains(p.getPostulacion().getId())) {
                    resultado.add(p.getId());
                }
            }
            return resultado;
        }
        if ("DocumentoPostulacionRS".equals(recurso)) {
            return documentos(expedientes);
        }
        if ("DocumentoObservacionRS".equals(recurso)) {
            Set<Integer> docs = documentos(expedientes);
            for (DocumentoObservacion o : new DocumentoObservacionBOImpl().listarTodos()) {
                if (o.getDocumento() != null && docs.contains(o.getDocumento().getId())) {
                    resultado.add(o.getId());
                }
            }
            return resultado;
        }
        if ("PostulacionHistorialRS".equals(recurso)) {
            for (PostulacionHistorial h : new PostulacionHistorialBOImpl().listarTodos()) {
                if (h.getPostulacion() != null && expedientes.contains(h.getPostulacion().getId())) {
                    resultado.add(h.getId());
                }
            }
            return resultado;
        }
        if ("CarnePostulanteRS".equals(recurso)) {
            for (CarnePostulante c : new CarnePostulanteBOImpl().listarTodos()) {
                if (c.getPostulacion() != null && expedientes.contains(c.getPostulacion().getId())) {
                    resultado.add(c.getId());
                }
            }
            return resultado;
        }
        throw new ForbiddenException("Recurso fuera del ámbito de evaluación");
    }

    private static Set<Integer> documentos(Set<Integer> expedientes) {
        Set<Integer> docs = new HashSet<>();
        for (DocumentoPostulacion d : new DocumentoPostulacionBOImpl().listarTodos()) {
            if (d.getPostulacion() != null && expedientes.contains(d.getPostulacion().getId())) {
                docs.add(d.getId());
            }
        }
        return docs;
    }
}
