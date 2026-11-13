package pe.edu.pucp.admitu.reporte.impl;
import java.sql.*;
import java.util.*;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.reporte.*;
import pe.edu.pucp.admitu.reporte.dao.ReporteDAO;
public class ReporteImpl implements ReporteDAO {
 public List<Map<String,?>> consultar(TipoReporte tipo,FiltroReporte f){
  List<Map<String,?>> filas=new ArrayList<>();
  try(Connection con=DBManager.getInstance().getConnection();CallableStatement cs=con.prepareCall("{call "+tipo.getProcedimiento()+"(?,?,?,?)}")){
   cs.setInt(1,f.getConvocatoriaId());cs.setInt(2,f.getModalidadId());
   cs.setDate(3,f.getDesde()==null?null:java.sql.Date.valueOf(f.getDesde()));
   cs.setDate(4,f.getHasta()==null?null:java.sql.Date.valueOf(f.getHasta()));
   try(ResultSet rs=cs.executeQuery()){
    ResultSetMetaData meta=rs.getMetaData();
    while(rs.next()){
     Map<String,Object> fila=new HashMap<>();
     for(int i=1;i<=meta.getColumnCount();i++){
      String key=meta.getColumnLabel(i).toLowerCase(Locale.ROOT);
      Object value=switch(key){
       case "cantidad","completadas","cupo_carrera","convocatoria_id","modalidad_id","carrera_id" -> rs.getLong(i);
       case "total" -> rs.getBigDecimal(i);
       default -> rs.getString(i);
      };fila.put(key,value);
     }filas.add(fila);
    }
   }return filas;
  }catch(SQLException e){throw new IllegalStateException("No fue posible consultar los datos del reporte",e);}
 }
}
