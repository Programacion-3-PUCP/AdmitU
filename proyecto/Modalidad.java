public class Modalidad {
    private static int siguienteId = 1;
    private final int id;
    private String codigoModalidad;
    private String nombre;
    private String descripcion;
    private boolean requiereColegio;
    private boolean requiereUniversidad;

    public Modalidad(String codigoModalidad, String nombre, String descripcion
            , boolean requiereColegio, boolean requiereUniversidad) {
        this.id = siguienteId++;
        this.codigoModalidad = codigoModalidad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.requiereColegio = requiereColegio;
        this.requiereUniversidad = requiereUniversidad;
    }

    public int getId() {
        return id;
    }

    public String getCodigoModalidad() {
        return codigoModalidad;
    }

    public void setCodigoModalidad(String codigoModalidad) {
        this.codigoModalidad = codigoModalidad;
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

    public boolean isRequiereColegio() {
        return requiereColegio;
    }

    public void setRequiereColegio(boolean requiereColegio) {
        this.requiereColegio = requiereColegio;
    }

    public boolean isRequiereUniversidad() {
        return requiereUniversidad;
    }

    public void setRequiereUniversidad(boolean requiereUniversidad) {
        this.requiereUniversidad = requiereUniversidad;
    }
}
