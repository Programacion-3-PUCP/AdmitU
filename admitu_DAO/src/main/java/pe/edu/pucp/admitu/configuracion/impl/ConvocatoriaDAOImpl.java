package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.EstadoConvocatoria;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaDAO;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ConvocatoriaDAOImpl implements ConvocatoriaDAO {
    @Override
    public boolean insertar(Convocatoria obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convocatoria_insertar(?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, obj.getCodigoConvocatoria()); cs.setString(2, obj.getNombre()); cs.setString(3, obj.getPeriodo());
            cs.setDate(4, obj.getFechaInicio() != null ? Date.valueOf(obj.getFechaInicio()) : null);
            cs.setDate(5, obj.getFechaFin() != null ? Date.valueOf(obj.getFechaFin()) : null);
            cs.setString(6, obj.getEstado().name()); cs.setString(7, obj.getDescripcion());
            cs.registerOutParameter(8, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(8)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Convocatoria: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Convocatoria obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convocatoria_actualizar(?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setString(2, obj.getCodigoConvocatoria()); cs.setString(3, obj.getNombre()); cs.setString(4, obj.getPeriodo());
            cs.setDate(5, obj.getFechaInicio() != null ? Date.valueOf(obj.getFechaInicio()) : null);
            cs.setDate(6, obj.getFechaFin() != null ? Date.valueOf(obj.getFechaFin()) : null);
            cs.setString(7, obj.getEstado().name()); cs.setString(8, obj.getDescripcion());
            cs.setBoolean(9, obj.isActivo());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Convocatoria: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convocatoria_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Convocatoria: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Convocatoria buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convocatoria_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Convocatoria: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Convocatoria> listar() {
        List<Convocatoria> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_convocatoria_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar Convocatoria: " + ex.getMessage()); }
        return lista;
    }
    // Nota: las listas (postulantes, modalidadesHabilitadas, carrerasOfrecidas, etapas) no se cargan aquí;
    // se obtienen con ConvocatoriaModalidadDAO/OfertaCarreraDAO/ConvocatoriaEtapaDAO por id de convocatoria,
    // y se setean con obj.setModalidadesHabilitadas(...), etc., desde la capa de negocio.
    private Convocatoria mapear(ResultSet rs) throws SQLException {
        Date fi = rs.getDate("fecha_inicio"), ff = rs.getDate("fecha_fin");
        Convocatoria c = new Convocatoria();
        c.setId(0); // el id real se fija abajo, ver nota
        c.setCodigoConvocatoria(rs.getString("codigo_convocatoria"));
        c.setNombre(rs.getString("nombre"));
        c.setPeriodo(rs.getString("periodo"));
        c.setFechaInicio(fi != null ? fi.toLocalDate() : null);
        c.setFechaFin(ff != null ? ff.toLocalDate() : null);
        c.setEstado(EstadoConvocatoria.valueOf(rs.getString("estado")));
        c.setDescripcion(rs.getString("descripcion"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
