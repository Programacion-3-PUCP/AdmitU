package pe.edu.pucp.admitu.reporte.bo;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.bo.ConvocatoriaBOImpl;
import pe.edu.pucp.admitu.configuracion.bo.ModalidadBOImpl;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaBO;
import pe.edu.pucp.admitu.configuracion.boi.IModalidadBO;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.bo.PostulacionBOImpl;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.reporte.FiltroReporte;
import pe.edu.pucp.admitu.reporte.TipoReporte;
import pe.edu.pucp.admitu.reporte.boi.IReporteBO;
import pe.edu.pucp.admitu.reporte.dao.ReporteDAO;
import pe.edu.pucp.admitu.reporte.impl.ReporteImpl;

public class ReporteBOImpl implements IReporteBO {

    private static final Map<TipoReporte, JasperReport> PLANTILLAS = new EnumMap<>(TipoReporte.class);

    private ReporteDAO daoReporte;
    private IConvocatoriaBO boConvocatoria;
    private IModalidadBO boModalidad;
    private IPostulacionBO boPostulacion;

    public ReporteBOImpl() {
        daoReporte = new ReporteImpl();
        boConvocatoria = new ConvocatoriaBOImpl();
        boModalidad = new ModalidadBOImpl();
        boPostulacion = new PostulacionBOImpl();
    }

    public ReporteBOImpl(ReporteDAO daoReporte) {
        this.daoReporte = daoReporte;
        this.boConvocatoria = new ConvocatoriaBOImpl();
        this.boModalidad = new ModalidadBOImpl();
        this.boPostulacion = new PostulacionBOImpl();
    }

    @Override
    public byte[] generar(TipoReporte tipo, FiltroReporte filtro) {
        return generar(tipo, filtro, null);
    }

    @Override
    public byte[] generarObservadosAsignados(FiltroReporte filtro, int evaluadorId) {
        if (evaluadorId <= 0) {
            throw new IllegalArgumentException("Evaluador requerido");
        }
        Set<Integer> expedientes = new HashSet<>();
        for (Postulacion p : boPostulacion.listarTodos()) {
            if (Integer.valueOf(evaluadorId).equals(p.getEvaluadorId())) {
                expedientes.add(p.getId());
            }
        }
        return generar(TipoReporte.OBSERVADOS, filtro, expedientes);
    }

    private String etiquetaConvocatoria(int convocatoriaId, List<Map<String, ?>> filas) {
        if (convocatoriaId == 0) {
            return "todas";
        }
        for (Map<String, ?> fila : filas) {
            Object val = fila.get("convocatoria");
            if (val instanceof String && !((String) val).trim().isEmpty()) {
                return (String) val;
            }
        }
        try {
            Convocatoria conv = boConvocatoria.buscarPorId(convocatoriaId);
            if (conv != null && conv.getNombre() != null && !conv.getNombre().trim().isEmpty()) {
                return conv.getNombre();
            }
        } catch (Exception ignored) {
        }
        return "ID " + convocatoriaId;
    }

    private String etiquetaModalidad(int modalidadId, List<Map<String, ?>> filas) {
        if (modalidadId == 0) {
            return "todas";
        }
        for (Map<String, ?> fila : filas) {
            Object val = fila.get("modalidad");
            if (val instanceof String && !((String) val).trim().isEmpty()) {
                return (String) val;
            }
        }
        try {
            Modalidad mod = boModalidad.buscarPorId(modalidadId);
            if (mod != null && mod.getNombre() != null && !mod.getNombre().trim().isEmpty()) {
                return mod.getNombre();
            }
        } catch (Exception ignored) {
        }
        return "ID " + modalidadId;
    }

    private String textoFila(Map<String, ?> fila, String clave, String defecto) {
        Object valor = fila.get(clave);
        return valor == null ? defecto : String.valueOf(valor);
    }

    private Map<String, Object> puntoGrafico(String serie, String etiqueta, Number valor) {
        Map<String, Object> item = new HashMap<>();
        item.put("serie", serie);
        item.put("etiqueta", etiqueta == null || etiqueta.trim().isEmpty() ? "Sin clasificar" : etiqueta);
        item.put("valor", valor);
        return item;
    }

