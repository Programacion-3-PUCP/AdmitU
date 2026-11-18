package pe.edu.pucp.admitu.reporte.dao;

import java.util.List;
import java.util.Map;
import pe.edu.pucp.admitu.reporte.FiltroReporte;
import pe.edu.pucp.admitu.reporte.TipoReporte;

public interface ReporteDAO {

    List<Map<String, ?>> consultar(TipoReporte tipo, FiltroReporte filtro);
}
