package pe.edu.pucp.admitu.persona.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.persona.dao.EvaluadorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluadorImpl implements EvaluadorDAO {

    @Override
    public int insertar(Evaluador evaluador) {
        String sql = "{call INSERTAR_EVALUADOR(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setString("_nombres", evaluador.getNombres());
                cs.setString("_ape_paterno", evaluador.getApellidoPaterno());
                cs.setString("_ape_materno", evaluador.getApellidoMaterno());
                cs.setString("_correo", evaluador.getCorreo());
                cs.setString("_tipo_doc", evaluador.getTipoDocumento() != null ? evaluador.getTipoDocumento().name() : "DNI");
                cs.setString("_num_doc", evaluador.getNumeroDocumento());
                cs.setString("_telefono", evaluador.getTelefono());
                cs.setString("_cargo", evaluador.getCargo());
                cs.executeUpdate();
                evaluador.setId(cs.getInt("_id"));
                return evaluador.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR EVALUADOR: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Evaluador evaluador) {
        String sql = "{call MODIFICAR_EVALUADOR(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", evaluador.getId());
            cs.setString("_nombres", evaluador.getNombres());
            cs.setString("_ape_paterno", evaluador.getApellidoPaterno());
            cs.setString("_ape_materno", evaluador.getApellidoMaterno());
            cs.setString("_correo", evaluador.getCorreo());
            cs.setString("_telefono", evaluador.getTelefono());
            cs.setString("_cargo", evaluador.getCargo());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR EVALUADOR: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idEvaluador) {
        String sql = "{call ELIMINAR_EVALUADOR(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idEvaluador);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR EVALUADOR: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Evaluador mapear(ResultSet rs) throws Exception {
        Evaluador evaluador = new Evaluador(rs.getString("nombres"), rs.getString("apellido_paterno"),
            rs.getString("apellido_materno"), rs.getString("correo"),
            TipoDocumento.valueOf(rs.getString("tipo_documento")), rs.getString("numero_documento"),
            rs.getString("telefono"), rs.getString("cargo"));
        evaluador.setId(rs.getInt("id"));
        evaluador.setActivo(rs.getBoolean("activo"));
        return evaluador;
    }

    @Override
    public Evaluador buscarPorId(int idEvaluador) {
        Evaluador evaluador = null;
        String sql = "{call LISTAR_EVALUADOR_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idEvaluador);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) evaluador = mapear(rs);
            }
            return evaluador;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR EVALUADOR: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Evaluador> listarTodos() {
        List<Evaluador> evaluadores = null;
        String sql = "{call LISTAR_EVALUADORES_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (evaluadores == null) evaluadores = new ArrayList<>();
                evaluadores.add(mapear(rs));
            }
            return evaluadores;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR EVALUADORES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
