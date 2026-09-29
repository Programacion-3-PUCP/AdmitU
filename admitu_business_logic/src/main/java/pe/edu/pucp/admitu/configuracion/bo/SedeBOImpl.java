package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Sede;
import pe.edu.pucp.admitu.configuracion.boi.ISedeBO;
import pe.edu.pucp.admitu.configuracion.dao.SedeDAO;
import pe.edu.pucp.admitu.configuracion.impl.SedeImpl;

import java.util.List;

public class SedeBOImpl implements ISedeBO {

    private SedeDAO daoSede;

    public SedeBOImpl() {
        daoSede = new SedeImpl();
    }

    private void validar(Sede s) {
        if (s == null) throw new RuntimeException("La sede es null");
        if (s.getCodigo() == null || s.getCodigo().trim().isEmpty())
            throw new RuntimeException("El codigo de sede no puede estar vacio");
        if (s.getCodigo().length() > 20)
            throw new RuntimeException("El codigo no debe exceder 20 caracteres");
        if (s.getNombre() == null || s.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre de sede no puede estar vacio");
        if (s.getNombre().length() > 100)
            throw new RuntimeException("El nombre no debe exceder 100 caracteres");
        if (s.getDireccion() != null && s.getDireccion().length() > 200)
            throw new RuntimeException("La direccion no debe exceder 200 caracteres");
    }

    @Override
    public int insertar(Sede s) {
        validar(s);
        try {
            int r = daoSede.insertar(s);
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
    public int modificar(Sede s) {
        validar(s);
        if (s.getId() <= 0) throw new RuntimeException("Id de sede no valido");
        return daoSede.modificar(s);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de sede no valido");
        return daoSede.eliminar(id);
    }

    @Override
    public List<Sede> listarTodos() {
        return daoSede.listarTodos();
    }

    @Override
    public Sede buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de sede no valido");
        return daoSede.buscarPorId(id);
    }
}
