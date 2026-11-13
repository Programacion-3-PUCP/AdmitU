package pe.edu.pucp.admitu.reporte;
public enum TipoReporte {
 INGRESOS("ReporteIngresos","LAB12_REPORTE_INGRESOS"), OBSERVADOS("ReporteObservados","LAB12_REPORTE_OBSERVADOS"), VACANTES("ReporteVacantes","LAB12_REPORTE_VACANTES");
 private final String plantilla,procedimiento;
 TipoReporte(String p,String s){plantilla=p;procedimiento=s;}
 public String getPlantilla(){return plantilla;} public String getProcedimiento(){return procedimiento;}
}
