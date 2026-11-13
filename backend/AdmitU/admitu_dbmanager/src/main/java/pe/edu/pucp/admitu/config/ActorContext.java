package pe.edu.pucp.admitu.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

/** Identidad autenticada del servidor. Nunca se deserializa desde JSON del cliente.
 * El filtro síncrono la fija y la elimina para cada petición; los procesos BO de
 * laboratorio sin sesión quedan identificados explícitamente como SISTEMA.
 */
public final class ActorContext {
    private record Actor(int cuentaId, int personaId, String rol) {
        String etiqueta() { return rol + ":" + personaId + " cuenta:" + cuentaId; }
    }
    private static final ThreadLocal<Actor> ACTUAL = new ThreadLocal<>();
    private ActorContext() { }

    public static void set(int cuentaId, int personaId, String rol) {
        if (cuentaId <= 0 || rol == null || !Set.of("admin", "evaluador", "postulante").contains(rol)
                || personaId < 0 || (personaId == 0 && !"admin".equals(rol)))
            throw new IllegalArgumentException("Identidad del servidor no válida");
        ACTUAL.set(new Actor(cuentaId, personaId, rol));
    }

    public static void clear() { ACTUAL.remove(); }

    /** Inicializa todas las variables aun sin actor; evita reutilizar identidad de otra conexión. */
    static void configurar(Connection connection) throws SQLException {
        Actor actor = ACTUAL.get();
        try (PreparedStatement statement = connection.prepareStatement(
                "SET @admitu_actor_cuenta=?, @admitu_actor_persona=?, @admitu_actor_rol=?, @admitu_actor=?")) {
            if (actor == null) {
                statement.setNull(1, Types.INTEGER);
                statement.setNull(2, Types.INTEGER);
                statement.setNull(3, Types.VARCHAR);
                statement.setString(4, "SISTEMA");
            } else {
                statement.setInt(1, actor.cuentaId());
                statement.setInt(2, actor.personaId());
                statement.setString(3, actor.rol());
                statement.setString(4, actor.etiqueta());
            }
            statement.executeUpdate();
        }
    }
}
