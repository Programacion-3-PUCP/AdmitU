package pe.edu.pucp.admitu.reporte.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.reporte.FiltroReporte;
import pe.edu.pucp.admitu.reporte.TipoReporte;
import pe.edu.pucp.admitu.reporte.dao.ReporteDAO;

public class ReporteImpl implements ReporteDAO {

    @Override
    public List<Map<String, ?>> consultar(TipoReporte tipo, FiltroReporte filtro) {
        List<Map<String, ?>> filas = new ArrayList<>();
        String sql = "{call " + tipo.getProcedimiento() + "(?,?,?,?)}";

        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_conv", filtro.getConvocatoriaId());
            cs.setInt("_mod", filtro.getModalidadId());
            cs.setDate("_desde", filtro.getDesde() == null ? null : Date.valueOf(filtro.getDesde()));
            cs.setDate("_hasta", filtro.getHasta() == null ? null : Date.valueOf(filtro.getHasta()));

            try (ResultSet rs = cs.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        String key = meta.getColumnLabel(i).toLowerCase();
                        Object value;
                        if ("postulacion_id".equals(key) || "observacion_id".equals(key)
                                || "cantidad".equals(key) || "completadas".equals(key)
                                || "cupo_carrera".equals(key) || "convocatoria_id".equals(key)
                                || "modalidad_id".equals(key) || "carrera_id".equals(key)) {
                            value = rs.getLong(i);
                        } else if ("total".equals(key)) {
                            value = rs.getBigDecimal(i);
                        } else {
                            value = rs.getString(i);
                        }
                        fila.put(key, value);
                    }
                    filas.add(fila);
                }
            }
            return filas;
        } catch (Exception ex) {
            System.out.println("ERROR AL CONSULTAR REPORTE: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
