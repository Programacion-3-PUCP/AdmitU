package pe.edu.pucp.admitu.pago;

import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.time.LocalDate;

public class Pago {
    private static int siguienteId = 1;
    private final int id;
    private Postulacion postulacion;
    private MedioPago medioPago;
    private double monto;
    private LocalDate fechaGeneracion;
    private LocalDate fechaPago;
    private String codigoPago;
    private String referenciaPasarela;
    private EstadoPago estadoPago;
    private String rutaVoucher;

    public Pago(Postulacion postulacion, MedioPago medioPago, double monto
            , LocalDate fechaGeneracion, LocalDate fechaPago, String codigoPago
            , String referenciaPasarela, EstadoPago estadoPago, String rutaVoucher) {
        this.id = siguienteId++;
        this.postulacion = postulacion;
        this.medioPago = medioPago;
        this.monto = monto;
        this.fechaGeneracion = fechaGeneracion;
        this.fechaPago = fechaPago;
        this.codigoPago = codigoPago;
        this.referenciaPasarela = referenciaPasarela;
        this.estadoPago = estadoPago;
        this.rutaVoucher = rutaVoucher;
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

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
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

    public String getCodigoPago() {
        return codigoPago;
    }

    public void setCodigoPago(String codigoPago) {
        this.codigoPago = codigoPago;
    }

    public String getReferenciaPasarela() {
        return referenciaPasarela;
    }

    public void setReferenciaPasarela(String referenciaPasarela) {
        this.referenciaPasarela = referenciaPasarela;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getRutaVoucher() {
        return rutaVoucher;
    }

    public void setRutaVoucher(String rutaVoucher) {
        this.rutaVoucher = rutaVoucher;
    }
}
