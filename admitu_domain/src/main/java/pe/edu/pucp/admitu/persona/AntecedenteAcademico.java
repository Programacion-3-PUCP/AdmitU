package pe.edu.pucp.admitu.persona;

public class AntecedenteAcademico {
    private static int siguienteId = 1;
    private final int id;
    private Postulante postulante;
    private InstitucionEducativa institucion;
    private int anioInicio;
    private int anioFin;
    private String descripcion;
    private boolean activo;

    public AntecedenteAcademico(Postulante postulante, InstitucionEducativa institucion
            , int anioInicio, int anioFin, String descripcion) {
        this.id = siguienteId++;
        this.postulante = postulante;
        this.institucion = institucion;
        this.anioInicio = anioInicio;
        this.anioFin = anioFin;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    public Postulante getPostulante() {
        return postulante;
    }

    public void setPostulante(Postulante postulante) {
        this.postulante = postulante;
    }

    public InstitucionEducativa getInstitucion() {
        return institucion;
    }

    public void setInstitucion(InstitucionEducativa institucion) {
        this.institucion = institucion;
    }

    public int getAnioInicio() {
        return anioInicio;
    }

    public void setAnioInicio(int anioInicio) {
        this.anioInicio = anioInicio;
    }

    public int getAnioFin() {
        return anioFin;
    }

    public void setAnioFin(int anioFin) {
        this.anioFin = anioFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}