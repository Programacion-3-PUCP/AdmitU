package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.pago.boi.IPagoBO;
import pe.edu.pucp.admitu.services.seguridad.Seguridad;

@Path("PagoRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PagoRS {

    private IPagoBO boPago;

    @Context
    private SecurityContext seguridad;

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
        Seguridad.exigirRoles(seguridad, "admin");
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
        if (pago == null || pago.getEstadoPago() == null) {
            throw new BadRequestException("Pago y estado requeridos");
        }
        Pago actual = boPago.buscarPorId(pago.getId());
        if (actual == null) {
            throw new NotFoundException("Pago no encontrado");
        }
        Seguridad.exigirRevision(seguridad, actual.getPostulacion().getId());
        if (pago.getPostulacion() != null
                && pago.getPostulacion().getId() != actual.getPostulacion().getId()) {
            throw new ForbiddenException("No puedes cambiar el expediente del pago");
        }
        actual.setEstadoPago(pago.getEstadoPago());
        return boPago.modificar(actual);
    }

    @DELETE
    @Path("eliminarPago/{id}")
    public int eliminarPago(@PathParam("id") int idPago) {
        Seguridad.exigirRoles(seguridad, "admin");
        return boPago.eliminar(idPago);
    }
}
