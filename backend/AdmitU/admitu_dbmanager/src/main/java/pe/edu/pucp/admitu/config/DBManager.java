package pe.edu.pucp.admitu.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;

public class DBManager {
    private static DBManager instance;

    private final String hostname;
    private final String user;
    private final String password;
    private final String port;
    private final String database;

    private final String DB_CREDENTIALS_FILE = "db";

    private DBManager() {
        ResourceBundle db = ResourceBundle.getBundle(DB_CREDENTIALS_FILE);
        hostname = db.getString("db.hostname");
        user = db.getString("db.user");
        password = db.getString("db.password");
        port = db.getString("db.port");
        database = db.getString("db.database");
    }

    public static synchronized DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    public Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
            con = DriverManager.getConnection(url, user, password);
            ActorContext.configurar(con);
        } catch (Exception ex) {
            System.out.println("ERROR al conectar con la BD: " + ex.getMessage());
        }
        return con;
    }
}
