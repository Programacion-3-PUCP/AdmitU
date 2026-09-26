package pe.edu.pucp.admitu.pago.dao;
import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.pago.Pago;
import java.util.List;
public interface PagoDAO extends IDAO<Pago> {
    List<Pago> listarPorPostulacion(int idPostulacion);
}