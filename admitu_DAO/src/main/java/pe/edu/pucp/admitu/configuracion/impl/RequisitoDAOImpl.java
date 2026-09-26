package pe.edu.pucp.admitu.configuracion.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.TipoArchivo;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoDAO;
import java.sql.*;
import java.util.*;

public class RequisitoDAOImpl implements RequisitoDAO {
    @Override
    public boolean insertar(Requisito obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_requisito_insertar(?,?,?,?,?,?)}")) {
            cs.setString(1, obj.getCodigoRequisito()); cs.setString(2, obj.getNombre()); cs.setString(3, obj.getDescripcion());
            cs.setString(4, obj.getTipoArchivoRequerido().name()); cs.setInt(5, obj.getTamanioMaximoBytes());
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(6)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Requisito: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Requisito obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_requisito_actualizar(?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId()); cs.setString(2, obj.getCodigoRequisito()); cs.setString(3, obj.getNombre());
            cs.setString(4, obj.getDescripcion()); cs.setString(5, obj.getTipoArchivoRequerido().name());
            cs.setInt(6, obj.getTamanioMaximoBytes()); cs.setBoolean(7, obj.isActivo());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Requisito: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_requisito_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Requisito: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Requisito buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_requisito_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Requisito: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Requisito> listar() {
        List<Requisito> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_requisito_listar()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException ex) { System.out.println("ERROR listar Requisito: " + ex.getMessage()); }
        return lista;
    }
    private Requisito mapear(ResultSet rs) throws SQLException {
        Requisito r = new Requisito(rs.getString("codigo_requisito"), rs.getString("nombre"), rs.getString("descripcion"),
                TipoArchivo.valueOf(rs.getString("tipo_archivo_requerido")), rs.getInt("tamanio_maximo_bytes"));
        r.setId(rs.getInt("id"));
        r.setActivo(rs.getBoolean("activo"));
        return r;
    }
}
