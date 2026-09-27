package pe.edu.pucp.admitu.pago.bo;

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
        if (p == null) throw new RuntimeException("El pago es null");
        if (p.getPostulacion() == null || p.getPostulacion().getId() <= 0)
            throw new RuntimeException("El pago no tiene postulacion valida");
        if (p.getMedioPago() == null) throw new RuntimeException("El pago no tiene medio de pago");
        if (p.getMonto() < 0) throw new RuntimeException("El monto no puede ser negativo");
        if (p.getCodigoPago() == null || p.getCodigoPago().trim().isEmpty())
            throw new RuntimeException("El codigo de pago no puede estar vacio");
        if (p.getEstadoPago() == null) throw new RuntimeException("El pago no tiene estado");
        if (p.getMedioPago().name().equals("TRANSFERENCIA")
                && (p.getRutaVoucher() == null || p.getRutaVoucher().trim().isEmpty()))
            throw new RuntimeException("La transferencia requiere voucher");
    }

    @Override
    public int insertar(Pago p) {
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
        if (p == null) throw new RuntimeException("El pago es null");
        if (p.getId() <= 0) throw new RuntimeException("Id de pago no valido");
        if (p.getEstadoPago() == null) throw new RuntimeException("El pago no tiene estado");
        return daoPago.modificar(p);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de pago no valido");
        return daoPago.eliminar(id);
    }

    @Override
    public List<Pago> listarTodos() {
        return daoPago.listarTodos();
    }

    @Override
    public Pago buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de pago no valido");
        return daoPago.buscarPorId(id);
    }
}
