package pe.edu.pucp.admitu.seguridad.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.seguridad.CuentaUsuario;
import pe.edu.pucp.admitu.seguridad.SesionUsuario;
import pe.edu.pucp.admitu.seguridad.dao.CuentaUsuarioDAO;

public class CuentaUsuarioImpl implements CuentaUsuarioDAO {

    @Override
    public CuentaUsuario buscarCorreo(String correo) {
        CuentaUsuario cuenta = null;
        String sql = "{call BUSCAR_CUENTA_CORREO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString("_correo", correo);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    cuenta = new CuentaUsuario();
                    cuenta.setId(rs.getInt("id"));
                    cuenta.setPersonaId(rs.getInt("persona_id"));
                    cuenta.setCorreo(rs.getString("correo"));
                    cuenta.setRol(rs.getString("rol"));
                    cuenta.setClaveHash(rs.getString("clave_hash"));
                    cuenta.setActivo(rs.getBoolean("activo"));
                    cuenta.setBloqueada(rs.getBoolean("bloqueada"));
                }
            }
            return cuenta;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR CUENTA DE USUARIO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void intento(int id, boolean correcto) {
        String sql = "{call REGISTRAR_INTENTO_CUENTA(?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", id);
            cs.setBoolean("_correcto", correcto);
            cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL REGISTRAR INTENTO DE CUENTA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void crearCuenta(int persona, String correo, String rol, String hash) {
        String sql = "{call INSERTAR_CUENTA_USUARIO(?,?,?,?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                if (persona > 0) {
                    cs.setInt("_persona", persona);
                } else {
                    cs.setNull("_persona", Types.INTEGER);
                }
                cs.setString("_correo", correo);
                cs.setString("_rol", rol);
                cs.setString("_hash", hash);
                cs.executeUpdate();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL CREAR CUENTA DE USUARIO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void crearSesion(String hash, int cuentaId) {
        String sql = "{call INSERTAR_SESION_USUARIO(?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString("_hash", hash);
            cs.setInt("_cuenta", cuentaId);
            cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL CREAR SESION DE USUARIO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SesionUsuario buscarSesion(String hash) {
        SesionUsuario sesion = null;
        String sql = "{call BUSCAR_SESION_USUARIO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString("_hash", hash);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    sesion = new SesionUsuario();
                    sesion.setCuentaId(rs.getInt("id"));
                    sesion.setPersonaId(rs.getInt("persona_id"));
                    sesion.setCorreo(rs.getString("correo"));
                    sesion.setRol(rs.getString("rol"));
                }
            }
            return sesion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR SESION DE USUARIO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void eliminarSesion(String hash) {
        String sql = "{call ELIMINAR_SESION_USUARIO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString("_hash", hash);
            cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR SESION DE USUARIO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Set<Integer> ids(String recurso, int persona) {
        Set<Integer> autorizados = new HashSet<>();
        String sql = "{call LISTAR_IDS_AUTORIZADOS(?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString("_recurso", recurso);
            cs.setInt("_persona", persona);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    autorizados.add(rs.getInt("id"));
                }
            }
            return autorizados;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR IDS AUTORIZADOS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
