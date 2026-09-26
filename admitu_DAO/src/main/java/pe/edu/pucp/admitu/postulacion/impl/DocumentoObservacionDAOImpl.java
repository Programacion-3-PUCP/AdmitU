package pe.edu.pucp.admitu.postulacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.dao.EvaluadorDAO;
import pe.edu.pucp.admitu.persona.impl.EvaluadorDAOImpl;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoObservacionDAO;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoPostulacionDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DocumentoObservacionDAOImpl implements DocumentoObservacionDAO {
    private final DocumentoPostulacionDAO documentoDAO = new DocumentoPostulacionDAOImpl();
    private final EvaluadorDAO evaluadorDAO = new EvaluadorDAOImpl();

    @Override
    public boolean insertar(DocumentoObservacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_docobs_insertar(?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getDocumento().getId());
            cs.setInt(2, obj.getEvaluador().getId());
            cs.setString(3, obj.getTipoObservacion().name());
            cs.setString(4, obj.getDescripcion());
            cs.setDate(5, obj.getFechaObservacion() != null ? Date.valueOf(obj.getFechaObservacion()) : null);
            cs.setString(6, obj.getEstadoObservacion().name());
            cs.setDate(7, obj.getFechaSubsanacion() != null ? Date.valueOf(obj.getFechaSubsanacion()) : null);
            cs.setString(8, obj.getComentarioSubsanacion());
            cs.registerOutParameter(9, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(9)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar DocumentoObservacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(DocumentoObservacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_docobs_actualizar(?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setString(2, obj.getEstadoObservacion().name());
            cs.setDate(3, obj.getFechaSubsanacion() != null ? Date.valueOf(obj.getFechaSubsanacion()) : null);
            cs.setString(4, obj.getComentarioSubsanacion());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar DocumentoObservacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_docobs_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar DocumentoObservacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public DocumentoObservacion buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_docobs_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar DocumentoObservacion: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<DocumentoObservacion> listar() { throw new UnsupportedOperationException("Usar listarPorDocumento(id)"); }
    @Override
    public List<DocumentoObservacion> listarPorDocumento(int idDocumento) {
        List<DocumentoObservacion> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_docobs_listar_por_documento(?)}")) {
            cs.setInt(1, idDocumento);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar DocumentoObservacion: " + ex.getMessage()); }
        return lista;
    }
    private DocumentoObservacion mapear(ResultSet rs) throws SQLException {
        DocumentoPostulacion doc = documentoDAO.buscarPorId(rs.getInt("id_documento"));
        Evaluador ev = evaluadorDAO.buscarPorId(rs.getInt("id_evaluador"));
        Date fo = rs.getDate("fecha_observacion"), fs = rs.getDate("fecha_subsanacion");
        DocumentoObservacion d = new DocumentoObservacion(doc, ev, TipoObservacion.valueOf(rs.getString("tipo_observacion")),
                rs.getString("descripcion"), fo != null ? fo.toLocalDate() : null,
                EstadoObservacion.valueOf(rs.getString("estado_observacion")),
                fs != null ? fs.toLocalDate() : null, rs.getString("comentario_subsanacion"));
        d.setId(rs.getInt("id"));
        return d;
    }
}
