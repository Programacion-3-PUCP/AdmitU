package pe.edu.pucp.admitu.reporte;

/**
 * Enumeración de plantillas JasperReports (.jrxml) y sus procedimientos almacenados.
 */
public enum TipoReporte {
    INGRESOS("ReporteIngresos", "LAB12_REPORTE_INGRESOS"),
    OBSERVADOS("ReporteObservados", "LAB12_REPORTE_OBSERVADOS"),
    VACANTES("ReporteVacantes", "LAB12_REPORTE_VACANTES");

    private final String plantilla;
    private final String procedimiento;

    TipoReporte(String plantilla, String procedimiento) {
        this.plantilla = plantilla;
        this.procedimiento = procedimiento;
    }

    public String getPlantilla() {
        return plantilla;
    }

    public String getProcedimiento() {
        return procedimiento;
    }
}
