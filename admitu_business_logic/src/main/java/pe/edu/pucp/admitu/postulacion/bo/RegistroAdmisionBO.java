package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.pago.bo.PagoBOImpl;
import pe.edu.pucp.admitu.postulacion.Postulacion;

public class RegistroAdmisionBO {
    private final PostulacionBOImpl postulaciones = new PostulacionBOImpl();
    private final PagoBOImpl pagos = new PagoBOImpl();

    public int registrar(Postulacion postulacion, Pago pago) {
        if (postulacion == null || pago == null)
            throw new IllegalArgumentException("La postulación y el pago son obligatorios");
        if (pago.getPostulacion() != postulacion)
            throw new IllegalArgumentException("El pago debe pertenecer a la misma postulación");
        return TransactionContext.ejecutar(() -> {
            int id = postulaciones.insertar(postulacion);
            pagos.insertar(pago);
            return id;
        });
    }
}
