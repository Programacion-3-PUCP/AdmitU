package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
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
        if (f == null) throw new IllegalArgumentException("La facultad es null");
        Validaciones.codigo(f.getCodigo(), "Codigo de facultad", 20);
        Validaciones.texto(f.getNombre(), "Nombre de facultad", 150);
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
