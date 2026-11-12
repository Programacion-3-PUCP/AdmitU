package pe.edu.pucp.admitu.services;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.LocalDate;
import pe.edu.pucp.admitu.reporte.*;
import pe.edu.pucp.admitu.reporte.bo.ReporteBOImpl;
import pe.edu.pucp.admitu.reporte.boi.IReporteBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;
@Path("ReporteRS") @Produces("application/pdf")
public class ReporteRS {
 private final IReporteBO bo=new ReporteBOImpl();
 @Context private SecurityContext seguridad;
 private Response generar(TipoReporte tipo,int convocatoria,int modalidad,String desde,String hasta){
  if(tipo==TipoReporte.OBSERVADOS)Seguridad.exigirRoles(seguridad,"admin","evaluador");else Seguridad.exigirRoles(seguridad,"admin");
  FiltroReporte f=new FiltroReporte();f.setConvocatoriaId(convocatoria);f.setModalidadId(modalidad);
  try{f.setDesde(desde==null||desde.isBlank()?null:LocalDate.parse(desde));f.setHasta(hasta==null||hasta.isBlank()?null:LocalDate.parse(hasta));}
  catch(java.time.format.DateTimeParseException e){throw new BadRequestException("Fecha no válida; use AAAA-MM-DD");}
  return Response.ok(bo.generar(tipo,f),"application/pdf").header("Content-Disposition","inline; filename=\""+tipo.getPlantilla()+".pdf\"")
   .header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").build();
 }
 @GET @Path("generarIngresos") public Response ingresos(@QueryParam("convocatoriaId")int c,@QueryParam("modalidadId")int m,@QueryParam("desde")String d,@QueryParam("hasta")String h){return generar(TipoReporte.INGRESOS,c,m,d,h);}
 @GET @Path("generarObservados") public Response observados(@QueryParam("convocatoriaId")int c,@QueryParam("modalidadId")int m,@QueryParam("desde")String d,@QueryParam("hasta")String h){return generar(TipoReporte.OBSERVADOS,c,m,d,h);}
 @GET @Path("generarVacantes") public Response vacantes(@QueryParam("convocatoriaId")int c,@QueryParam("modalidadId")int m,@QueryParam("desde")String d,@QueryParam("hasta")String h){return generar(TipoReporte.VACANTES,c,m,d,h);}
}
