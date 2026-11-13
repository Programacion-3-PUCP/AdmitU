package pe.edu.pucp.admitu.seguridad.bo;
import java.security.*;import java.util.*;import javax.crypto.SecretKeyFactory;import javax.crypto.spec.PBEKeySpec;
public final class ClaveSegura {
 private ClaveSegura(){}public static final int ITERACIONES=600000;
 public static String crear(String clave){if(clave==null||clave.length()<12||clave.length()>128)throw new IllegalArgumentException("La contraseña debe tener entre 12 y 128 caracteres");byte[] sal=new byte[16];new SecureRandom().nextBytes(sal);return "pbkdf2_sha256$"+ITERACIONES+"$"+Base64.getEncoder().encodeToString(sal)+"$"+Base64.getEncoder().encodeToString(derivar(clave,sal,ITERACIONES));}
 public static boolean verificar(String clave,String guardada){try{if(clave==null||clave.length()>128)return false;String[] p=guardada.split("\\$");int it=Integer.parseInt(p[1]);if(p.length!=4||!p[0].equals("pbkdf2_sha256")||it<100000||it>1000000)return false;return MessageDigest.isEqual(Base64.getDecoder().decode(p[3]),derivar(clave,Base64.getDecoder().decode(p[2]),it));}catch(Exception e){return false;}}
 private static byte[] derivar(String c,byte[] s,int it){PBEKeySpec spec=new PBEKeySpec(c.toCharArray(),s,it,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(Exception e){throw new IllegalStateException(e);}finally{spec.clearPassword();}}
 public static String token(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
 public static String huella(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
