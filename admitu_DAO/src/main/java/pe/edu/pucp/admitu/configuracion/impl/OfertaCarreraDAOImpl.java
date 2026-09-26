package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.configuracion.dao.OfertaCarreraDAO;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaDAO;
import pe.edu.pucp.admitu.configuracion.dao.CarreraDAO;
import java.sql.*;
import java.util.*;

public class OfertaCarreraDAOImpl implements OfertaCarreraDAO {
    private final ConvocatoriaDAO convocatoriaDAO = new ConvocatoriaDAOImpl();
    private final CarreraDAO carreraDAO = new CarreraDAOImpl();

    @Override
    public boolean insertar(OfertaCarrera obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_ofertacarrera_insertar(?,?,?,?)}")) {
            cs.setInt(1, obj.getConvocatoria().getId());
            cs.setInt(2, obj.getCarrera().getId());
            cs.setInt(3, obj.getCantidadVacantes());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(4)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar OfertaCarrera: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(OfertaCarrera obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_ofertacarrera_actualizar(?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setInt(2, obj.getCarrera().getId()); cs.setInt(3, obj.getCantidadVacantes());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar OfertaCarrera: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_ofertacarrera_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar OfertaCarrera: " + ex.getMessage()); }
        return false;
    }
    @Override
    public OfertaCarrera buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_ofertacarrera_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar OfertaCarrera: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<OfertaCarrera> listar() { throw new UnsupportedOperationException("Usar listarPorConvocatoria(idConvocatoria)"); }
    @Override
    public List<OfertaCarrera> listarPorConvocatoria(int idConvocatoria) {
        List<OfertaCarrera> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_ofertacarrera_listar_por_convocatoria(?)}")) {
            cs.setInt(1, idConvocatoria);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar OfertaCarrera: " + ex.getMessage()); }
        return lista;
    }
    private OfertaCarrera mapear(ResultSet rs) throws SQLException {
        Convocatoria conv = convocatoriaDAO.buscarPorId(rs.getInt("id_convocatoria"));
        Carrera car = carreraDAO.buscarPorId(rs.getInt("id_carrera"));
        OfertaCarrera oc = new OfertaCarrera(conv, car, rs.getInt("cantidad_vacantes"));
        oc.setId(rs.getInt("id"));
        return oc;
    }
}
