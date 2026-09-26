package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaEtapaDAO;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaDAO;
import pe.edu.pucp.admitu.configuracion.dao.EtapaDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ConvocatoriaEtapaDAOImpl implements ConvocatoriaEtapaDAO {
    private final ConvocatoriaDAO convocatoriaDAO = new ConvocatoriaDAOImpl();
    private final EtapaDAO etapaDAO = new EtapaDAOImpl();

    @Override
    public boolean insertar(ConvocatoriaEtapa obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convetapa_insertar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getConvocatoria().getId());
            cs.setInt(2, obj.getEtapa().getId());
            cs.setDate(3, obj.getFechaInicio() != null ? Date.valueOf(obj.getFechaInicio()) : null);
            cs.setDate(4, obj.getFechaFin() != null ? Date.valueOf(obj.getFechaFin()) : null);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(5)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar ConvocatoriaEtapa: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(ConvocatoriaEtapa obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convetapa_actualizar(?,?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setInt(2, obj.getEtapa().getId());
            cs.setDate(3, obj.getFechaInicio() != null ? Date.valueOf(obj.getFechaInicio()) : null);
            cs.setDate(4, obj.getFechaFin() != null ? Date.valueOf(obj.getFechaFin()) : null);
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar ConvocatoriaEtapa: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convetapa_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar ConvocatoriaEtapa: " + ex.getMessage()); }
        return false;
    }
    @Override
    public ConvocatoriaEtapa buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convetapa_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar ConvocatoriaEtapa: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<ConvocatoriaEtapa> listar() { throw new UnsupportedOperationException("Usar listarPorConvocatoria(idConvocatoria)"); }
    @Override
    public List<ConvocatoriaEtapa> listarPorConvocatoria(int idConvocatoria) {
        List<ConvocatoriaEtapa> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convetapa_listar_por_convocatoria(?)}")) {
            cs.setInt(1, idConvocatoria);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar ConvocatoriaEtapa: " + ex.getMessage()); }
        return lista;
    }
    private ConvocatoriaEtapa mapear(ResultSet rs) throws SQLException {
        Convocatoria conv = convocatoriaDAO.buscarPorId(rs.getInt("id_convocatoria"));
        Etapa et = etapaDAO.buscarPorId(rs.getInt("id_etapa"));
        Date fi = rs.getDate("fecha_inicio"), ff = rs.getDate("fecha_fin");
        ConvocatoriaEtapa ce = new ConvocatoriaEtapa(conv, et, fi != null ? fi.toLocalDate() : null, ff != null ? ff.toLocalDate() : null);
        ce.setId(rs.getInt("id"));
        return ce;
    }
}