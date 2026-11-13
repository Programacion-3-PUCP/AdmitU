package pe.edu.pucp.admitu.configuracion.impl;
import java.sql.*;
import java.util.Map;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.configuracion.dao.CatalogoDAO;
public class CatalogoImpl implements CatalogoDAO {
    private static final Map<String,String> PROCEDIMIENTOS = Map.of("Facultad","REACTIVAR_FACULTAD","Carrera","REACTIVAR_CARRERA","Modalidad","REACTIVAR_MODALIDAD","Sede","REACTIVAR_SEDE","Evaluador","REACTIVAR_EVALUADOR","Requisito","REACTIVAR_REQUISITO");
    public int reactivar(String entidad, int id) {
        String sp = PROCEDIMIENTOS.get(entidad);
        if (sp == null) throw new IllegalArgumentException("Catálogo no permitido");
        try (Connection con = DBManager.getInstance().getConnection(); CallableStatement cs = con.prepareCall("{call " + sp + "(?)}")) {
            cs.setInt(1, id); return cs.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("No se pudo reactivar el catálogo",e); }
    }
}
