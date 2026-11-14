package pe.edu.pucp.admitu.reporte.boi;
import pe.edu.pucp.admitu.reporte.*;
public interface IReporteBO {
 byte[] generar(TipoReporte tipo,FiltroReporte filtro);
 byte[] generarObservadosAsignados(FiltroReporte filtro,int evaluadorId);
}
