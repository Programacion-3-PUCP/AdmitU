package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.boi.ICarreraBO;
import pe.edu.pucp.admitu.configuracion.dao.CarreraDAO;
import pe.edu.pucp.admitu.configuracion.impl.CarreraImpl;
import pe.edu.pucp.admitu.configuracion.Carrera;

import java.util.List;

public class CarreraBOImpl implements ICarreraBO {

    private CarreraDAO daoCarrera;

    public CarreraBOImpl() {
        daoCarrera = new CarreraImpl();
    }

    private void validar(Carrera c) {
        if (c == null) throw new IllegalArgumentException("La carrera es null");
        if (c.getFacultad() == null || c.getFacultad().getId() <= 0)
            throw new IllegalArgumentException("La carrera no tiene facultad valida");
        Validaciones.codigo(c.getCodigoCarrera(), "Codigo de carrera", 20);
        Validaciones.texto(c.getNombre(), "Nombre de carrera", 150);
    }

    @Override
    public int insertar(Carrera c) {
        validar(c);
        try {
            int r = daoCarrera.insertar(c);
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
    public int modificar(Carrera c) {
        validar(c);
        if (c.getId() <= 0) throw new RuntimeException("Id de carrera no valido");
        return daoCarrera.modificar(c);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de carrera no valido");
        return daoCarrera.eliminar(id);
    }

    @Override
    public List<Carrera> listarTodos() {
        return daoCarrera.listarTodos();
    }

    @Override
    public Carrera buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de carrera no valido");
        return daoCarrera.buscarPorId(id);
    }
}
