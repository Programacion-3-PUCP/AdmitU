package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.TipoDocumento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulanteImpl implements PostulanteDAO {

    @Override
    public int insertar(Postulante post) {
        String sql = "{call INSERTAR_POSTULANTE(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_nombres", post.getNombres());
                cs.setString("_ape_paterno", post.getApellidoPaterno());
                cs.setString("_ape_materno", post.getApellidoMaterno());
                cs.setString("_correo", post.getCorreo());
                cs.setString("_tipo_doc", post.getTipoDocumento() != null ? post.getTipoDocumento().name() : "DNI");
                cs.setString("_num_doc", post.getNumeroDocumento());
                cs.setString("_telefono", post.getTelefono());
                cs.setDate("_fecha_nac", post.getFechaNacimiento() != null ? Date.valueOf(post.getFechaNacimiento()) : null);
                cs.executeUpdate();
                post.setId(cs.getInt("_id"));
                return post.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Postulante post) {
        String sql = "{call MODIFICAR_POSTULANTE(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", post.getId());
            cs.setString("_nombres", post.getNombres());
            cs.setString("_ape_paterno", post.getApellidoPaterno());
            cs.setString("_ape_materno", post.getApellidoMaterno());
            cs.setString("_correo", post.getCorreo());
            cs.setString("_telefono", post.getTelefono());
            cs.setDate("_fecha_nac", post.getFechaNacimiento() != null ? Date.valueOf(post.getFechaNacimiento()) : null);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idPostulante) {
        String sql = "{call ELIMINAR_POSTULANTE(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPostulante);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Postulante buscarPorId(int idPostulante) {
        Postulante post = null;
        String sql = "{call LISTAR_POSTULANTE_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPostulante);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    post = new Postulante(
                        rs.getString("nombres"), rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"), rs.getString("correo"),
                        TipoDocumento.valueOf(rs.getString("tipo_documento")),
                        rs.getString("numero_documento"), rs.getString("telefono"),
                        null,
                        rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null,
                        false, null, false, null, null, null);
                    post.setId(rs.getInt("id"));
                }
            }
            return post;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR POSTULANTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Postulante> listarTodos() {
        List<Postulante> lista = null;
        String sql = "{call LISTAR_POSTULANTES_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (lista == null) lista = new ArrayList<>();
                Postulante post = new Postulante(
                    rs.getString("nombres"), rs.getString("apellido_paterno"),
                    rs.getString("apellido_materno"), rs.getString("correo"),
                    TipoDocumento.valueOf(rs.getString("tipo_documento")),
                    rs.getString("numero_documento"), rs.getString("telefono"),
                    null,
                    rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null,
                    false, null, false, null, null, null);
                post.setId(rs.getInt("id"));
                lista.add(post);
            }
            return lista;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR POSTULANTES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
