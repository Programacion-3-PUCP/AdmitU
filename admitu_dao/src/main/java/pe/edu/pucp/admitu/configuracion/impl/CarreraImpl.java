package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.dao.CarreraDAO;
import pe.edu.pucp.admitu.configuracion.Carrera;
import pe.edu.pucp.admitu.configuracion.Facultad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarreraImpl implements CarreraDAO {

    @Override
    public int insertar(Carrera carrera) {
        String sql = "{call INSERTAR_CARRERA(?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_facultad_id", carrera.getFacultad().getId());
                cs.setString("_codigo", carrera.getCodigoCarrera());
                cs.setString("_nombre", carrera.getNombre());
                cs.executeUpdate();
                carrera.setId(cs.getInt("_id"));
                return carrera.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Carrera carrera) {
        String sql = "{call MODIFICAR_CARRERA(?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", carrera.getId());
            cs.setInt("_facultad_id", carrera.getFacultad().getId());
            cs.setString("_codigo", carrera.getCodigoCarrera());
            cs.setString("_nombre", carrera.getNombre());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idCarrera) {
        String sql = "{call ELIMINAR_CARRERA(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idCarrera);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Carrera buscarPorId(int idCarrera) {
        Carrera carrera = null;
        String sql = "{call LISTAR_CARRERA_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idCarrera);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Facultad f = new Facultad("TMP", rs.getString("nombre_facultad"));
                    f.setId(rs.getInt("facultad_id"));
                    carrera = new Carrera(f, rs.getString("codigo_carrera"), rs.getString("nombre"));
                    carrera.setId(rs.getInt("id"));
                    carrera.setActivo(rs.getBoolean("activo"));
                }
            }
            return carrera;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Carrera> listarTodos() {
        List<Carrera> carreras = null;
        String sql = "{call LISTAR_CARRERAS_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (carreras == null) carreras = new ArrayList<>();
                Facultad f = new Facultad("TMP", rs.getString("nombre_facultad"));
                f.setId(rs.getInt("facultad_id"));
                Carrera c = new Carrera(f, rs.getString("codigo_carrera"), rs.getString("nombre"));
                c.setId(rs.getInt("id"));
                c.setActivo(rs.getBoolean("activo"));
                carreras.add(c);
            }
            return carreras;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR CARRERAS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
