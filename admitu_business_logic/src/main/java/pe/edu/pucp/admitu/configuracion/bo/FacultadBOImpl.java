package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.boi.IFacultadBO;
import pe.edu.pucp.admitu.configuracion.dao.FacultadDAO;
import pe.edu.pucp.admitu.configuracion.impl.FacultadImpl;
import pe.edu.pucp.admitu.configuracion.Facultad;

import java.util.List;

public class FacultadBOImpl implements IFacultadBO {

    private FacultadDAO daoFacultad;

    public FacultadBOImpl() {
        daoFacultad = new FacultadImpl();
    }

    private void validar(Facultad f) {
        if (f == null) throw new RuntimeException("La facultad es null");
        if (f.getCodigo() == null || f.getCodigo().trim().isEmpty())
            throw new RuntimeException("El codigo de facultad no puede estar vacio");
        if (f.getCodigo().length() > 20)
            throw new RuntimeException("El codigo no debe exceder 20 caracteres");
        if (f.getNombre() == null || f.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre de facultad no puede estar vacio");
        if (f.getNombre().length() > 150)
            throw new RuntimeException("El nombre no debe exceder 150 caracteres");
    }

    @Override
    public int insertar(Facultad f) {
        validar(f);
        try {
            int r = daoFacultad.insertar(f);
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
    public int modificar(Facultad f) {
        validar(f);
        if (f.getId() <= 0) throw new RuntimeException("Id de facultad no valido");
        return daoFacultad.modificar(f);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de facultad no valido");
        return daoFacultad.eliminar(id);
    }

    @Override
    public List<Facultad> listarTodos() {
        return daoFacultad.listarTodos();
    }

    @Override
    public Facultad buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de facultad no valido");
        return daoFacultad.buscarPorId(id);
    }
}
