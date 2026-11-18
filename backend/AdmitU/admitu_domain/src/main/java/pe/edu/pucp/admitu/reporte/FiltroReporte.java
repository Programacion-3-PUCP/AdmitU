package pe.edu.pucp.admitu.reporte;

import java.time.LocalDate;

/**
 * Parámetros de filtrado para los tres reportes PDF generados con JasperReports.
 */
public class FiltroReporte {

    private int convocatoriaId;
    private int modalidadId;
    private LocalDate desde;
    private LocalDate hasta;

    public FiltroReporte() {
    }

    public int getConvocatoriaId() {
        return convocatoriaId;
    }

    public void setConvocatoriaId(int convocatoriaId) {
        this.convocatoriaId = convocatoriaId;
    }

    public int getModalidadId() {
        return modalidadId;
    }

    public void setModalidadId(int modalidadId) {
        this.modalidadId = modalidadId;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }
}
