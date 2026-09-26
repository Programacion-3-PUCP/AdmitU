package pe.edu.pucp.admitu.configuracion;

public class OfertaCarrera {
    private int id;
    private Convocatoria convocatoria;
    private Carrera carrera;
    private int cantidadVacantes;

    public OfertaCarrera(Convocatoria convocatoria, Carrera carrera, int cantidadVacantes) {
        this.convocatoria = convocatoria;
        this.carrera = carrera;
        this.cantidadVacantes = cantidadVacantes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {this.id = id;}

    public Convocatoria getConvocatoria() {
        return convocatoria;
    }

    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public int getCantidadVacantes() {
        return cantidadVacantes;
    }

    public void setCantidadVacantes(int cantidadVacantes) {
        this.cantidadVacantes = cantidadVacantes;
    }
}