    private void poblarSeriesGraficas(TipoReporte tipo, List<Map<String, ?>> filas, Map<String, Object> params) {
        List<Map<String, Object>> serie1 = new ArrayList<>();
        List<Map<String, Object>> serie2 = new ArrayList<>();

        if (tipo == TipoReporte.INGRESOS) {
            Map<String, BigDecimal> porModalidad = new LinkedHashMap<>();
            for (Map<String, ?> fila : filas) {
                String mod = textoFila(fila, "modalidad", "Modalidad");
                Object totalObj = fila.get("total");
                BigDecimal monto = totalObj instanceof BigDecimal ? (BigDecimal) totalObj : BigDecimal.ZERO;
                BigDecimal acumulado = porModalidad.getOrDefault(mod, BigDecimal.ZERO).add(monto);
                porModalidad.put(mod, acumulado);
            }
            for (Map.Entry<String, BigDecimal> entry : porModalidad.entrySet()) {
                serie1.add(puntoGrafico("Monto aprobado (S/)", entry.getKey(), entry.getValue()));
                if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    serie2.add(puntoGrafico("Ingresos", entry.getKey(), entry.getValue()));
                }
            }
        } else if (tipo == TipoReporte.OBSERVADOS) {
            Map<String, Long> porModalidad = new LinkedHashMap<>();
            Map<String, Long> porTipo = new LinkedHashMap<>();
            for (Map<String, ?> fila : filas) {
                String mod = textoFila(fila, "modalidad", "Modalidad");
                String tip = textoFila(fila, "tipo", "OTRO");
                porModalidad.put(mod, porModalidad.getOrDefault(mod, 0L) + 1L);
                porTipo.put(tip, porTipo.getOrDefault(tip, 0L) + 1L);
            }
            for (Map.Entry<String, Long> entry : porModalidad.entrySet()) {
                serie1.add(puntoGrafico("Pendientes", entry.getKey(), entry.getValue()));
            }
            for (Map.Entry<String, Long> entry : porTipo.entrySet()) {
                serie2.add(puntoGrafico("Por tipo", entry.getKey(), entry.getValue()));
            }
        } else if (tipo == TipoReporte.VACANTES) {
            Set<Long> convocatorias = new HashSet<>();
            for (Map<String, ?> fila : filas) {
                Object convObj = fila.get("convocatoria_id");
                if (convObj instanceof Number) {
                    convocatorias.add(((Number) convObj).longValue());
                }
            }
            boolean multiConvocatoria = convocatorias.size() > 1;
            Map<String, Long> cuposCarrera = new LinkedHashMap<>();
            Map<String, Long> completadasCarrera = new LinkedHashMap<>();
            Map<String, String> etiquetaCarrera = new LinkedHashMap<>();
            Map<String, Long> completadasModalidad = new LinkedHashMap<>();

            for (Map<String, ?> fila : filas) {
                Object convObj = fila.get("convocatoria_id");
                Object carObj = fila.get("carrera_id");
                long convId = convObj instanceof Number ? ((Number) convObj).longValue() : 0L;
                long carId = carObj instanceof Number ? ((Number) carObj).longValue() : 0L;
                String clave = convId + ":" + carId;
                String carrera = textoFila(fila, "carrera", "Carrera");
                String conv = textoFila(fila, "convocatoria", "");
                String label = multiConvocatoria && !conv.trim().isEmpty() ? conv + " · " + carrera : carrera;

                Object cupoObj = fila.get("cupo_carrera");
                Object compObj = fila.get("completadas");
                long cupo = cupoObj instanceof Number ? ((Number) cupoObj).longValue() : 0L;
                long comp = compObj instanceof Number ? ((Number) compObj).longValue() : 0L;
                if (!etiquetaCarrera.containsKey(clave)) {
                    etiquetaCarrera.put(clave, label);
                }
                if (!cuposCarrera.containsKey(clave)) {
                    cuposCarrera.put(clave, cupo);
                }
                completadasCarrera.put(clave, completadasCarrera.getOrDefault(clave, 0L) + comp);

                String mod = textoFila(fila, "modalidad", "Modalidad");
                completadasModalidad.put(mod, completadasModalidad.getOrDefault(mod, 0L) + comp);
            }

            for (String clave : cuposCarrera.keySet()) {
                String label = etiquetaCarrera.get(clave);
                serie1.add(puntoGrafico("Cupo carrera", label, cuposCarrera.get(clave)));
                serie1.add(puntoGrafico("Completadas", label, completadasCarrera.getOrDefault(clave, 0L)));
            }
            for (Map.Entry<String, Long> entry : completadasModalidad.entrySet()) {
                serie2.add(puntoGrafico("Completadas", entry.getKey(), entry.getValue()));
            }
        }

