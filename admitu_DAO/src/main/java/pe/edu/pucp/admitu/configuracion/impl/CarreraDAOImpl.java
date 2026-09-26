package pe.edu.pucp.admitu.configuracion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.Carrera;
import pe.edu.pucp.admitu.configuracion.Facultad;
import pe.edu.pucp.admitu.configuracion.dao.CarreraDAO;
import pe.edu.pucp.admitu.configuracion.dao.FacultadDAO;

import java.sql.*;
import java.util.*;


public class CarreraDAOImpl implements CarreraDAO {
    private final FacultadDAO facultadDAO = new FacultadDAOImpl();

    @Override
    public boolean insertar(Carrera obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carrera_insertar(?,?,?,?)}")) {
            cs.setInt(1, obj.getFacultad().getId());
            cs.setString(2, obj.getCodigoCarrera());
            cs.setString(3, obj.getNombre());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            obj.setId(cs.getInt(4));
            return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Carrera: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Carrera obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carrera_actualizar(?,?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setInt(2, obj.getFacultad().getId());
            cs.setString(3, obj.getCodigoCarrera());
            cs.setString(4, obj.getNombre());
            cs.setBoolean(5, obj.isActivo());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Carrera: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carrera_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Carrera: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Carrera buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_carrera_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Carrera: " + ex.getMessage()); }
        return null;
    }

    @Override
    public List<Carrera> listar() { return listarInterno(null); }


    @Override
    public List<Carrera> listarPorFacultad(int idFacultad) { return listarInterno(idFacultad); }

    private List<Carrera> listarInterno(Integer idFacultad) {
        List<Carrera> lista = new ArrayList<>();
        String proc = idFacultad != null ? "{call sp_carrera_listar_por_facultad(?)}" : "{call sp_carrera_listar()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(proc)) {
            if (idFacultad != null) cs.setInt(1, idFacultad);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar Carrera: " + ex.getMessage()); }
        return lista;
    }
    private Carrera mapear(ResultSet rs) throws SQLException {
        Facultad f = facultadDAO.buscarPorId(rs.getInt("id_facultad"));
        Carrera c = new Carrera(f, rs.getString("codigo_carrera"), rs.getString("nombre"));
        c.setId(rs.getInt("id"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
