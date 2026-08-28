class Pago {
    private static int correlativo = 1;
    private int idPago;
    private double monto;
    private Date fechaPago;
    private MetodoPago metodo;

    public Pago(double monto, Date fechaPago, MetodoPago metodo) {
        this.idPago = correlativo++;
        this.monto = monto;
        this.fechaPago = (Date) fechaPago.clone();
        this.metodo = metodo;
    }

    public int getIdPago() {
        return idPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Date getFechaPago() {
        return (Date) fechaPago.clone();
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = (Date) fechaPago.clone();
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }
}
