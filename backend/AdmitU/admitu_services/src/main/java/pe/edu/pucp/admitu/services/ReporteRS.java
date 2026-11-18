package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import pe.edu.pucp.admitu.reporte.FiltroReporte;
import pe.edu.pucp.admitu.reporte.TipoReporte;
import pe.edu.pucp.admitu.reporte.bo.ReporteBOImpl;
import pe.edu.pucp.admitu.reporte.boi.IReporteBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("ReporteRS")
@Produces("application/pdf")
public class ReporteRS {

    private IReporteBO boReporte;

    @Context
    private SecurityContext seguridad;

    public ReporteRS() {
        boReporte = new ReporteBOImpl();
    }

    private Response generar(TipoReporte tipo, int convocatoria, int modalidad, String desde, String hasta) {
        if (tipo == TipoReporte.OBSERVADOS) {
            Seguridad.exigirRoles(seguridad, "admin", "evaluador");
        } else {
            Seguridad.exigirRoles(seguridad, "admin");
        }

        FiltroReporte filtro = new FiltroReporte();
        filtro.setConvocatoriaId(convocatoria);
        filtro.setModalidadId(modalidad);
        try {
            filtro.setDesde(desde == null || desde.trim().isEmpty() ? null : LocalDate.parse(desde));
            filtro.setHasta(hasta == null || hasta.trim().isEmpty() ? null : LocalDate.parse(hasta));
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Fecha no válida; use AAAA-MM-DD");
        }

        byte[] pdf;
        if (tipo == TipoReporte.OBSERVADOS && "evaluador".equals(Seguridad.rol(seguridad))) {
            pdf = boReporte.generarObservadosAsignados(filtro, Seguridad.id(seguridad));
        } else {
            pdf = boReporte.generar(tipo, filtro);
        }

        return Response.ok(pdf, "application/pdf")
                .header("Content-Disposition", "inline; filename=\"" + tipo.getPlantilla() + ".pdf\"")
                .header("Cache-Control", "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

    @GET
    @Path("generarIngresos")
    public Response ingresos(
            @QueryParam("convocatoriaId") int convocatoriaId,
            @QueryParam("modalidadId") int modalidadId,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return generar(TipoReporte.INGRESOS, convocatoriaId, modalidadId, desde, hasta);
    }

    @GET
    @Path("generarObservados")
    public Response observados(
            @QueryParam("convocatoriaId") int convocatoriaId,
            @QueryParam("modalidadId") int modalidadId,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return generar(TipoReporte.OBSERVADOS, convocatoriaId, modalidadId, desde, hasta);
    }

    @GET
    @Path("generarVacantes")
    public Response vacantes(
            @QueryParam("convocatoriaId") int convocatoriaId,
            @QueryParam("modalidadId") int modalidadId,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {
        return generar(TipoReporte.VACANTES, convocatoriaId, modalidadId, desde, hasta);
    }
}
