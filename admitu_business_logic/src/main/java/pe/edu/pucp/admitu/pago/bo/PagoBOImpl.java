package pe.edu.pucp.admitu.pago.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.pago.boi.IPagoBO;
import pe.edu.pucp.admitu.pago.dao.PagoDAO;
import pe.edu.pucp.admitu.pago.impl.PagoImpl;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.bo.Validaciones;
import java.time.LocalDate;

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
        Validaciones.monto(p.getMonto(), "Monto del pago", false);
        if (p.getCodigoPago() == null || p.getCodigoPago().trim().isEmpty())
            throw new RuntimeException("El codigo de pago no puede estar vacio");
        if (p.getEstadoPago() == null) throw new RuntimeException("El pago no tiene estado");
        Validaciones.codigo(p.getCodigoPago(), "Código de pago", 30);
        Validaciones.longitud(p.getReferenciaPasarela(), "Referencia bancaria", 100);
        Validaciones.longitud(p.getRutaVoucher(), "Ruta del voucher", 255);
        if (p.getMedioPago().name().equals("TRANSFERENCIA")) {
            Validaciones.texto(p.getRutaVoucher(), "Voucher de transferencia", 255);
            String voucher = p.getRutaVoucher().toLowerCase();
            if (!(voucher.endsWith(".pdf") || voucher.endsWith(".jpg")
                    || voucher.endsWith(".jpeg") || voucher.endsWith(".png")))
                throw new IllegalArgumentException("El voucher debe ser PDF, JPG o PNG");
        }
        Validaciones.noFutura(p.getFechaGeneracion(), "Fecha de generación");
        Validaciones.noFutura(p.getFechaPago(), "Fecha del pago");
        if (p.getEstadoPago() == EstadoPago.APROBADO && p.getFechaPago() == null)
            throw new IllegalArgumentException("El pago aprobado requiere fecha");
        if (p.getFechaPago() != null && p.getFechaGeneracion() != null
                && p.getFechaPago().isBefore(p.getFechaGeneracion()))
            throw new IllegalArgumentException("El pago no puede anteceder a su generación");
    }

    @Override
    public int insertar(Pago p) {
        validar(p);
        if (p.getEstadoPago() != EstadoPago.GENERADO && p.getEstadoPago() != EstadoPago.PENDIENTE)
            throw new IllegalArgumentException("Un nuevo pago debe iniciar GENERADO o PENDIENTE");
        return TransactionContext.ejecutar(() -> daoPago.insertar(p));
    }

    @Override
    public int modificar(Pago pago) {
        if (pago == null || pago.getId() <= 0)
            throw new IllegalArgumentException("Debe indicar un pago existente");
        if (pago.getEstadoPago() == null)
            throw new IllegalArgumentException("Debe indicar el estado del pago");
        Validaciones.longitud(pago.getReferenciaPasarela(), "Referencia bancaria", 100);

        return TransactionContext.ejecutar(() -> {
            Pago anterior = daoPago.buscarPorId(pago.getId());
            if (anterior == null) throw new IllegalArgumentException("No se encontró el pago");
            EstadoPago antes = anterior.getEstadoPago();
            EstadoPago despues = pago.getEstadoPago();
            if (antes != despues) {
                boolean permitida = switch (antes) {
                    case GENERADO -> despues == EstadoPago.PENDIENTE || despues == EstadoPago.RECHAZADO
                            || despues == EstadoPago.VENCIDO;
                    case PENDIENTE -> despues == EstadoPago.APROBADO || despues == EstadoPago.RECHAZADO
                            || despues == EstadoPago.VENCIDO;
                    case APROBADO, RECHAZADO, VENCIDO -> false;
                };
                if (!permitida) throw new IllegalArgumentException("Cambio de estado de pago no permitido: "
                        + antes + " a " + despues);
            }
            if (despues == EstadoPago.APROBADO) {
                if (anterior.getMedioPago().name().equals("TRANSFERENCIA")
                        && (anterior.getRutaVoucher() == null || anterior.getRutaVoucher().isBlank()))
                    throw new IllegalArgumentException("No se puede aprobar una transferencia sin voucher");
                if (antes != EstadoPago.APROBADO) pago.setFechaPago(LocalDate.now());
                else pago.setFechaPago(anterior.getFechaPago());
            }
            return daoPago.modificar(pago);
        });
    }

    @Override
    public int eliminar(int id) {
        Validaciones.positivo(id, "Id de pago");
        return TransactionContext.ejecutar(() -> {
            Pago pago = daoPago.buscarPorId(id);
            if (pago == null) throw new IllegalArgumentException("No se encontró el pago");
            if (pago.getEstadoPago() == EstadoPago.APROBADO)
                throw new IllegalArgumentException("No se puede anular un pago aprobado");
            return daoPago.eliminar(id);
        });
    }

    @Override
    public List<Pago> listarTodos() {
        return TransactionContext.ejecutar(() -> daoPago.listarTodos());
    }

    @Override
    public Pago buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de pago no valido");
        return TransactionContext.ejecutar(() -> daoPago.buscarPorId(id));
    }
}
