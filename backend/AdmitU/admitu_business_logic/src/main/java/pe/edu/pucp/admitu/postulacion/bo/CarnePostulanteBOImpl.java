package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
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
        if (c == null) throw new IllegalArgumentException("El carne de postulante es null");
        if (c.getPostulacion() == null || c.getPostulacion().getId() <= 0)
            throw new IllegalArgumentException("El carne debe pertenecer a una postulacion valida");
        if (c.getSede() == null || c.getSede().getId() <= 0)
            throw new IllegalArgumentException("El carne debe asignarse a una sede valida");
        Validaciones.codigo(c.getCodigoCarne(), "Codigo del carne", 30);
        Validaciones.rango(c.getFechaInicioVigencia(), c.getFechaFinVigencia(),
                "la vigencia del carne", false);
        Validaciones.longitud(c.getAulaExamen(), "Aula de examen", 20);
    }

    @Override
    public int insertar(CarnePostulante c) {
        validar(c);
        try {
            int r = daoCarne.insertar(c);
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
    public int modificar(CarnePostulante c) {
        validar(c);
        if (c.getId() <= 0) throw new RuntimeException("Id de carne no valido");
        return daoCarne.modificar(c);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de carne no valido");
        return daoCarne.eliminar(id);
    }

    @Override
    public List<CarnePostulante> listarTodos() {
        return daoCarne.listarTodos();
    }

    @Override
    public CarnePostulante buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de carne no valido");
        return daoCarne.buscarPorId(id);
    }
}
