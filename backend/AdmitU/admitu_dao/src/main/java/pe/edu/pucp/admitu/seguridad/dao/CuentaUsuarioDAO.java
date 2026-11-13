package pe.edu.pucp.admitu.seguridad.dao;
import java.util.Set;import pe.edu.pucp.admitu.seguridad.*;
public interface CuentaUsuarioDAO {
 CuentaUsuario buscarCorreo(String correo);void intento(int id,boolean correcto);void crearCuenta(int persona,String correo,String rol,String hash);void crearSesion(String hash,int cuenta);SesionUsuario buscarSesion(String hash);void eliminarSesion(String hash);Set<Integer> ids(String recurso,int persona);
}
