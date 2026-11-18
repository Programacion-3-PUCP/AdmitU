package pe.edu.pucp.admitu.seguridad.boi;

import java.util.Set;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.seguridad.SesionUsuario;

public interface IAutenticacionBO {

    SesionUsuario ingresar(String correo, String clave);

    int registrar(Postulante postulante, String clave);

    int registrarEvaluador(Evaluador evaluador, String clave);

    SesionUsuario autenticar(String token);

    void salir(String token);

    Set<Integer> idsAutorizados(String recurso, int persona);
}
