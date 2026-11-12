package pe.edu.pucp.admitu.seguridad.boi;
import java.util.Set;import pe.edu.pucp.admitu.seguridad.SesionUsuario;import pe.edu.pucp.admitu.persona.Postulante;
public interface IAutenticacionBO {SesionUsuario ingresar(String correo,String clave);int registrar(Postulante p,String clave);SesionUsuario autenticar(String token);void salir(String token);Set<Integer> idsAutorizados(String recurso,int persona);}
