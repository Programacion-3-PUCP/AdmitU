public class RequisitoConvocatoriaModalidad {
    private static int siguienteId = 1;
    private final int id;
    private ConvocatoriaModalidad convocatoriaModalidad;
    private Requisito requisito;
    private boolean obligatorio;
    private Integer ordenPresentacion;

    public RequisitoConvocatoriaModalidad(ConvocatoriaModalidad convocatoriaModalidad
            , Requisito requisito, boolean obligatorio, Integer ordenPresentacion) {
        this.id = siguienteId++;
        this.convocatoriaModalidad = convocatoriaModalidad;
        this.requisito = requisito;
        this.obligatorio = obligatorio;
        this.ordenPresentacion = ordenPresentacion;
    }

    public int getId() {
        return id;
    }

    public ConvocatoriaModalidad getConvocatoriaModalidad() {
        return convocatoriaModalidad;
    }

    public void setConvocatoriaModalidad(ConvocatoriaModalidad convocatoriaModalidad) {
        this.convocatoriaModalidad = convocatoriaModalidad;
    }

    public Requisito getRequisito() {
        return requisito;
    }

    public void setRequisito(Requisito requisito) {
        this.requisito = requisito;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public Integer getOrdenPresentacion() {
        return ordenPresentacion;
    }

    public void setOrdenPresentacion(Integer ordenPresentacion) {
        this.ordenPresentacion = ordenPresentacion;
    }
}
