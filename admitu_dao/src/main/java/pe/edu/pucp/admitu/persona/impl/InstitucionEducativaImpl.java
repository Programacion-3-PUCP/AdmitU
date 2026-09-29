package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.InstitucionEducativa;
import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.TipoInstitucion;
import pe.edu.pucp.admitu.persona.dao.InstitucionEducativaDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstitucionEducativaImpl implements InstitucionEducativaDAO {

    @Override
    public int insertar(InstitucionEducativa institucion) {
        String sql = "{call INSERTAR_INSTITUCION_EDUCATIVA(?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_pais_id", institucion.getPais().getId());
                cs.setString("_codigo_externo", institucion.getCodigoExterno());
                cs.setString("_nombre", institucion.getNombre());
                cs.setString("_tipo_institucion", institucion.getTipoInstitucion() != null ? institucion.getTipoInstitucion().name() : null);
                cs.executeUpdate();
                institucion.setId(cs.getInt("_id"));
                return institucion.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR INSTITUCION EDUCATIVA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(InstitucionEducativa institucion) {
        String sql = "{call MODIFICAR_INSTITUCION_EDUCATIVA(?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", institucion.getId());
            cs.setInt("_pais_id", institucion.getPais().getId());
            cs.setString("_codigo_externo", institucion.getCodigoExterno());
            cs.setString("_nombre", institucion.getNombre());
            cs.setString("_tipo_institucion", institucion.getTipoInstitucion() != null ? institucion.getTipoInstitucion().name() : null);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR INSTITUCION EDUCATIVA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idInstitucion) {
        String sql = "{call ELIMINAR_INSTITUCION_EDUCATIVA(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idInstitucion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR INSTITUCION EDUCATIVA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private InstitucionEducativa mapear(ResultSet rs) throws Exception {
        Pais pais = new Pais(rs.getString("codigo_pais"), rs.getString("nombre_pais"));
        pais.setId(rs.getInt("pais_id"));
        InstitucionEducativa institucion = new InstitucionEducativa(pais, rs.getString("codigo_externo"),
            rs.getString("nombre"), rs.getString("tipo_institucion") != null ? TipoInstitucion.valueOf(rs.getString("tipo_institucion")) : null);
        institucion.setId(rs.getInt("id"));
        institucion.setActivo(rs.getBoolean("activo"));
        return institucion;
    }

    @Override
    public InstitucionEducativa buscarPorId(int idInstitucion) {
        InstitucionEducativa institucion = null;
        String sql = "{call LISTAR_INSTITUCION_EDUCATIVA_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idInstitucion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) institucion = mapear(rs);
            }
            return institucion;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR INSTITUCION EDUCATIVA: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<InstitucionEducativa> listarTodos() {
        List<InstitucionEducativa> instituciones = null;
        String sql = "{call LISTAR_INSTITUCIONES_EDUCATIVAS_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (instituciones == null) instituciones = new ArrayList<>();
                instituciones.add(mapear(rs));
            }
            return instituciones;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR INSTITUCIONES EDUCATIVAS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
