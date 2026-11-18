package pe.edu.pucp.admitu.postulacion.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.admitu.postulacion.DocumentoObservacion;
import pe.edu.pucp.admitu.postulacion.DocumentoPostulacion;
import pe.edu.pucp.admitu.postulacion.ExpedienteResumen;
import pe.edu.pucp.admitu.postulacion.ObservacionResumen;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoObservacionBO;
import pe.edu.pucp.admitu.postulacion.boi.IDocumentoPostulacionBO;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.postulacion.boi.IResumenExpedienteBO;

public class ResumenExpedienteBOImpl implements IResumenExpedienteBO {

    private IPostulacionBO boPostulacion;
    private IDocumentoPostulacionBO boDocumento;
    private IDocumentoObservacionBO boObservacion;

    public ResumenExpedienteBOImpl() {
        boPostulacion = new PostulacionBOImpl();
        boDocumento = new DocumentoPostulacionBOImpl();
        boObservacion = new DocumentoObservacionBOImpl();
    }

    @Override
    public List<ExpedienteResumen> listarGlobales() {
        Map<Integer, DocumentoPostulacion> porDocumento = new HashMap<>();
        for (DocumentoPostulacion d : boDocumento.listarTodos()) {
            porDocumento.put(d.getId(), d);
        }

        Map<Integer, List<ObservacionResumen>> porPostulacion = new HashMap<>();
        for (DocumentoObservacion o : boObservacion.listarTodos()) {
            DocumentoPostulacion d = o.getDocumento() == null ? null : porDocumento.get(o.getDocumento().getId());
            if (d != null && d.getPostulacion() != null && d.getRequisitoAplicable() != null && o.getEvaluador() != null) {
                int postId = d.getPostulacion().getId();
                List<ObservacionResumen> lista = porPostulacion.get(postId);
                if (lista == null) {
                    lista = new ArrayList<>();
                    porPostulacion.put(postId, lista);
                }
                lista.add(new ObservacionResumen(o, d));
            }
        }

        List<ExpedienteResumen> resultado = new ArrayList<>();
        for (Postulacion p : boPostulacion.listarTodos()) {
            List<ObservacionResumen> obs = porPostulacion.get(p.getId());
            if (obs == null) {
                obs = new ArrayList<>();
            }
            resultado.add(new ExpedienteResumen(p, obs));
        }
        return resultado;
    }
}
