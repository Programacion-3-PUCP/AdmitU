package pe.edu.pucp.admitu.pago.bo;

import java.time.LocalDate;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.pago.boi.IPagoBO;
import pe.edu.pucp.admitu.pago.dao.PagoDAO;
import pe.edu.pucp.admitu.pago.impl.PagoImpl;
import pe.edu.pucp.admitu.pago.Pago;

import java.util.List;

public class PagoBOImpl implements IPagoBO {

    private PagoDAO daoPago;

    public PagoBOImpl() {
        daoPago = new PagoImpl();
    }

    private void validar(Pago p) {
        if (p == null) throw new IllegalArgumentException("El pago es null");
        if (p.getPostulacion() == null || p.getPostulacion().getId() <= 0)
            throw new IllegalArgumentException("El pago no tiene postulacion valida");
        if (p.getMedioPago() == null) throw new IllegalArgumentException("El pago no tiene medio de pago");
        if (p.getEstadoPago() == null) throw new IllegalArgumentException("El pago no tiene estado");
        Validaciones.monto(p.getMonto(), "Monto del pago", true);
        Validaciones.codigo(p.getCodigoPago(), "Codigo de pago", 30);
        Validaciones.longitud(p.getReferenciaPasarela(), "Referencia de la pasarela", 100);
        Validaciones.longitud(p.getRutaVoucher(), "Ruta del voucher", 255);
        Validaciones.noFutura(p.getFechaGeneracion(), "La fecha de generacion");
        Validaciones.noFutura(p.getFechaPago(), "La fecha de pago");
        if (p.getMedioPago().name().equals("TRANSFERENCIA")
                && (p.getRutaVoucher() == null || p.getRutaVoucher().trim().isEmpty()))
            throw new IllegalArgumentException("La transferencia requiere voucher");
    }

    private void registrarFechaPago(Pago p) {
        if (p.getEstadoPago() == EstadoPago.APROBADO && p.getFechaPago() == null)
            p.setFechaPago(LocalDate.now());
    }

    @Override
    public int insertar(Pago p) {
        registrarFechaPago(p);
        validar(p);
        try {
            int r = daoPago.insertar(p);
            TransactionContext.commit();
            return r;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("Error: " + ex.getMessage());
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int modificar(Pago p) {
        if (p == null) throw new IllegalArgumentException("El pago es null");
        Validaciones.positivo(p.getId(), "Id de pago");
        if (p.getEstadoPago() == null) throw new IllegalArgumentException("El pago no tiene estado");
        registrarFechaPago(p);
        validar(p);
        return daoPago.modificar(p);
    }

    @Override
    public int eliminar(int id) {
        Validaciones.positivo(id, "Id de pago");
        return daoPago.eliminar(id);
    }

    @Override
    public List<Pago> listarTodos() {
        return daoPago.listarTodos();
    }

    @Override
    public Pago buscarPorId(int id) {
        Validaciones.positivo(id, "Id de pago");
        return daoPago.buscarPorId(id);
    }
}
