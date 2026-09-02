import java.time.LocalDate;

public class Etapa {
    private static int siguienteId = 1;
    private final int id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String codigoEtapa;
    private String nombre;
    private String descripcion;

    public Etapa(LocalDate fechaInicio, LocalDate fechaFin, String codigoEtapa, String nombre
            , String descripcion) {
        this.id = siguienteId++;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.codigoEtapa = codigoEtapa;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getCodigoEtapa() {
        return codigoEtapa;
    }

    public void setCodigoEtapa(String codigoEtapa) {
        this.codigoEtapa = codigoEtapa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}