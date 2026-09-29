package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.postulacion.CarnePostulante;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.dao.CarnePostulanteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarnePostulanteImpl implements CarnePostulanteDAO {

    @Override
    public int insertar(CarnePostulante carne) {
        String sql = "{call INSERTAR_CARNE_POSTULANTE(?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulacion_id", carne.getPostulacion().getId());
                cs.setInt("_sede_id", carne.getSede().getId());
                cs.setString("_codigo_carne", carne.getCodigoCarne());
                cs.setDate("_fecha_generacion", carne.getFechaGeneracion() != null ? Date.valueOf(carne.getFechaGeneracion()) : null);
                cs.setDate("_fecha_inicio_vigencia", carne.getFechaInicioVigencia() != null ? Date.valueOf(carne.getFechaInicioVigencia()) : null);
                cs.setDate("_fecha_fin_vigencia", carne.getFechaFinVigencia() != null ? Date.valueOf(carne.getFechaFinVigencia()) : null);
                cs.setString("_aula_examen", carne.getAulaExamen());
                cs.executeUpdate();
                carne.setId(cs.getInt("_id"));
                return carne.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR CARNE POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(CarnePostulante carne) {
        String sql = "{call MODIFICAR_CARNE_POSTULANTE(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", carne.getId());
            cs.setInt("_postulacion_id", carne.getPostulacion().getId());
            cs.setInt("_sede_id", carne.getSede().getId());
            cs.setString("_codigo_carne", carne.getCodigoCarne());
            cs.setDate("_fecha_generacion", carne.getFechaGeneracion() != null ? Date.valueOf(carne.getFechaGeneracion()) : null);
            cs.setDate("_fecha_inicio_vigencia", carne.getFechaInicioVigencia() != null ? Date.valueOf(carne.getFechaInicioVigencia()) : null);
            cs.setDate("_fecha_fin_vigencia", carne.getFechaFinVigencia() != null ? Date.valueOf(carne.getFechaFinVigencia()) : null);
            cs.setString("_aula_examen", carne.getAulaExamen());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR CARNE POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idCarne) {
        String sql = "{call ELIMINAR_CARNE_POSTULANTE(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idCarne);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR CARNE POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private CarnePostulante mapear(ResultSet rs) throws Exception {
        Postulacion postulacion = new Postulacion();
        postulacion.setId(rs.getInt("postulacion_id"));
        Sede sede = new Sede(rs.getString("codigo_sede"), rs.getString("nombre_sede"), rs.getString("direccion_sede"));
        sede.setId(rs.getInt("sede_id"));
        CarnePostulante carne = new CarnePostulante(postulacion, sede, rs.getString("codigo_carne"),
            rs.getDate("fecha_generacion") != null ? rs.getDate("fecha_generacion").toLocalDate() : null,
            rs.getDate("fecha_inicio_vigencia") != null ? rs.getDate("fecha_inicio_vigencia").toLocalDate() : null,
            rs.getDate("fecha_fin_vigencia") != null ? rs.getDate("fecha_fin_vigencia").toLocalDate() : null,
            rs.getString("aula_examen"));
        carne.setId(rs.getInt("id"));
        return carne;
    }

    @Override
    public CarnePostulante buscarPorId(int idCarne) {
        CarnePostulante carne = null;
        String sql = "{call LISTAR_CARNE_POSTULANTE_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idCarne);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) carne = mapear(rs);
            }
            return carne;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR CARNE POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<CarnePostulante> listarTodos() {
        List<CarnePostulante> carnes = null;
        String sql = "{call LISTAR_CARNES_POSTULANTE_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (carnes == null) carnes = new ArrayList<>();
                carnes.add(mapear(rs));
            }
            return carnes;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR CARNES POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
