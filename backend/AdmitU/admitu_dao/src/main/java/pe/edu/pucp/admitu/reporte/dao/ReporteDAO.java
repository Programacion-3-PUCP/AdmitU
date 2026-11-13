package pe.edu.pucp.admitu.reporte.dao;
import java.util.*;
import pe.edu.pucp.admitu.reporte.*;
public interface ReporteDAO { List<Map<String,?>> consultar(TipoReporte tipo,FiltroReporte filtro); }
