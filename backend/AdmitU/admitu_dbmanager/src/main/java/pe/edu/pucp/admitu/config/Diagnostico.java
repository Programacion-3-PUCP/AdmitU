package pe.edu.pucp.admitu.config;

import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Describe un fallo sin imprimir mensajes, SQL, datos personales ni credenciales. */
public final class Diagnostico {
    private Diagnostico() { }

    public static String resumen(Throwable error) {
        if (error == null) return "tipo=desconocido";
        String tipo = error.getClass().getSimpleName();
        Set<Throwable> visitados = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable causa = error; causa != null && visitados.add(causa); causa = causa.getCause()) {
            if (causa instanceof SQLException sql) {
                String estado = sql.getSQLState();
                if (estado == null || !estado.matches("[A-Z0-9]{5}")) estado = "desconocido";
                return "tipo=" + tipo + "; SQLState=" + estado + "; codigo=" + sql.getErrorCode();
            }
        }
        return "tipo=" + tipo;
    }
}
