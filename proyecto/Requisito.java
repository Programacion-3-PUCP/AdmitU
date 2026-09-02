public class Requisito {
    private static int siguienteId = 1;
    private final int id;
    private String codigoRequisito;
    private String nombre;
    private String descripcion;
    private TipoArchivo tipoArchivoRequerido;
    private int tamanioMaximoBytes;

    public Requisito(String codigoRequisito, String nombre, String descripcion
            , TipoArchivo tipoArchivoRequerido, int tamanioMaximoBytes ) {
        this.id = siguienteId++;
        this.codigoRequisito = codigoRequisito;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoArchivoRequerido = tipoArchivoRequerido;
        this.tamanioMaximoBytes = tamanioMaximoBytes;
    }

    public int getId() {
        return id;
    }

    public String getCodigoRequisito() {
        return codigoRequisito;
    }

    public void setCodigoRequisito(String codigoRequisito) {
        this.codigoRequisito = codigoRequisito;
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

    public TipoArchivo getTipoArchivoRequerido() {
        return tipoArchivoRequerido;
    }

    public void setTipoArchivoRequerido(TipoArchivo tipoArchivoRequerido) {
        this.tipoArchivoRequerido = tipoArchivoRequerido;
    }

    public int getTamanioMaximoBytes() {
        return tamanioMaximoBytes;
    }

    public void setTamanioMaximoBytes(int tamanioMaximoBytes) {
        this.tamanioMaximoBytes = tamanioMaximoBytes;
    }
}
