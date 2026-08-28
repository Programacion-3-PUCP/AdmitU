class Postulante extends Persona {
    private EstadoPostulante estado;
    private List<Pago> pagos;

    public Postulante(String nombres, String apellidos, String dni, EstadoPostulante estado) {
        super(nombres, apellidos, dni);
        this.estado = estado;
        this.pagos = new ArrayList<>();
    }

    public EstadoPostulante getEstado() {
        return estado;
    }

    public void setEstado(EstadoPostulante estado) {
        this.estado = estado;
    }

    public List<Pago> getPagos() {
        return new ArrayList<>(pagos);
    }

    public void agregarPago(Pago pago) {
        this.pagos.add(pago);
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("Postulante: " + getNombres() + " " + getApellidos() + " - DNI: " + getDni() + " - Estado: " + estado);
    }
}
