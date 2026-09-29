package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.dao.ModalidadDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModalidadImpl implements ModalidadDAO {

    @Override
    public int insertar(Modalidad modalidad) {
        String sql = "{call INSERTAR_MODALIDAD(?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo_modalidad", modalidad.getCodigoModalidad());
                cs.setString("_nombre", modalidad.getNombre());
                cs.setString("_descripcion", modalidad.getDescripcion());
                cs.setBoolean("_requiere_colegio", modalidad.isRequiereColegio());
                cs.setBoolean("_requiere_universidad", modalidad.isRequiereUniversidad());
                cs.executeUpdate();
                modalidad.setId(cs.getInt("_id"));
                return modalidad.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Modalidad modalidad) {
        String sql = "{call MODIFICAR_MODALIDAD(?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", modalidad.getId());
            cs.setString("_codigo_modalidad", modalidad.getCodigoModalidad());
            cs.setString("_nombre", modalidad.getNombre());
            cs.setString("_descripcion", modalidad.getDescripcion());
            cs.setBoolean("_requiere_colegio", modalidad.isRequiereColegio());
            cs.setBoolean("_requiere_universidad", modalidad.isRequiereUniversidad());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idModalidad) {
        String sql = "{call ELIMINAR_MODALIDAD(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idModalidad);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Modalidad mapear(ResultSet rs) throws Exception {
        Modalidad modalidad = new Modalidad(rs.getString("codigo_modalidad"), rs.getString("nombre"),
            rs.getString("descripcion"), rs.getBoolean("requiere_colegio"), rs.getBoolean("requiere_universidad"));
        modalidad.setId(rs.getInt("id"));
        modalidad.setActivo(rs.getBoolean("activo"));
        return modalidad;
    }

    @Override
    public Modalidad buscarPorId(int idModalidad) {
        Modalidad modalidad = null;
        String sql = "{call LISTAR_MODALIDAD_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idModalidad);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) modalidad = mapear(rs);
            }
            return modalidad;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Modalidad> listarTodos() {
        List<Modalidad> modalidades = null;
        String sql = "{call LISTAR_MODALIDADES_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (modalidades == null) modalidades = new ArrayList<>();
                modalidades.add(mapear(rs));
            }
            return modalidades;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR MODALIDADES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
