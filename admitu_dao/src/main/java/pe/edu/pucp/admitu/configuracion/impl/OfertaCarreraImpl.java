package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.dao.OfertaCarreraDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfertaCarreraImpl implements OfertaCarreraDAO {

    @Override
    public int insertar(pe.edu.pucp.admitu.configuracion.OfertaCarrera oferta) {
        String sql = "{call INSERTAR_OFERTA_CARRERA(?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_convocatoria_id", oferta.getConvocatoria().getId());
                cs.setInt("_carrera_id", oferta.getCarrera().getId());
                cs.setInt("_cantidad_vacantes", oferta.getCantidadVacantes());
                cs.executeUpdate();
                oferta.setId(cs.getInt("_id"));
                return oferta.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR OFERTA CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(pe.edu.pucp.admitu.configuracion.OfertaCarrera oferta) {
        String sql = "{call MODIFICAR_OFERTA_CARRERA(?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", oferta.getId());
            cs.setInt("_convocatoria_id", oferta.getConvocatoria().getId());
            cs.setInt("_carrera_id", oferta.getCarrera().getId());
            cs.setInt("_cantidad_vacantes", oferta.getCantidadVacantes());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR OFERTA CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idOferta) {
        String sql = "{call ELIMINAR_OFERTA_CARRERA(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idOferta);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR OFERTA CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private pe.edu.pucp.admitu.configuracion.OfertaCarrera mapear(ResultSet rs) throws Exception {
        pe.edu.pucp.admitu.configuracion.Convocatoria convocatoria = new pe.edu.pucp.admitu.configuracion.Convocatoria();
        convocatoria.setId(rs.getInt("convocatoria_id"));
        pe.edu.pucp.admitu.configuracion.Facultad facultad = new pe.edu.pucp.admitu.configuracion.Facultad("TMP", rs.getString("nombre_facultad"));
        facultad.setId(rs.getInt("facultad_id"));
        pe.edu.pucp.admitu.configuracion.Carrera carrera = new pe.edu.pucp.admitu.configuracion.Carrera(facultad,
            rs.getString("codigo_carrera"), rs.getString("nombre_carrera"));
        carrera.setId(rs.getInt("carrera_id"));
        pe.edu.pucp.admitu.configuracion.OfertaCarrera oferta = new pe.edu.pucp.admitu.configuracion.OfertaCarrera(convocatoria,
            carrera, rs.getInt("cantidad_vacantes"));
        oferta.setId(rs.getInt("id"));
        return oferta;
    }

    @Override
    public pe.edu.pucp.admitu.configuracion.OfertaCarrera buscarPorId(int idOferta) {
        pe.edu.pucp.admitu.configuracion.OfertaCarrera oferta = null;
        String sql = "{call LISTAR_OFERTA_CARRERA_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idOferta);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) oferta = mapear(rs);
            }
            return oferta;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR OFERTA CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public java.util.List<pe.edu.pucp.admitu.configuracion.OfertaCarrera> listarTodos() {
        java.util.List<pe.edu.pucp.admitu.configuracion.OfertaCarrera> ofertas = null;
        String sql = "{call LISTAR_OFERTAS_CARRERA_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (ofertas == null) ofertas = new ArrayList<>();
                ofertas.add(mapear(rs));
            }
            return ofertas;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR OFERTAS CARRERA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
