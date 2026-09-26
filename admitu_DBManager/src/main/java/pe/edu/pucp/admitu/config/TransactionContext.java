package pe.edu.pucp.admitu.config;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionContext {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static Connection getConnection() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null || conn.isClosed()) {
            conn = DBManager.getInstance().getConnection();
            conn.setAutoCommit(false);
            connectionHolder.set(conn);
        }
        return conn;
    }

    public static void commit() throws SQLException{
        Connection conn = connectionHolder.get();
        if(conn != null){
            conn.commit();
        }
    }

    public static void rollback() {
        Connection conn = connectionHolder.get();
        if(conn != null){
            try {
                conn.rollback();
            }catch(SQLException ex){
                System.out.println("ERROR AL REALIZAR ROLLBACK" + ex.getMessage());
            }
        }
    }

    public static void close() {
        Connection conn = connectionHolder.get();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println("ERROR AL CERRAR LA CONEXION: " + ex.getMessage());
        } finally {
            connectionHolder.remove();
        }
    }
}
