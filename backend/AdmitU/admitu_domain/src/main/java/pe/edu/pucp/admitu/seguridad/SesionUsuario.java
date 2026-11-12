package pe.edu.pucp.admitu.seguridad;
import java.security.Principal;
public class SesionUsuario implements Principal {
 private int cuentaId,personaId;private String correo,rol,token;
 public SesionUsuario(){}
 public SesionUsuario(int c,int p,String e,String r){cuentaId=c;personaId=p;correo=e;rol=r;}
 public int getCuentaId(){return cuentaId;}public int getPersonaId(){return personaId;}
 public String getCorreo(){return correo;}public String getRol(){return rol;}public String getToken(){return token;}public void setToken(String t){token=t;}
 public String getName(){return correo;}
}
