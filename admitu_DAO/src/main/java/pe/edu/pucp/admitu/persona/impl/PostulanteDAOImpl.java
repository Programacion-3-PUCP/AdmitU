package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.persona.*;
import pe.edu.pucp.admitu.persona.dao.ApoderadoDAO;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulanteDAOImpl implements PostulanteDAO {

    private final ApoderadoDAO apoderadoDAO = new ApoderadoDAOImpl();

    @Override
    public boolean insertar(Postulante obj) {
        String sql = "{call sp_postulante_insertar(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, obj.getNombres());
            cs.setString(2, obj.getApellidoPaterno());
            cs.setString(3, obj.getApellidoMaterno());
            cs.setString(4, obj.getCorreo());
            cs.setString(5, obj.getTipoDocumento().name());
            cs.setString(6, obj.getNumeroDocumento());
            cs.setString(7, obj.getTelefono());
            if (obj.getApoderado() != null) cs.setInt(8, obj.getApoderado().getId());
            else cs.setNull(8, Types.INTEGER);
            cs.setDate(9, obj.getFechaNacimiento() != null ? Date.valueOf(obj.getFechaNacimiento()) : null);
            cs.setBoolean(10, obj.isTieneDiscapacidad());
            cs.setString(11, obj.getNumeroCarnetConadis());
            cs.setBoolean(12, obj.isCorreoValidado());
            cs.setDate(13, obj.getFechaValidacionCorreo() != null ? Date.valueOf(obj.getFechaValidacionCorreo()) : null);
            cs.registerOutParameter(14, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(14));
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al insertar Postulante: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizar(Postulante obj) {
        String sql = "{call sp_postulante_actualizar(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, obj.getId());
            cs.setString(2, obj.getNombres());
            cs.setString(3, obj.getApellidoPaterno());
            cs.setString(4, obj.getApellidoMaterno());
            cs.setString(5, obj.getCorreo());
            cs.setString(6, obj.getTipoDocumento().name());
            cs.setString(7, obj.getNumeroDocumento());
            cs.setString(8, obj.getTelefono());
            if (obj.getApoderado() != null) cs.setInt(9, obj.getApoderado().getId());
            else cs.setNull(9, Types.INTEGER);
            cs.setDate(10, obj.getFechaNacimiento() != null ? Date.valueOf(obj.getFechaNacimiento()) : null);
            cs.setBoolean(11, obj.isTieneDiscapacidad());
            cs.setString(12, obj.getNumeroCarnetConadis());
            cs.setBoolean(13, obj.isCorreoValidado());
            cs.setDate(14, obj.getFechaValidacionCorreo() != null ? Date.valueOf(obj.getFechaValidacionCorreo()) : null);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al actualizar Postulante: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "{call sp_postulante_eliminar(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al eliminar Postulante: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public Postulante buscarPorId(int id) {
        String sql = "{call sp_postulante_buscar_por_id(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al buscar Postulante: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public List<Postulante> listar() {
        List<Postulante> lista = new ArrayList<>();
        String sql = "{call sp_postulante_listar()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) {
            System.out.println("ERROR al listar Postulante: " + ex.getMessage());
        }
        return lista;
    }

    private Postulante mapear(ResultSet rs) throws SQLException {
        Apoderado apoderado = rs.getObject("id_apoderado") != null
                ? apoderadoDAO.buscarPorId(rs.getInt("id_apoderado")) : null;
        Date fn = rs.getDate("fecha_nacimiento");
        Date fvc = rs.getDate("fecha_validacion_correo");
        Postulante p = new Postulante(
                rs.getString("nombres"), rs.getString("apellido_paterno"), rs.getString("apellido_materno"),
                rs.getString("correo"), TipoDocumento.valueOf(rs.getString("tipo_documento")),
                rs.getString("numero_documento"), rs.getString("telefono"), apoderado,
                fn != null ? fn.toLocalDate() : null, rs.getBoolean("tiene_discapacidad"),
                rs.getString("numero_carnet_conadis"), rs.getBoolean("correo_validado"),
                fvc != null ? fvc.toLocalDate() : null, null, null
        );
        p.setId(rs.getInt("id"));
        return p;
    }
}
