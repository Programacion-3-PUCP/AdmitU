import java.util.Date;

class Pago {
    private static int correlativo = 1;
    private int idPago;
    private Postulacion postulacion;
    private MedioPago medioPago;
    private int numeroIntento;
    private double montoOriginal;
    private double porcentajeDescuento;
    private double montoNeto;
    private Date fechaPago;
    private String estadoPago;
    private String codigoPago;
    private Date fechaConciliacion;
    private int conciliadoPorId;

    public Pago() {
        this.numeroIntento = 1;
        this.estadoPago = "G";
        this.conciliadoPorId = 0;
    }

    public Pago(double monto, Date fechaPago, MedioPago medioPago) {
        this.idPago = correlativo++;
        this.montoOriginal = monto;
        this.fechaPago = (Date) fechaPago.clone();
        this.medioPago = medioPago;
        this.montoNeto = monto;
        this.numeroIntento = 1;
        this.estadoPago = "G";
        this.conciliadoPorId = 0;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public double getMonto() {
        return montoOriginal;
    }

    public void setMonto(double monto) {
        this.montoOriginal = monto;
    }

    public Date getFechaPago() {
        return (Date) fechaPago.clone();
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = (Date) fechaPago.clone();
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

    public double getMontoNeto() {
        return montoNeto;
    }

    public void setMontoNeto(double montoNeto) {
        this.montoNeto = montoNeto;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getCodigoPago() {
        return codigoPago;
    }

    public void setCodigoPago(String codigoPago) {
        this.codigoPago = codigoPago;
    }

    public Date getFechaConciliacion() {
        return (Date) fechaConciliacion.clone();
    }

    public void setFechaConciliacion(Date fechaConciliacion) {
        this.fechaConciliacion = (Date) fechaConciliacion.clone();
    }

    public int getConciliadoPorId() {
        return conciliadoPorId;
    }

    public void setConciliadoPorId(int conciliadoPorId) {
        this.conciliadoPorId = conciliadoPorId;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }
}