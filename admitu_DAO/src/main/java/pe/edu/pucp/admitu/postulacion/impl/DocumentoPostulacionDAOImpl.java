package pe.edu.pucp.admitu.postulacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.TipoArchivo;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoDAO;
import pe.edu.pucp.admitu.configuracion.impl.RequisitoDAOImpl;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.dao.DocumentoPostulacionDAO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DocumentoPostulacionDAOImpl implements DocumentoPostulacionDAO {
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final RequisitoDAO requisitoDAO = new RequisitoDAOImpl();

    @Override
    public boolean insertar(DocumentoPostulacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_documento_insertar(?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPostulacion().getId());
            cs.setInt(2, obj.getRequisitoAplicable().getId());
            cs.setInt(3, obj.getNumeroVersion());
            cs.setString(4, obj.getNombreArchivo());
            cs.setString(5, obj.getTipoArchivo().name());
            cs.setLong(6, obj.getTamanioArchivo());
            cs.setString(7, obj.getRutaArchivo());
            cs.setDate(8, obj.getFechaCarga() != null ? Date.valueOf(obj.getFechaCarga()) : null);
            cs.setString(9, obj.getEstadoDocumento().name());
            cs.setDate(10, obj.getFechaEvaluacion() != null ? Date.valueOf(obj.getFechaEvaluacion()) : null);
            cs.setString(11, obj.getComentarioEvaluacion());
            cs.registerOutParameter(12, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(12)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar DocumentoPostulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(DocumentoPostulacion obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_documento_actualizar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setInt(2, obj.getNumeroVersion());
            cs.setString(3, obj.getEstadoDocumento().name());
            cs.setDate(4, obj.getFechaEvaluacion() != null ? Date.valueOf(obj.getFechaEvaluacion()) : null);
            cs.setString(5, obj.getComentarioEvaluacion());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar DocumentoPostulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_documento_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar DocumentoPostulacion: " + ex.getMessage()); }
        return false;
    }
    @Override
    public DocumentoPostulacion buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_documento_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar DocumentoPostulacion: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<DocumentoPostulacion> listar() { throw new UnsupportedOperationException("Usar listarPorPostulacion(id)"); }
    @Override
    public List<DocumentoPostulacion> listarPorPostulacion(int idPostulacion) {
        List<DocumentoPostulacion> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_documento_listar_por_postulacion(?)}")) {
            cs.setInt(1, idPostulacion);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar DocumentoPostulacion: " + ex.getMessage()); }
        return lista;
    }
    private DocumentoPostulacion mapear(ResultSet rs) throws SQLException {
        Postulacion postulacion = postulacionDAO.buscarPorId(rs.getInt("id_postulacion"));
        Requisito requisito = requisitoDAO.buscarPorId(rs.getInt("id_requisito"));
        Date fc = rs.getDate("fecha_carga"), fe = rs.getDate("fecha_evaluacion");
        DocumentoPostulacion d = new DocumentoPostulacion(postulacion, requisito, rs.getInt("numero_version"),
                rs.getString("nombre_archivo"), TipoArchivo.valueOf(rs.getString("tipo_archivo")),
                rs.getLong("tamanio_archivo"), rs.getString("ruta_archivo"), fc != null ? fc.toLocalDate() : null,
                EstadoDocumento.valueOf(rs.getString("estado_documento")), fe != null ? fe.toLocalDate() : null,
                rs.getString("comentario_evaluacion"), null);
        d.setId(rs.getInt("id"));
        return d;
    }
}
