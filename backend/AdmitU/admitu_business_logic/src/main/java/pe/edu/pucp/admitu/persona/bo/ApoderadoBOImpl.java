package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Apoderado;
import pe.edu.pucp.admitu.persona.boi.IApoderadoBO;
import pe.edu.pucp.admitu.persona.dao.ApoderadoDAO;
import pe.edu.pucp.admitu.persona.impl.ApoderadoImpl;

import java.util.List;

public class ApoderadoBOImpl implements IApoderadoBO {

    private ApoderadoDAO daoApoderado;

    public ApoderadoBOImpl() {
        daoApoderado = new ApoderadoImpl();
    }

    private void validar(Apoderado a) {
        if (a == null) throw new IllegalArgumentException("El apoderado es null");
        Validaciones.persona(a.getNombres(), a.getApellidoPaterno(), a.getApellidoMaterno(),
                a.getCorreo(), a.getTipoDocumento(), a.getNumeroDocumento(), a.getTelefono());
        if (a.getParentesco() == null)
            throw new IllegalArgumentException("El apoderado no tiene parentesco definido");
    }

    @Override
    public int insertar(Apoderado a) {
        validar(a);
        try {
            int r = daoApoderado.insertar(a);
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
    public int modificar(Apoderado a) {
        validar(a);
        if (a.getId() <= 0) throw new RuntimeException("Id de apoderado no valido");
        return daoApoderado.modificar(a);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de apoderado no valido");
        return daoApoderado.eliminar(id);
    }

    @Override
    public List<Apoderado> listarTodos() {
        return daoApoderado.listarTodos();
    }

    @Override
    public Apoderado buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de apoderado no valido");
        return daoApoderado.buscarPorId(id);
    }
}
