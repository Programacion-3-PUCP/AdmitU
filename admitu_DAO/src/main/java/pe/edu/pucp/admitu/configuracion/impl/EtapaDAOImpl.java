package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Etapa;
import pe.edu.pucp.admitu.configuracion.dao.EtapaDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class EtapaDAOImpl implements EtapaDAO {
    @Override
    public boolean insertar(Etapa obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_etapa_insertar(?,?,?,?,?,?)}")) {
            cs.setDate(1, obj.getFechaInicio() != null ? Date.valueOf(obj.getFechaInicio()) : null);
            cs.setDate(2, obj.getFechaFin() != null ? Date.valueOf(obj.getFechaFin()) : null);
            cs.setString(3, obj.getCodigoEtapa()); cs.setString(4, obj.getNombre()); cs.setString(5, obj.getDescripcion());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(6)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Etapa: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Etapa obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_etapa_actualizar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setDate(2, obj.getFechaInicio() != null ? Date.valueOf(obj.getFechaInicio()) : null);
            cs.setDate(3, obj.getFechaFin() != null ? Date.valueOf(obj.getFechaFin()) : null);
            cs.setString(4, obj.getCodigoEtapa()); cs.setString(5, obj.getNombre()); cs.setString(6, obj.getDescripcion());
            cs.setBoolean(7, obj.isActivo());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Etapa: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_etapa_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Etapa: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Etapa buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_etapa_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Etapa: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Etapa> listar() {
        List<Etapa> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_etapa_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar Etapa: " + ex.getMessage()); }
        return lista;
    }
    private Etapa mapear(ResultSet rs) throws SQLException {
        Date fi = rs.getDate("fecha_inicio"), ff = rs.getDate("fecha_fin");
        Etapa e = new Etapa(fi != null ? fi.toLocalDate() : null, ff != null ? ff.toLocalDate() : null,
                rs.getString("codigo_etapa"), rs.getString("nombre"), rs.getString("descripcion"));
        e.setId(rs.getInt("id"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }
}
