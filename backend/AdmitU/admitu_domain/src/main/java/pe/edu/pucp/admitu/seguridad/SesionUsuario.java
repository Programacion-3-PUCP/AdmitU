package pe.edu.pucp.admitu.seguridad;

import java.security.Principal;

public class SesionUsuario implements Principal {
    private int cuentaId;
    private int personaId;
    private String correo;
    private String rol;
    private String token;

    public SesionUsuario() {
    }

    public SesionUsuario(int cuentaId, int personaId, String correo, String rol) {
        this.cuentaId = cuentaId;
        this.personaId = personaId;
        this.correo = correo;
        this.rol = rol;
    }

    public int getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(int cuentaId) {
        this.cuentaId = cuentaId;
    }

    public int getPersonaId() {
        return personaId;
    }

    public void setPersonaId(int personaId) {
        this.personaId = personaId;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getName() {
        return correo;
    }
}
