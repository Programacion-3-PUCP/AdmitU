public class InstitucionEducativa {
    private static int siguienteId = 1;
    private final int id;
    private Pais pais;
    private String codigoExterno;
    private String nombre;
    private TipoInstitucion tipoInstitucion;

    public InstitucionEducativa(Pais pais, String codigoExterno, String nombre
            , TipoInstitucion tipoInstitucion) {
        this.id = siguienteId++;
        this.pais = pais;
        this.codigoExterno = codigoExterno;
        this.nombre = nombre;
        this.tipoInstitucion = tipoInstitucion;
    }

    public int getId() {
        return id;
    }

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
}