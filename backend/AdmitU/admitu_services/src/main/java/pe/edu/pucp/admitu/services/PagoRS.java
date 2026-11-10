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
        return boPago.modificar(pago);
    }

    @DELETE
    @Path("eliminarPago/{id}")
    public int eliminarPago(@PathParam("id") int idPago) {
        return boPago.eliminar(idPago);
    }
}
