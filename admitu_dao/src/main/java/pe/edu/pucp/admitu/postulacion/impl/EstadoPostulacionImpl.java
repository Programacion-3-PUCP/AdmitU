package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.dao.EstadoPostulacionDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstadoPostulacionImpl implements EstadoPostulacionDAO {

    @Override
    public int insertar(EstadoPostulacion estado) {
        String sql = "{call INSERTAR_ESTADO_POSTULACION(?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo", estado.getCodigo());
                cs.setString("_nombre", estado.getNombre());
                cs.setString("_descripcion", estado.getDescripcion());
                cs.executeUpdate();
                estado.setId(cs.getInt("_id"));
                return estado.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR ESTADO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(EstadoPostulacion estado) {
        String sql = "{call MODIFICAR_ESTADO_POSTULACION(?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", estado.getId());
            cs.setString("_codigo", estado.getCodigo());
            cs.setString("_nombre", estado.getNombre());
            cs.setString("_descripcion", estado.getDescripcion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR ESTADO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idEstado) {
        String sql = "{call ELIMINAR_ESTADO_POSTULACION(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idEstado);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR ESTADO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private EstadoPostulacion mapear(ResultSet rs) throws Exception {
        EstadoPostulacion estado = new EstadoPostulacion(rs.getString("codigo"), rs.getString("nombre"),
            rs.getString("descripcion"));
        estado.setId(rs.getInt("id"));
        return estado;
    }

    @Override
    public EstadoPostulacion buscarPorId(int idEstado) {
        EstadoPostulacion estado = null;
        String sql = "{call LISTAR_ESTADO_POSTULACION_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idEstado);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) estado = mapear(rs);
            }
            return estado;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR ESTADO POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<EstadoPostulacion> listarTodos() {
        List<EstadoPostulacion> estados = null;
        String sql = "{call LISTAR_ESTADOS_POSTULACION_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (estados == null) estados = new ArrayList<>();
                estados.add(mapear(rs));
            }
            return estados;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR ESTADOS POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
