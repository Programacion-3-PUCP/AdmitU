package pe.edu.pucp.admitu.reporte;
import java.time.LocalDate;
public class FiltroReporte {
 private int convocatoriaId,modalidadId; private LocalDate desde,hasta;
 public int getConvocatoriaId(){return convocatoriaId;} public void setConvocatoriaId(int x){convocatoriaId=x;}
 public int getModalidadId(){return modalidadId;} public void setModalidadId(int x){modalidadId=x;}
 public LocalDate getDesde(){return desde;} public void setDesde(LocalDate x){desde=x;}
 public LocalDate getHasta(){return hasta;} public void setHasta(LocalDate x){hasta=x;}
}
