package pe.edu.pucp.admitu.postulacion.bo;

import java.util.*;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.boi.*;

public final class ResumenExpedienteBOImpl implements IResumenExpedienteBO {
    private final IPostulacionBO postulaciones = new PostulacionBOImpl();
    private final IDocumentoPostulacionBO documentos = new DocumentoPostulacionBOImpl();
    private final IDocumentoObservacionBO observaciones = new DocumentoObservacionBOImpl();

    public List<ExpedienteResumen> listarGlobales() {
        Map<Integer, DocumentoPostulacion> porDocumento = new HashMap<>();
        documentos.listarTodos().forEach(d -> porDocumento.put(d.getId(), d));
        Map<Integer, List<ObservacionResumen>> porPostulacion = new HashMap<>();
        for (var o : observaciones.listarTodos()) {
            var d = o.getDocumento() == null ? null : porDocumento.get(o.getDocumento().getId());
            if (d != null && d.getPostulacion() != null && d.getRequisitoAplicable() != null && o.getEvaluador() != null)
                porPostulacion.computeIfAbsent(d.getPostulacion().getId(), x -> new ArrayList<>()).add(new ObservacionResumen(o, d));
        }
        return postulaciones.listarTodos().stream()
            .map(p -> new ExpedienteResumen(p, porPostulacion.getOrDefault(p.getId(), List.of())))
            .toList();
    }
}
