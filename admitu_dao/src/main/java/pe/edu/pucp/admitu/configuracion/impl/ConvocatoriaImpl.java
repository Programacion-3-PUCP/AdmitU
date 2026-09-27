package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaDAO;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.EstadoConvocatoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConvocatoriaImpl implements ConvocatoriaDAO {

    @Override
    public int insertar(Convocatoria conv) {
        String sql = "{call INSERTAR_CONVOCATORIA(?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo", conv.getCodigoConvocatoria());
                cs.setString("_nombre", conv.getNombre());
                cs.setString("_periodo", conv.getPeriodo());
                cs.setDate("_fecha_inicio", conv.getFechaInicio() != null ? Date.valueOf(conv.getFechaInicio()) : null);
                cs.setDate("_fecha_fin", conv.getFechaFin() != null ? Date.valueOf(conv.getFechaFin()) : null);
                cs.setString("_estado", conv.getEstado() != null ? conv.getEstado().name() : "BORRADOR");
                cs.setString("_descripcion", conv.getDescripcion());
                cs.executeUpdate();
                conv.setId(cs.getInt("_id"));
                return conv.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR CONVOCATORIA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Convocatoria conv) {
        String sql = "{call MODIFICAR_CONVOCATORIA(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", conv.getId());
            cs.setString("_codigo", conv.getCodigoConvocatoria());
            cs.setString("_nombre", conv.getNombre());
            cs.setString("_periodo", conv.getPeriodo());
            cs.setDate("_fecha_inicio", conv.getFechaInicio() != null ? Date.valueOf(conv.getFechaInicio()) : null);
            cs.setDate("_fecha_fin", conv.getFechaFin() != null ? Date.valueOf(conv.getFechaFin()) : null);
            cs.setString("_estado", conv.getEstado() != null ? conv.getEstado().name() : "BORRADOR");
            cs.setString("_descripcion", conv.getDescripcion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR CONVOCATORIA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idConv) {
        String sql = "{call ELIMINAR_CONVOCATORIA(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idConv);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR CONVOCATORIA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Convocatoria buscarPorId(int idConv) {
        Convocatoria conv = null;
        String sql = "{call LISTAR_CONVOCATORIA_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idConv);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    conv = new Convocatoria();
                    conv.setId(rs.getInt("id"));
                    conv.setCodigoConvocatoria(rs.getString("codigo_convocatoria"));
                    conv.setNombre(rs.getString("nombre"));
                    conv.setPeriodo(rs.getString("periodo"));
                    if (rs.getDate("fecha_inicio") != null) conv.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                    if (rs.getDate("fecha_fin") != null) conv.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
                    conv.setEstado(EstadoConvocatoria.valueOf(rs.getString("estado")));
                    conv.setDescripcion(rs.getString("descripcion"));
                    conv.setActivo(rs.getBoolean("activo"));
                }
            }
            return conv;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR CONVOCATORIA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Convocatoria> listarTodos() {
        List<Convocatoria> lista = null;
        String sql = "{call LISTAR_CONVOCATORIAS_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (lista == null) lista = new ArrayList<>();
                Convocatoria conv = new Convocatoria();
                conv.setId(rs.getInt("id"));
                conv.setCodigoConvocatoria(rs.getString("codigo_convocatoria"));
                conv.setNombre(rs.getString("nombre"));
                conv.setPeriodo(rs.getString("periodo"));
                if (rs.getDate("fecha_inicio") != null) conv.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
                if (rs.getDate("fecha_fin") != null) conv.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
                conv.setEstado(EstadoConvocatoria.valueOf(rs.getString("estado")));
                conv.setDescripcion(rs.getString("descripcion"));
                conv.setActivo(rs.getBoolean("activo"));
                lista.add(conv);
            }
            return lista;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR CONVOCATORIAS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
