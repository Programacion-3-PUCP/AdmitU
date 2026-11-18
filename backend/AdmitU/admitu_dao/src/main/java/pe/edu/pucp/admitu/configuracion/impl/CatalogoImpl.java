package pe.edu.pucp.admitu.configuracion.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.dao.CatalogoDAO;

public class CatalogoImpl implements CatalogoDAO {

    @Override
    public int reactivar(String entidad, int id) {
        String sp;
        if ("Facultad".equals(entidad)) {
            sp = "REACTIVAR_FACULTAD";
        } else if ("Carrera".equals(entidad)) {
            sp = "REACTIVAR_CARRERA";
        } else if ("Modalidad".equals(entidad)) {
            sp = "REACTIVAR_MODALIDAD";
        } else if ("Sede".equals(entidad)) {
            sp = "REACTIVAR_SEDE";
        } else if ("Evaluador".equals(entidad)) {
            sp = "REACTIVAR_EVALUADOR";
        } else if ("Requisito".equals(entidad)) {
            sp = "REACTIVAR_REQUISITO";
        } else {
            throw new IllegalArgumentException("Catálogo no permitido");
        }

        String sql = "{call " + sp + "(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", id);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL REACTIVAR CATALOGO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
