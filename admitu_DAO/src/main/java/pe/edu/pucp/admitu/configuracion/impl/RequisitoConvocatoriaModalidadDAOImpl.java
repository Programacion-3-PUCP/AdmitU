package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.*;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoConvocatoriaModalidadDAO;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoDAO;
import java.sql.*;
import java.util.*;

public class RequisitoConvocatoriaModalidadDAOImpl implements RequisitoConvocatoriaModalidadDAO {
    private final ConvocatoriaModalidadDAOImpl convModDAO = new ConvocatoriaModalidadDAOImpl();
    private final RequisitoDAO requisitoDAO = new RequisitoDAOImpl();

    @Override
    public boolean insertar(RequisitoConvocatoriaModalidad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_reqconvmod_insertar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getConvocatoriaModalidad().getId());
            cs.setInt(2, obj.getRequisito().getId());
            cs.setBoolean(3, obj.isObligatorio());
            cs.setInt(4, obj.getOrdenPresentacion());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(5)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar RequisitoConvocatoriaModalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(RequisitoConvocatoriaModalidad obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_reqconvmod_actualizar(?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setBoolean(2, obj.isObligatorio()); cs.setInt(3, obj.getOrdenPresentacion());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar RequisitoConvocatoriaModalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_reqconvmod_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar RequisitoConvocatoriaModalidad: " + ex.getMessage()); }
        return false;
    }
    @Override
    public RequisitoConvocatoriaModalidad buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_reqconvmod_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar RequisitoConvocatoriaModalidad: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<RequisitoConvocatoriaModalidad> listar() { throw new UnsupportedOperationException("Usar listarPorConvocatoriaModalidad(id)"); }
    @Override
    public List<RequisitoConvocatoriaModalidad> listarPorConvocatoriaModalidad(int idConvocatoriaModalidad) {
        List<RequisitoConvocatoriaModalidad> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_reqconvmod_listar_por_convmod(?)}")) {
            cs.setInt(1, idConvocatoriaModalidad);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar RequisitoConvocatoriaModalidad: " + ex.getMessage()); }
        return lista;
    }
    private RequisitoConvocatoriaModalidad mapear(ResultSet rs) throws SQLException {
        ConvocatoriaModalidad cm = convModDAO.buscarPorId(rs.getInt("id_convocatoria_modalidad"));
        Requisito req = requisitoDAO.buscarPorId(rs.getInt("id_requisito"));
        RequisitoConvocatoriaModalidad r = new RequisitoConvocatoriaModalidad(cm, req, rs.getBoolean("obligatorio"), rs.getInt("orden_presentacion"));
        r.setId(rs.getInt("id"));
        return r;
    }
}