        params.put("CHART_SERIE_1", serie1);
        params.put("CHART_SERIE_2", serie2);
    }

    private byte[] generar(TipoReporte tipo, FiltroReporte filtro, Set<Integer> expedientesAutorizados) {
        if (tipo == null || filtro == null) {
            throw new IllegalArgumentException("Indique reporte y filtros");
        }
        if (filtro.getConvocatoriaId() < 0 || filtro.getModalidadId() < 0) {
            throw new IllegalArgumentException("Los filtros no pueden ser negativos");
        }
        if (filtro.getDesde() != null && filtro.getHasta() != null && filtro.getDesde().isAfter(filtro.getHasta())) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        }

        try {
            List<Map<String, ?>> filas = daoReporte.consultar(tipo, filtro);
            if (expedientesAutorizados != null) {
                List<Map<String, ?>> filtradas = new ArrayList<>();
                for (Map<String, ?> fila : filas) {
                    Object postIdObj = fila.get("postulacion_id");
                    if (!(postIdObj instanceof Number)) {
                        throw new IllegalStateException("El reporte requiere la migración de identidad de expedientes");
                    }
                    if (expedientesAutorizados.contains(((Number) postIdObj).intValue())) {
                        filtradas.add(fila);
                    }
                }
                filas = filtradas;
            }

            String nombreConv = etiquetaConvocatoria(filtro.getConvocatoriaId(), filas);
            String nombreMod = etiquetaModalidad(filtro.getModalidadId(), filas);
            String textoFiltros = "Convocatoria: " + nombreConv
                    + " | Modalidad: " + nombreMod
                    + (filtro.getDesde() == null ? "" : " | Desde: " + filtro.getDesde())
                    + (filtro.getHasta() == null ? "" : " | Hasta: " + filtro.getHasta())
                    + (expedientesAutorizados == null ? "" : " | Expedientes asignados");

            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("FILTROS", textoFiltros);
            parametros.put(JRParameter.REPORT_LOCALE, Locale.forLanguageTag("es-PE"));
            parametros.put(JRParameter.REPORT_TIME_ZONE, TimeZone.getTimeZone("America/Lima"));
            poblarSeriesGraficas(tipo, filas, parametros);

            JasperPrint jp = JasperFillManager.fillReport(
                    plantilla(tipo),
                    parametros,
                    new JRMapCollectionDataSource(filas)
            );
            return JasperExportManager.exportReportToPdf(jp);
        } catch (Exception ex) {
            System.out.println("ERROR AL GENERAR REPORTE PDF: " + ex.getMessage());
            throw new RuntimeException("No fue posible generar el PDF del reporte: " + ex.getMessage(), ex);
        }
    }

    private static synchronized JasperReport plantilla(TipoReporte tipo) throws Exception {
        JasperReport reporte = PLANTILLAS.get(tipo);
        if (reporte == null) {
            String ruta = "/reportes/" + tipo.getPlantilla() + ".jrxml";
            try (InputStream in = ReporteBOImpl.class.getResourceAsStream(ruta)) {
                if (in == null) {
                    throw new IllegalStateException("Diseño Jasper no encontrado: " + ruta);
                }
                reporte = JasperCompileManager.compileReport(in);
                PLANTILLAS.put(tipo, reporte);
            }
        }
        return reporte;
    }
}
