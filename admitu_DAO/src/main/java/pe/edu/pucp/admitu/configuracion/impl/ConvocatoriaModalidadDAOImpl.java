package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaModalidadDAO;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaDAO;
import pe.edu.pucp.admitu.configuracion.dao.ModalidadDAO;
import java.sql.*;
import java.util.*;

public class ConvocatoriaModalidadDAOImpl implements ConvocatoriaModalidadDAO {
    private final ConvocatoriaDAO convocatoriaDAO = new ConvocatoriaDAOImpl();
    private final ModalidadDAO modalidadDAO = new ModalidadDAOImpl();

    @Override
    public boolean insertar(ConvocatoriaModalidad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convmod_insertar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getConvocatoria().getId());
            cs.setInt(2, obj.getModalidad().getId());
            cs.setDouble(3, obj.getCostoInscripcion());
            cs.setString(4, obj.getObservacion());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(5)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar ConvocatoriaModalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(ConvocatoriaModalidad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convmod_actualizar(?,?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setInt(2, obj.getModalidad().getId());
            cs.setDouble(3, obj.getCostoInscripcion()); cs.setString(4, obj.getObservacion());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar ConvocatoriaModalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convmod_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar ConvocatoriaModalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public ConvocatoriaModalidad buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convmod_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar ConvocatoriaModalidad: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<ConvocatoriaModalidad> listar() { throw new UnsupportedOperationException("Usar listarPorConvocatoria(idConvocatoria)"); }
    @Override
    public List<ConvocatoriaModalidad> listarPorConvocatoria(int idConvocatoria) {
        List<ConvocatoriaModalidad> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convmod_listar_por_convocatoria(?)}")) {
            cs.setInt(1, idConvocatoria);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar ConvocatoriaModalidad: " + ex.getMessage()); }
        return lista;
    }
    private ConvocatoriaModalidad mapear(ResultSet rs) throws SQLException {
        Convocatoria conv = convocatoriaDAO.buscarPorId(rs.getInt("id_convocatoria"));
        Modalidad mod = modalidadDAO.buscarPorId(rs.getInt("id_modalidad"));
        ConvocatoriaModalidad cm = new ConvocatoriaModalidad(conv, mod, rs.getDouble("costo_inscripcion"), rs.getString("observacion"), null);
        cm.setId(rs.getInt("id"));
        return cm;
    }
}