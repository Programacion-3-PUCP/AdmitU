package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.InstitucionEducativa;
import pe.edu.pucp.admitu.persona.boi.IInstitucionEducativaBO;
import pe.edu.pucp.admitu.persona.dao.InstitucionEducativaDAO;
import pe.edu.pucp.admitu.persona.impl.InstitucionEducativaImpl;

import java.util.List;

public class InstitucionEducativaBOImpl implements IInstitucionEducativaBO {

    private InstitucionEducativaDAO daoInstitucion;

    public InstitucionEducativaBOImpl() {
        daoInstitucion = new InstitucionEducativaImpl();
    }

    private void validar(InstitucionEducativa i) {
        if (i == null) throw new RuntimeException("La institucion educativa es null");
        if (i.getPais() == null || i.getPais().getId() <= 0)
            throw new RuntimeException("La institucion debe pertenecer a un pais valido");
        if (i.getCodigoExterno() == null || i.getCodigoExterno().trim().isEmpty())
            throw new RuntimeException("El codigo externo no puede estar vacio");
        if (i.getCodigoExterno().length() > 30)
            throw new RuntimeException("El codigo externo no debe exceder 30 caracteres");
        if (i.getNombre() == null || i.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre de la institucion no puede estar vacio");
        if (i.getNombre().length() > 150)
            throw new RuntimeException("El nombre no debe exceder 150 caracteres");
        if (i.getTipoInstitucion() == null)
            throw new RuntimeException("La institucion no tiene tipo de institucion");
    }

    @Override
    public int insertar(InstitucionEducativa i) {
        validar(i);
        try {
            int r = daoInstitucion.insertar(i);
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
    public int modificar(InstitucionEducativa i) {
        validar(i);
        if (i.getId() <= 0) throw new RuntimeException("Id de institucion educativa no valido");
        return daoInstitucion.modificar(i);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de institucion educativa no valido");
        return daoInstitucion.eliminar(id);
    }

    @Override
    public List<InstitucionEducativa> listarTodos() {
        return daoInstitucion.listarTodos();
    }

    @Override
    public InstitucionEducativa buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de institucion educativa no valido");
        return daoInstitucion.buscarPorId(id);
    }
}
