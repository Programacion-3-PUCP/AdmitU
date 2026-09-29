package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;
import pe.edu.pucp.admitu.persona.InstitucionEducativa;
import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.persona.TipoInstitucion;
import pe.edu.pucp.admitu.persona.dao.AntecedenteAcademicoDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AntecedenteAcademicoImpl implements AntecedenteAcademicoDAO {

    @Override
    public int insertar(AntecedenteAcademico antecedente) {
        String sql = "{call INSERTAR_ANTECEDENTE_ACADEMICO(?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulante_id", antecedente.getPostulante().getId());
                cs.setInt("_institucion_id", antecedente.getInstitucion().getId());
                cs.setInt("_anio_inicio", antecedente.getAnioInicio());
                cs.setInt("_anio_fin", antecedente.getAnioFin());
                cs.setString("_descripcion", antecedente.getDescripcion());
                cs.executeUpdate();
                antecedente.setId(cs.getInt("_id"));
                return antecedente.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR ANTECEDENTE ACADEMICO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(AntecedenteAcademico antecedente) {
        String sql = "{call MODIFICAR_ANTECEDENTE_ACADEMICO(?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", antecedente.getId());
            cs.setInt("_postulante_id", antecedente.getPostulante().getId());
            cs.setInt("_institucion_id", antecedente.getInstitucion().getId());
            cs.setInt("_anio_inicio", antecedente.getAnioInicio());
            cs.setInt("_anio_fin", antecedente.getAnioFin());
            cs.setString("_descripcion", antecedente.getDescripcion());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR ANTECEDENTE ACADEMICO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idAntecedente) {
        String sql = "{call ELIMINAR_ANTECEDENTE_ACADEMICO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idAntecedente);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR ANTECEDENTE ACADEMICO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private AntecedenteAcademico mapear(ResultSet rs) throws Exception {
        Postulante postulante = new Postulante("TMP", "TMP", null, "tmp@tmp.com", TipoDocumento.DNI,
            "00000000", null, null, null, false, null, false, null, null, null);
        postulante.setId(rs.getInt("postulante_id"));
        Pais pais = new Pais("TMP", rs.getString("nombre_pais"));
        pais.setId(rs.getInt("pais_id"));
        InstitucionEducativa institucion = new InstitucionEducativa(pais, rs.getString("codigo_institucion"),
            rs.getString("nombre_institucion"), rs.getString("tipo_institucion") != null ? TipoInstitucion.valueOf(rs.getString("tipo_institucion")) : null);
        AntecedenteAcademico antecedente = new AntecedenteAcademico(postulante, institucion,
            rs.getInt("anio_inicio"), rs.getInt("anio_fin"), rs.getString("descripcion"));
        institucion.setId(rs.getInt("institucion_id"));
        antecedente.setId(rs.getInt("id"));
        antecedente.setActivo(rs.getBoolean("activo"));
        return antecedente;
    }

    @Override
    public AntecedenteAcademico buscarPorId(int idAntecedente) {
        AntecedenteAcademico antecedente = null;
        String sql = "{call LISTAR_ANTECEDENTE_ACADEMICO_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idAntecedente);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) antecedente = mapear(rs);
            }
            return antecedente;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR ANTECEDENTE ACADEMICO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<AntecedenteAcademico> listarTodos() {
        List<AntecedenteAcademico> antecedentes = null;
        String sql = "{call LISTAR_ANTECEDENTES_ACADEMICOS_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (antecedentes == null) antecedentes = new ArrayList<>();
                antecedentes.add(mapear(rs));
            }
            return antecedentes;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR ANTECEDENTES ACADEMICOS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
