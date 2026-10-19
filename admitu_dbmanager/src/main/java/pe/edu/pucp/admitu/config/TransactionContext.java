package pe.edu.pucp.admitu.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public final class TransactionContext {
    private static final ThreadLocal<Connection> conexiones = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ejecutando = ThreadLocal.withInitial(() -> false);

    private TransactionContext() { }

    public static Connection getConnection() {
        Connection con = conexiones.get();
        try {
            if (con == null || con.isClosed()) {
                con = DBManager.getInstance().getConnection();
                con.setAutoCommit(false);
                conexiones.set(con);
            }
            return con;
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo abrir la conexión", ex);
        }
    }

    public static void commit() throws SQLException {
        Connection con = conexiones.get();
        if (con != null && !con.isClosed()) con.commit();
    }

    public static void rollback() {
        Connection con = conexiones.get();
        if (con != null) {
            try {
                if (!con.isClosed()) con.rollback();
            } catch (SQLException ex) {
                throw new IllegalStateException("No se pudo revertir la transacción", ex);
            }
        }
    }

    public static void close() {
        Connection con = conexiones.get();
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo cerrar la conexión", ex);
        } finally {
            conexiones.remove();
        }
    }

    public static <T> T ejecutar(Supplier<T> operacion) {
        if (Boolean.TRUE.equals(ejecutando.get())) return operacion.get();
        ejecutando.set(true);
        try {
            T resultado = operacion.get();
            commit();
            return resultado;
        } catch (SQLException ex) {
            try { rollback(); } catch (RuntimeException otro) { ex.addSuppressed(otro); }
            throw new IllegalStateException("No se pudo confirmar la transacción", ex);
        } catch (RuntimeException | Error ex) {
            try { rollback(); } catch (RuntimeException otro) { ex.addSuppressed(otro); }
            throw ex;
        } finally {
            try { close(); } finally { ejecutando.remove(); }
        }
    }
}
