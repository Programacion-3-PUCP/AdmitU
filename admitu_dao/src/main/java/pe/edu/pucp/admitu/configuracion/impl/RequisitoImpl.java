package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequisitoImpl implements RequisitoDAO {

    @Override
    public int insertar(Requisito requisito) {
        String sql = "{call INSERTAR_REQUISITO(?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_codigo_requisito", requisito.getCodigoRequisito());
                cs.setString("_nombre", requisito.getNombre());
                cs.setString("_descripcion", requisito.getDescripcion());
                cs.setString("_tipo_archivo", requisito.getTipoArchivoRequerido() != null ? requisito.getTipoArchivoRequerido().name() : null);
                cs.setInt("_tamanio_maximo", requisito.getTamanioMaximoBytes());
                cs.executeUpdate();
                requisito.setId(cs.getInt("_id"));
                return requisito.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR REQUISITO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Requisito requisito) {
        String sql = "{call MODIFICAR_REQUISITO(?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", requisito.getId());
            cs.setString("_codigo_requisito", requisito.getCodigoRequisito());
            cs.setString("_nombre", requisito.getNombre());
            cs.setString("_descripcion", requisito.getDescripcion());
            cs.setString("_tipo_archivo", requisito.getTipoArchivoRequerido() != null ? requisito.getTipoArchivoRequerido().name() : null);
            cs.setInt("_tamanio_maximo", requisito.getTamanioMaximoBytes());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR REQUISITO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idRequisito) {
        String sql = "{call ELIMINAR_REQUISITO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRequisito);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR REQUISITO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Requisito mapear(ResultSet rs) throws Exception {
        Requisito requisito = new Requisito(rs.getString("codigo_requisito"), rs.getString("nombre"),
            rs.getString("descripcion"), rs.getString("tipo_archivo_requerido") != null ? pe.edu.pucp.admitu.configuracion.TipoArchivo.valueOf(rs.getString("tipo_archivo_requerido")) : null,
            rs.getInt("tamanio_maximo_bytes"));
        requisito.setId(rs.getInt("id"));
        requisito.setActivo(rs.getBoolean("activo"));
        return requisito;
    }

    @Override
    public Requisito buscarPorId(int idRequisito) {
        Requisito requisito = null;
        String sql = "{call LISTAR_REQUISITO_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idRequisito);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) requisito = mapear(rs);
            }
            return requisito;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR REQUISITO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Requisito> listarTodos() {
        List<Requisito> requisitos = null;
        String sql = "{call LISTAR_REQUISITOS_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (requisitos == null) requisitos = new ArrayList<>();
                requisitos.add(mapear(rs));
            }
            return requisitos;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR REQUISITOS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
