package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.postulacion.CarnePostulante;
import pe.edu.pucp.admitu.postulacion.boi.ICarnePostulanteBO;
import pe.edu.pucp.admitu.postulacion.dao.CarnePostulanteDAO;
import pe.edu.pucp.admitu.postulacion.impl.CarnePostulanteImpl;

import java.util.List;

public class CarnePostulanteBOImpl implements ICarnePostulanteBO {

    private CarnePostulanteDAO daoCarne;

    public CarnePostulanteBOImpl() {
        daoCarne = new CarnePostulanteImpl();
    }

    private void validar(CarnePostulante c) {
        if (c == null) throw new RuntimeException("El carne de postulante es null");
        if (c.getPostulacion() == null || c.getPostulacion().getId() <= 0)
            throw new RuntimeException("El carne debe pertenecer a una postulacion valida");
        if (c.getSede() == null || c.getSede().getId() <= 0)
            throw new RuntimeException("El carne debe asignarse a una sede valida");
        if (c.getCodigoCarne() == null || c.getCodigoCarne().trim().isEmpty())
            throw new RuntimeException("El codigo del carne no puede estar vacio");
        if (c.getCodigoCarne().length() > 30)
            throw new RuntimeException("El codigo del carne no debe exceder 30 caracteres");
        if (c.getFechaInicioVigencia() != null && c.getFechaFinVigencia() != null
            && c.getFechaInicioVigencia().isAfter(c.getFechaFinVigencia()))
            throw new RuntimeException("La fecha de inicio de vigencia no puede ser posterior al fin");
        if (c.getAulaExamen() != null && c.getAulaExamen().length() > 20)
            throw new RuntimeException("El aula de examen no debe exceder 20 caracteres");
        Validaciones.codigo(c.getCodigoCarne(), "Código de carné", 30);
        Validaciones.rango(c.getFechaInicioVigencia(), c.getFechaFinVigencia(), "vigencia del carné", true);
        Validaciones.longitud(c.getAulaExamen(), "Aula", 30);
    }

    @Override
    public int insertar(CarnePostulante c) {
        validar(c);
        return TransactionContext.ejecutar(() -> daoCarne.insertar(c));
    }

    @Override
    public int modificar(CarnePostulante c) {
        validar(c);
        if (c.getId() <= 0) throw new RuntimeException("Id de carne no valido");
        return TransactionContext.ejecutar(() -> daoCarne.modificar(c));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de carne no valido");
        return TransactionContext.ejecutar(() -> daoCarne.eliminar(id));
    }

    @Override
    public List<CarnePostulante> listarTodos() {
        return TransactionContext.ejecutar(() -> daoCarne.listarTodos());
    }

    @Override
    public CarnePostulante buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de carne no valido");
        return TransactionContext.ejecutar(() -> daoCarne.buscarPorId(id));
    }
}
