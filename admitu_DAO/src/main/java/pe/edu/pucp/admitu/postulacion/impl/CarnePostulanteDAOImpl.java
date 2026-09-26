package pe.edu.pucp.admitu.postulacion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.configuracion.dao.SedeDAO;
import pe.edu.pucp.admitu.configuracion.impl.SedeDAOImpl;
import pe.edu.pucp.admitu.postulacion.*;
import pe.edu.pucp.admitu.postulacion.dao.CarnePostulanteDAO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class CarnePostulanteDAOImpl implements CarnePostulanteDAO {
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();
    private final SedeDAO sedeDAO = new SedeDAOImpl();

    @Override
    public boolean insertar(CarnePostulante obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carne_insertar(?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPostulacion().getId());
            cs.setInt(2, obj.getSede().getId());
            cs.setString(3, obj.getCodigoCarne());
            cs.setDate(4, obj.getFechaGeneracion() != null ? Date.valueOf(obj.getFechaGeneracion()) : null);
            cs.setDate(5, obj.getFechaInicioVigencia() != null ? Date.valueOf(obj.getFechaInicioVigencia()) : null);
            cs.setDate(6, obj.getFechaFinVigencia() != null ? Date.valueOf(obj.getFechaFinVigencia()) : null);
            cs.setString(7, obj.getAulaExamen());
            cs.registerOutParameter(8, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(8)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar CarnePostulante: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(CarnePostulante obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carne_actualizar(?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setInt(2, obj.getSede().getId()); cs.setString(3, obj.getAulaExamen());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar CarnePostulante: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carne_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar CarnePostulante: " + ex.getMessage()); }
        return false;
    }
    @Override
    public CarnePostulante buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carne_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar CarnePostulante: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<CarnePostulante> listar() { throw new UnsupportedOperationException("Usar buscarPorPostulacion(id)"); }
    @Override
    public CarnePostulante buscarPorPostulacion(int idPostulacion) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carne_buscar_por_postulacion(?)}")) {
            cs.setInt(1, idPostulacion);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar CarnePostulante por postulacion: " + ex.getMessage()); }
        return null;
    }
    private CarnePostulante mapear(ResultSet rs) throws SQLException {
        Postulacion postulacion = postulacionDAO.buscarPorId(rs.getInt("id_postulacion"));
        Sede sede = sedeDAO.buscarPorId(rs.getInt("id_sede"));
        Date fg = rs.getDate("fecha_generacion"), fiv = rs.getDate("fecha_inicio_vigencia"), ffv = rs.getDate("fecha_fin_vigencia");
        CarnePostulante c = new CarnePostulante(postulacion, sede, rs.getString("codigo_carne"),
                fg != null ? fg.toLocalDate() : null, fiv != null ? fiv.toLocalDate() : null,
                ffv != null ? ffv.toLocalDate() : null, rs.getString("aula_examen"));
        c.setId(rs.getInt("id"));
        return c;
    }
}