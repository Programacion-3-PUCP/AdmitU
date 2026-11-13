package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.pago.boi.IPagoBO;

@Path("PagoRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PagoRS {

    @jakarta.ws.rs.core.Context private jakarta.ws.rs.core.SecurityContext seguridad;
    private final IPagoBO boPago;

    public PagoRS() {
        boPago = new PagoBOImpl();
    }

    @GET
    @Path("listarTodosPagos")
    public List<Pago> listarTodosPagos() {
        return boPago.listarTodos();
    }

    @POST
    @Path("insertarPago")
    public int insertarPago(Pago pago) {
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"admin");
        return boPago.insertar(pago);
    }

    @GET
    @Path("buscarPagoPorId/{id}")
    public Pago buscarPagoPorId(@PathParam("id") int idPago) {
        return boPago.buscarPorId(idPago);
    }

    @PUT
    @Path("modificarPago")
    public int modificarPago(Pago pago) {
        if(pago==null||pago.getEstadoPago()==null)throw new BadRequestException("Pago y estado requeridos");
        Pago actual=boPago.buscarPorId(pago.getId());
        if(actual==null)throw new NotFoundException("Pago no encontrado");
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRevision(seguridad,actual.getPostulacion().getId());
        if(pago.getPostulacion()!=null&&pago.getPostulacion().getId()!=actual.getPostulacion().getId())throw new ForbiddenException("No puedes cambiar el expediente del pago");
        actual.setEstadoPago(pago.getEstadoPago());
        return boPago.modificar(actual);
    }

    @DELETE
    @Path("eliminarPago/{id}")
    public int eliminarPago(@PathParam("id") int idPago) {
        pe.edu.pucp.admitu.services.seguridad.Seguridad.exigirRoles(seguridad,"admin");
        return boPago.eliminar(idPago);
    }
}
