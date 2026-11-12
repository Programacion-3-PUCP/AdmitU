package pe.edu.pucp.admitu.reporte.bo;
import java.io.InputStream;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import pe.edu.pucp.admitu.reporte.*;
import pe.edu.pucp.admitu.reporte.boi.IReporteBO;
import pe.edu.pucp.admitu.reporte.dao.ReporteDAO;
import pe.edu.pucp.admitu.reporte.impl.ReporteImpl;
public class ReporteBOImpl implements IReporteBO {
 private static final Map<TipoReporte,JasperReport> PLANTILLAS=new EnumMap<>(TipoReporte.class);
 private final ReporteDAO dao=new ReporteImpl();
 public byte[] generar(TipoReporte tipo,FiltroReporte f){
  if(tipo==null||f==null)throw new IllegalArgumentException("Indique reporte y filtros");
  if(f.getConvocatoriaId()<0||f.getModalidadId()<0)throw new IllegalArgumentException("Los filtros no pueden ser negativos");
  if(f.getDesde()!=null&&f.getHasta()!=null&&f.getDesde().isAfter(f.getHasta()))throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
  Map<String,Object> params=new HashMap<>();
  params.put("FILTROS","Convocatoria: "+(f.getConvocatoriaId()==0?"todas":f.getConvocatoriaId())+" | Modalidad: "+(f.getModalidadId()==0?"todas":f.getModalidadId())+(f.getDesde()==null?"":" | Desde: "+f.getDesde())+(f.getHasta()==null?"":" | Hasta: "+f.getHasta()));
  params.put(JRParameter.REPORT_LOCALE,Locale.forLanguageTag("es-PE"));
  params.put(JRParameter.REPORT_TIME_ZONE,TimeZone.getTimeZone("America/Lima"));
  try{
   JasperPrint print=JasperFillManager.fillReport(plantilla(tipo),params,new JRMapCollectionDataSource(dao.consultar(tipo,f)));
   return JasperExportManager.exportReportToPdf(print);
  }catch(Exception e){throw new IllegalStateException("No fue posible generar el PDF del reporte",e);}
 }
 private static synchronized JasperReport plantilla(TipoReporte tipo)throws Exception{
  JasperReport r=PLANTILLAS.get(tipo);
  if(r==null){try(InputStream in=ReporteBOImpl.class.getResourceAsStream("/reportes/"+tipo.getPlantilla()+".jrxml")){
   if(in==null)throw new IllegalStateException("Diseño Jasper no encontrado");
   r=JasperCompileManager.compileReport(in);PLANTILLAS.put(tipo,r);
  }}return r;
 }
}
