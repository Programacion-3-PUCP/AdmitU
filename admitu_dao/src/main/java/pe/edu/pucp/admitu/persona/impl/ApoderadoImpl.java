package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Apoderado;
import pe.edu.pucp.admitu.persona.Parentesco;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.persona.dao.ApoderadoDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApoderadoImpl implements ApoderadoDAO {

    @Override
    public int insertar(Apoderado apoderado) {
        String sql = "{call INSERTAR_APODERADO(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_nombres", apoderado.getNombres());
                cs.setString("_ape_paterno", apoderado.getApellidoPaterno());
                cs.setString("_ape_materno", apoderado.getApellidoMaterno());
                cs.setString("_correo", apoderado.getCorreo());
                cs.setString("_tipo_doc", apoderado.getTipoDocumento() != null ? apoderado.getTipoDocumento().name() : "DNI");
                cs.setString("_num_doc", apoderado.getNumeroDocumento());
                cs.setString("_telefono", apoderado.getTelefono());
                cs.setString("_parentesco", apoderado.getParentesco() != null ? apoderado.getParentesco().name() : null);
                cs.executeUpdate();
                apoderado.setId(cs.getInt("_id"));
                return apoderado.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR APODERADO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Apoderado apoderado) {
        String sql = "{call MODIFICAR_APODERADO(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", apoderado.getId());
            cs.setString("_nombres", apoderado.getNombres());
            cs.setString("_ape_paterno", apoderado.getApellidoPaterno());
            cs.setString("_ape_materno", apoderado.getApellidoMaterno());
            cs.setString("_correo", apoderado.getCorreo());
            cs.setString("_telefono", apoderado.getTelefono());
            cs.setString("_parentesco", apoderado.getParentesco() != null ? apoderado.getParentesco().name() : null);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR APODERADO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idApoderado) {
        String sql = "{call ELIMINAR_APODERADO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idApoderado);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR APODERADO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Apoderado mapear(ResultSet rs) throws Exception {
        Apoderado apoderado = new Apoderado(rs.getString("nombres"), rs.getString("apellido_paterno"),
            rs.getString("apellido_materno"), rs.getString("correo"),
            TipoDocumento.valueOf(rs.getString("tipo_documento")), rs.getString("numero_documento"),
            rs.getString("telefono"), rs.getString("parentesco") != null ? Parentesco.valueOf(rs.getString("parentesco")) : null, null);
        apoderado.setId(rs.getInt("id"));
        apoderado.setActivo(rs.getBoolean("activo"));
        return apoderado;
    }

    @Override
    public Apoderado buscarPorId(int idApoderado) {
        Apoderado apoderado = null;
        String sql = "{call LISTAR_APODERADO_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idApoderado);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) apoderado = mapear(rs);
            }
            return apoderado;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR APODERADO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Apoderado> listarTodos() {
        List<Apoderado> apoderados = null;
        String sql = "{call LISTAR_APODERADOS_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (apoderados == null) apoderados = new ArrayList<>();
                apoderados.add(mapear(rs));
            }
            return apoderados;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR APODERADOS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
