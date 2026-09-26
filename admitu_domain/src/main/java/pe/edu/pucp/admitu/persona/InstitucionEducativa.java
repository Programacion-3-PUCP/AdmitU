package pe.edu.pucp.admitu.persona;

public class InstitucionEducativa {
    private int id;
    private Pais pais;
    private String codigoExterno;
    private String nombre;
    private TipoInstitucion tipoInstitucion;
    private boolean activo;

    public InstitucionEducativa(Pais pais, String codigoExterno, String nombre
            , TipoInstitucion tipoInstitucion) {
        this.pais = pais;
        this.codigoExterno = codigoExterno;
        this.nombre = nombre;
        this.tipoInstitucion = tipoInstitucion;
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {this.id = id;}

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    public void setCodigoExterno(String codigoExterno) {
        this.codigoExterno = codigoExterno;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoInstitucion getTipoInstitucion() {
        return tipoInstitucion;
    }

    public void setTipoInstitucion(TipoInstitucion tipoInstitucion) {
        this.tipoInstitucion = tipoInstitucion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}