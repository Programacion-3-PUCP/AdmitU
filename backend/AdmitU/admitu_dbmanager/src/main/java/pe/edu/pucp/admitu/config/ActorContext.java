package pe.edu.pucp.admitu.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ActorContext {

    private static class Actor {
        private int cuentaId;
        private int personaId;
        private String rol;

        public Actor(int cuentaId, int personaId, String rol) {
            this.cuentaId = cuentaId;
            this.personaId = personaId;
            this.rol = rol;
        }

        public int getCuentaId() {
            return cuentaId;
        }

        public int getPersonaId() {
            return personaId;
        }

        public String getRol() {
            return rol;
        }

        public String getEtiqueta() {
            return rol + ":" + personaId + " cuenta:" + cuentaId;
        }
    }

    private static final ThreadLocal<Actor> ACTUAL = new ThreadLocal<>();

    private ActorContext() {
    }

    public static void set(int cuentaId, int personaId, String rol) {
        boolean rolValido = "admin".equals(rol) || "evaluador".equals(rol) || "postulante".equals(rol);
        if (cuentaId <= 0 || !rolValido || personaId < 0 || (personaId == 0 && !"admin".equals(rol))) {
            throw new IllegalArgumentException("Identidad del servidor no válida");
        }
        ACTUAL.set(new Actor(cuentaId, personaId, rol));
    }

    public static void clear() {
        ACTUAL.remove();
    }

    static void configurar(Connection connection) throws SQLException {
        Actor actor = ACTUAL.get();
        String sql = "SET @admitu_actor_cuenta=?, @admitu_actor_persona=?, @admitu_actor_rol=?, @admitu_actor=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (actor == null) {
                statement.setNull(1, Types.INTEGER);
                statement.setNull(2, Types.INTEGER);
                statement.setNull(3, Types.VARCHAR);
                statement.setString(4, "SISTEMA");
            } else {
                statement.setInt(1, actor.getCuentaId());
                statement.setInt(2, actor.getPersonaId());
                statement.setString(3, actor.getRol());
                statement.setString(4, actor.getEtiqueta());
            }
            statement.executeUpdate();
        }
    }
}
