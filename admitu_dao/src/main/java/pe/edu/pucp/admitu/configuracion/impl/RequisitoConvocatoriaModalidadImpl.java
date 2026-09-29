package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.TipoArchivo;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoConvocatoriaModalidadDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequisitoConvocatoriaModalidadImpl implements RequisitoConvocatoriaModalidadDAO {

    @Override
    public int insertar(RequisitoConvocatoriaModalidad relacion) {
        String sql = "{call INSERTAR_REQUISITO_CONVOCATORIA_MODALIDAD(?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_convocatoria_modalidad_id", relacion.getConvocatoriaModalidad().getId());
                cs.setInt("_requisito_id", relacion.getRequisito().getId());
                cs.setBoolean("_obligatorio", relacion.isObligatorio());
                if (relacion.getOrdenPresentacion() == null) cs.setNull("_orden_presentacion", Types.INTEGER);
                else cs.setInt("_orden_presentacion", relacion.getOrdenPresentacion());
                cs.executeUpdate();
                relacion.setId(cs.getInt("_id"));
                return relacion.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR REQUISITO CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(RequisitoConvocatoriaModalidad relacion) {
        String sql = "{call MODIFICAR_REQUISITO_CONVOCATORIA_MODALIDAD(?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", relacion.getId());
            cs.setInt("_convocatoria_modalidad_id", relacion.getConvocatoriaModalidad().getId());
            cs.setInt("_requisito_id", relacion.getRequisito().getId());
            cs.setBoolean("_obligatorio", relacion.isObligatorio());
            if (relacion.getOrdenPresentacion() == null) cs.setNull("_orden_presentacion", Types.INTEGER);
            else cs.setInt("_orden_presentacion", relacion.getOrdenPresentacion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR REQUISITO CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idRelacion) {
        String sql = "{call ELIMINAR_REQUISITO_CONVOCATORIA_MODALIDAD(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRelacion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR REQUISITO CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private RequisitoConvocatoriaModalidad mapear(ResultSet rs) throws Exception {
        ConvocatoriaModalidad convocatoriaModalidad = new ConvocatoriaModalidad();
        convocatoriaModalidad.setId(rs.getInt("convocatoria_modalidad_id"));
        Requisito requisito = new Requisito(rs.getString("codigo_requisito"), rs.getString("nombre_requisito"),
            rs.getString("descripcion_requisito"), rs.getString("tipo_archivo_requisito") != null ? TipoArchivo.valueOf(rs.getString("tipo_archivo_requisito")) : null,
            rs.getInt("tamanio_maximo_requisito"));
        requisito.setId(rs.getInt("requisito_id"));
        Object orden = rs.getObject("orden_presentacion");
        RequisitoConvocatoriaModalidad relacion = new RequisitoConvocatoriaModalidad(convocatoriaModalidad, requisito,
            rs.getBoolean("obligatorio"), orden != null ? rs.getInt("orden_presentacion") : null);
        relacion.setId(rs.getInt("id"));
        return relacion;
    }

    @Override
    public RequisitoConvocatoriaModalidad buscarPorId(int idRelacion) {
        RequisitoConvocatoriaModalidad relacion = null;
        String sql = "{call LISTAR_REQUISITO_CONVOCATORIA_MODALIDAD_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRelacion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) relacion = mapear(rs);
            }
            return relacion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR REQUISITO CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<RequisitoConvocatoriaModalidad> listarTodos() {
        List<RequisitoConvocatoriaModalidad> relaciones = null;
        String sql = "{call LISTAR_REQUISITOS_CONVOCATORIA_MODALIDAD_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (relaciones == null) relaciones = new ArrayList<>();
                relaciones.add(mapear(rs));
            }
            return relaciones;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR REQUISITOS CONVOCATORIA MODALIDAD: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
