package pe.edu.pucp.admitu.seguridad;

public class CuentaUsuario {
    private int id;
    private int personaId;
    private String correo;
    private String rol;
    private String claveHash;
    private boolean activo;
    private boolean bloqueada;

    public CuentaUsuario() {
    }

    public CuentaUsuario(int id, int personaId, String correo, String rol, String claveHash, boolean activo, boolean bloqueada) {
        this.id = id;
        this.personaId = personaId;
        this.correo = correo;
        this.rol = rol;
        this.claveHash = claveHash;
        this.activo = activo;
        this.bloqueada = bloqueada;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }
}
