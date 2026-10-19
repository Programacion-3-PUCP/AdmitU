package pe.edu.pucp.admitu.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public final class DBManager {
    private static final DBManager INSTANCE = new DBManager();
    private final String hostname;
    private final String user;
    private final String password;
    private final String port;
    private final String database;

    private DBManager() {
        ResourceBundle db = ResourceBundle.getBundle("db");
        hostname = valor("ADMITU_DB_HOST", db.getString("db.hostname"));
        user = valor("ADMITU_DB_USER", db.getString("db.user"));
        password = valor("ADMITU_DB_PASSWORD", db.getString("db.password"));
        port = valor("ADMITU_DB_PORT", db.getString("db.port"));
        database = valor("ADMITU_DB_NAME", db.getString("db.database"));
    }

    private static String valor(String variable, String defecto) {
        String valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? defecto : valor;
    }

    public static DBManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database
                + "?connectionTimeZone=SERVER&useUnicode=true&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }
}
