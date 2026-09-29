package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaModalidadDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConvocatoriaModalidadImpl implements ConvocatoriaModalidadDAO {

    @Override
    public int insertar(ConvocatoriaModalidad relacion) {
        String sql = "{call INSERTAR_CONVOCATORIA_MODALIDAD(?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_convocatoria_id", relacion.getConvocatoria().getId());
                cs.setInt("_modalidad_id", relacion.getModalidad().getId());
                cs.setDouble("_costo_inscripcion", relacion.getCostoInscripcion());
                cs.setString("_observacion", relacion.getObservacion());
                cs.executeUpdate();
                relacion.setId(cs.getInt("_id"));
                return relacion.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(ConvocatoriaModalidad relacion) {
        String sql = "{call MODIFICAR_CONVOCATORIA_MODALIDAD(?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", relacion.getId());
            cs.setInt("_convocatoria_id", relacion.getConvocatoria().getId());
            cs.setInt("_modalidad_id", relacion.getModalidad().getId());
            cs.setDouble("_costo_inscripcion", relacion.getCostoInscripcion());
            cs.setString("_observacion", relacion.getObservacion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idRelacion) {
        String sql = "{call ELIMINAR_CONVOCATORIA_MODALIDAD(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRelacion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private ConvocatoriaModalidad mapear(ResultSet rs) throws Exception {
        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setId(rs.getInt("convocatoria_id"));
        Modalidad modalidad = new Modalidad(rs.getString("codigo_modalidad"), rs.getString("nombre_modalidad"),
            rs.getString("descripcion_modalidad"), rs.getBoolean("requiere_colegio"), rs.getBoolean("requiere_universidad"));
        modalidad.setId(rs.getInt("modalidad_id"));
        ConvocatoriaModalidad relacion = new ConvocatoriaModalidad(convocatoria, modalidad,
            rs.getDouble("costo_inscripcion"), rs.getString("observacion"), null);
        relacion.setId(rs.getInt("id"));
        return relacion;
    }

    @Override
    public ConvocatoriaModalidad buscarPorId(int idRelacion) {
        ConvocatoriaModalidad relacion = null;
        String sql = "{call LISTAR_CONVOCATORIA_MODALIDAD_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRelacion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) relacion = mapear(rs);
            }
            return relacion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<ConvocatoriaModalidad> listarTodos() {
        List<ConvocatoriaModalidad> relaciones = null;
        String sql = "{call LISTAR_CONVOCATORIAS_MODALIDAD_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (relaciones == null) relaciones = new ArrayList<>();
                relaciones.add(mapear(rs));
            }
            return relaciones;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR CONVOCATORIAS MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
