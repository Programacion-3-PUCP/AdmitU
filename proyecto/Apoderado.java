import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Apoderado extends Persona {
    private static int siguienteId = 1;
    private Parentesco parentesco;
    private List<Postulante> postulantes;

    public Apoderado(String nombres, String apellidoPaterno, String apellidoMaterno, String correo
            , TipoDocumento tipoDocumento, String numeroDocumento, String telefono, Parentesco parentesco
            , List<Postulante> postulantes) {
        super(nombres, apellidoPaterno, apellidoMaterno, correo, tipoDocumento, numeroDocumento, telefono);
        setId(siguienteId);
        siguienteId++;
        this.parentesco = parentesco;
        this.postulantes = new ArrayList<>(postulantes);
    }

    public Parentesco getParentesco() {
        return parentesco;
    }

    public void setParentesco(Parentesco parentesco) {
        this.parentesco = parentesco;
    }

    public List<Postulante> getPostulantes() {
        return new ArrayList<Postulante>(postulantes);
    }

    public void setPostulantes(List<Postulante> postulantes) {
        this.postulantes = postulantes;
    }
}
