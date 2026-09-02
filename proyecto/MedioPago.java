public class MedioPago {
    private static int siguienteId = 1;
    private final int id;
    private String codigo;
    private String nombre;
    private boolean esPasarela;

    public MedioPago(String codigo, String nombre, boolean esPasarela) {
        this.id = siguienteId++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.esPasarela = esPasarela;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsPasarela() {
        return esPasarela;
    }

    public void setEsPasarela(boolean esPasarela) {
        this.esPasarela = esPasarela;
    }
}