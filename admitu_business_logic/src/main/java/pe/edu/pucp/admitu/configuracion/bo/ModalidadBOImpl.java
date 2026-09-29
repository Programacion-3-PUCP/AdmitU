package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Modalidad;
import pe.edu.pucp.admitu.configuracion.boi.IModalidadBO;
import pe.edu.pucp.admitu.configuracion.dao.ModalidadDAO;
import pe.edu.pucp.admitu.configuracion.impl.ModalidadImpl;

import java.util.List;

public class ModalidadBOImpl implements IModalidadBO {

    private ModalidadDAO daoModalidad;

    public ModalidadBOImpl() {
        daoModalidad = new ModalidadImpl();
    }

    private void validar(Modalidad m) {
        if (m == null) throw new RuntimeException("La modalidad es null");
        if (m.getCodigoModalidad() == null || m.getCodigoModalidad().trim().isEmpty())
            throw new RuntimeException("El codigo de modalidad no puede estar vacio");
        if (m.getCodigoModalidad().length() > 20)
            throw new RuntimeException("El codigo no debe exceder 20 caracteres");
        if (m.getNombre() == null || m.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre de modalidad no puede estar vacio");
        if (m.getNombre().length() > 100)
            throw new RuntimeException("El nombre no debe exceder 100 caracteres");
        if (m.getDescripcion() != null && m.getDescripcion().length() > 255)
            throw new RuntimeException("La descripcion no debe exceder 255 caracteres");
    }

    @Override
    public int insertar(Modalidad m) {
        validar(m);
        try {
            int r = daoModalidad.insertar(m);
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
    public int modificar(Modalidad m) {
        validar(m);
        if (m.getId() <= 0) throw new RuntimeException("Id de modalidad no valido");
        return daoModalidad.modificar(m);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de modalidad no valido");
        return daoModalidad.eliminar(id);
    }

    @Override
    public List<Modalidad> listarTodos() {
        return daoModalidad.listarTodos();
    }

    @Override
    public Modalidad buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de modalidad no valido");
        return daoModalidad.buscarPorId(id);
    }
}
