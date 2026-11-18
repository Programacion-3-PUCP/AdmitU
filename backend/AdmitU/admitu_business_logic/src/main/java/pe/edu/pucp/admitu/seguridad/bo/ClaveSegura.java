package pe.edu.pucp.admitu.seguridad.bo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ClaveSegura {

    public static final int ITERACIONES = 600000;

    private ClaveSegura() {
    }

    public static String crear(String clave) {
        if (clave == null || clave.length() < 12 || clave.length() > 128) {
            throw new IllegalArgumentException("La contraseña debe tener entre 12 y 128 caracteres");
        }
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] derivado = derivar(clave, sal, ITERACIONES);
        return "pbkdf2_sha256$"
                + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(derivado);
    }

    public static boolean verificar(String clave, String guardada) {
        try {
            if (clave == null || clave.length() > 128 || guardada == null) {
                return false;
            }
            String[] partes = guardada.split("\\$");
            if (partes.length != 4 || !"pbkdf2_sha256".equals(partes[0])) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < 100000 || iteraciones > 1000000) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iteraciones));
        } catch (Exception ex) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            spec.clearPassword();
        }
    }

    public static String token() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String huella(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
