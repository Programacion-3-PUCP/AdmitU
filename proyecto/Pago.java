import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Pago {
    private static int siguienteId = 1;
    private final int id;
    private Postulacion postulacion;
    private MedioPago medioPago;
    private int numeroIntento;
    private String codigoPago;
    private String referenciaExterna;
    private LocalDate fechaGeneracion;
    private LocalDate fechaPago;
    private double montoOriginal;
    private double porcentajeDescuento;
    private double montoPagado;
    private EstadoPago estadoPago;
    private LocalDate fechaValidacion;
    private String validadoPor;
    private String observacionValidacion;

    public Pago(Postulacion postulacion, MedioPago medioPago, int numeroIntento
            , String codigoPago, String referenciaExterna, LocalDate fechaGeneracion
            , LocalDate fechaPago, double montoOriginal, double porcentajeDescuento
            , double montoPagado, EstadoPago estadoPago, LocalDate fechaValidacion
            , String validadoPor, String observacionValidacion) {
        this.id = siguienteId++;
        this.postulacion = postulacion;
        this.medioPago = medioPago;
        this.numeroIntento = numeroIntento;
        this.codigoPago = codigoPago;
        this.referenciaExterna = referenciaExterna;
        this.fechaGeneracion = fechaGeneracion;
        this.fechaPago = fechaPago;
        this.montoOriginal = montoOriginal;
        this.porcentajeDescuento = porcentajeDescuento;
        this.montoPagado = montoPagado;
        this.estadoPago = estadoPago;
        this.fechaValidacion = fechaValidacion;
        this.validadoPor = validadoPor;
        this.observacionValidacion = observacionValidacion;
    }


    public int getId() {
        return id;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(MedioPago medioPago) {
        this.medioPago = medioPago;
    }

    public int getNumeroIntento() {
        return numeroIntento;
    }

    public void setNumeroIntento(int numeroIntento) {
        this.numeroIntento = numeroIntento;
    }

    public String getCodigoPago() {
        return codigoPago;
    }

    public void setCodigoPago(String codigoPago) {
        this.codigoPago = codigoPago;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getMontoOriginal() {
        return montoOriginal;
    }

    public void setMontoOriginal(double montoOriginal) {
        this.montoOriginal = montoOriginal;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public LocalDate getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDate fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public String getValidadoPor() {
        return validadoPor;
    }

    public void setValidadoPor(String validadoPor) {
        this.validadoPor = validadoPor;
    }

    public String getObservacionValidacion() {
        return observacionValidacion;
    }

    public void setObservacionValidacion(String observacionValidacion) {
        this.observacionValidacion = observacionValidacion;
    }
}
