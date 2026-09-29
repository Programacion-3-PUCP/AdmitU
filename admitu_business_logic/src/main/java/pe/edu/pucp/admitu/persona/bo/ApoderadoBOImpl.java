package pe.edu.pucp.admitu.persona.bo;

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
        if (a == null) throw new RuntimeException("El apoderado es null");
        if (a.getNombres() == null || a.getNombres().trim().isEmpty())
            throw new RuntimeException("Los nombres del apoderado no pueden estar vacios");
        if (a.getNombres().length() > 100)
            throw new RuntimeException("Los nombres no deben exceder 100 caracteres");
        if (a.getApellidoPaterno() == null || a.getApellidoPaterno().trim().isEmpty())
            throw new RuntimeException("El apellido paterno no puede estar vacio");
        if (a.getApellidoPaterno().length() > 100)
            throw new RuntimeException("El apellido paterno no debe exceder 100 caracteres");
        if (a.getCorreo() == null || a.getCorreo().trim().isEmpty())
            throw new RuntimeException("El correo del apoderado no puede estar vacio");
        if (a.getCorreo().length() > 150)
            throw new RuntimeException("El correo no debe exceder 150 caracteres");
        if (a.getTipoDocumento() == null)
            throw new RuntimeException("El apoderado no tiene tipo de documento");
        if (a.getNumeroDocumento() == null || a.getNumeroDocumento().trim().isEmpty())
            throw new RuntimeException("El numero de documento no puede estar vacio");
        if (a.getNumeroDocumento().length() > 20)
            throw new RuntimeException("El numero de documento no debe exceder 20 caracteres");
        if (a.getTelefono() != null && a.getTelefono().length() > 20)
            throw new RuntimeException("El telefono no debe exceder 20 caracteres");
        if (a.getParentesco() == null)
            throw new RuntimeException("El apoderado no tiene parentesco definido");
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
