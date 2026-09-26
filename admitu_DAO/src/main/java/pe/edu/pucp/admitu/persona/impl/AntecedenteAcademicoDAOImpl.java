package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.persona.*;
import pe.edu.pucp.admitu.persona.dao.AntecedenteAcademicoDAO;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AntecedenteAcademicoDAOImpl implements AntecedenteAcademicoDAO {

    private final PostulanteDAO postulanteDAO = new PostulanteDAOImpl();
   private final InstitucionEducativaDAOImpl institucionDAO = new InstitucionEducativaDAOImpl();

    @Override
    public boolean insertar(AntecedenteAcademico obj) {
        String sql = "{call sp_antecedente_insertar(?,?,?,?,?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, obj.getPostulante().getId());
            cs.setInt(2, obj.getInstitucion().getId());
            cs.setInt(3, obj.getAnioInicio());
            cs.setInt(4, obj.getAnioFin());
            cs.setString(5, obj.getDescripcion());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(6));
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al insertar AntecedenteAcademico: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizar(AntecedenteAcademico obj) {
        String sql = "{call sp_antecedente_actualizar(?,?,?,?,?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, obj.getId());
            cs.setInt(2, obj.getInstitucion().getId());
            cs.setInt(3, obj.getAnioInicio());
            cs.setInt(4, obj.getAnioFin());
            cs.setString(5, obj.getDescripcion());
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al actualizar AntecedenteAcademico: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "{call sp_antecedente_eliminar(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            System.out.println("ERROR al eliminar AntecedenteAcademico: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public AntecedenteAcademico buscarPorId(int id) {
        String sql = "{call sp_antecedente_buscar_por_id(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al buscar AntecedenteAcademico: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public List<AntecedenteAcademico> listar() {
        return listarPorPostulante(0); // no aplica sin filtro; se usa el método específico
    }

    @Override
    public List<AntecedenteAcademico> listarPorPostulante(int idPostulante) {
        List<AntecedenteAcademico> lista = new ArrayList<>();
        String sql = "{call sp_antecedente_listar_por_postulante(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, idPostulante);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            System.out.println("ERROR al listar AntecedenteAcademico: " + ex.getMessage());
        }
        return lista;
    }

    private AntecedenteAcademico mapear(ResultSet rs) throws SQLException {
        Postulante postulante = postulanteDAO.buscarPorId(rs.getInt("id_postulante"));
        InstitucionEducativa institucion = institucionDAO.buscarPorId(rs.getInt("id_institucion"));
        AntecedenteAcademico a = new AntecedenteAcademico(
                postulante, institucion, rs.getInt("anio_inicio"), rs.getInt("anio_fin"), rs.getString("descripcion")
        );
        a.setId(rs.getInt("id"));
        a.setActivo(rs.getBoolean("activo"));
        return a;
    }
}
