package pe.edu.pucp.admitu.reporte.boi;

import pe.edu.pucp.admitu.reporte.FiltroReporte;
import pe.edu.pucp.admitu.reporte.TipoReporte;

/**
 * Interfaz de negocio para generación de reportes PDF con JasperReports.
 * Retorna byte[] con los bytes del documento PDF exportado por JasperExportManager,
 * siguiendo el mismo patrón del servicio ReporteRS del curso.
 */
public interface IReporteBO {

    byte[] generar(TipoReporte tipo, FiltroReporte filtro);

    byte[] generarObservadosAsignados(FiltroReporte filtro, int evaluadorId);
}
