package pe.edu.pucp.admitu.seguridad;
public record CuentaUsuario(int id,int personaId,String correo,String rol,String claveHash,boolean activo,boolean bloqueada) {}
