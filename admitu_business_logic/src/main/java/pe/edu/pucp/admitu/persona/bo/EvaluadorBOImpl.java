package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.boi.IEvaluadorBO;
import pe.edu.pucp.admitu.persona.dao.EvaluadorDAO;
import pe.edu.pucp.admitu.persona.impl.EvaluadorImpl;

import java.util.List;

public class EvaluadorBOImpl implements IEvaluadorBO {

    private EvaluadorDAO daoEvaluador;

    public EvaluadorBOImpl() {
        daoEvaluador = new EvaluadorImpl();
    }

    private void validar(Evaluador e) {
        if (e == null) throw new RuntimeException("El evaluador es null");
        if (e.getNombres() == null || e.getNombres().trim().isEmpty())
            throw new RuntimeException("Los nombres del evaluador no pueden estar vacios");
        if (e.getNombres().length() > 100)
            throw new RuntimeException("Los nombres no deben exceder 100 caracteres");
        if (e.getApellidoPaterno() == null || e.getApellidoPaterno().trim().isEmpty())
            throw new RuntimeException("El apellido paterno no puede estar vacio");
        if (e.getApellidoPaterno().length() > 100)
            throw new RuntimeException("El apellido paterno no debe exceder 100 caracteres");
        if (e.getCorreo() == null || e.getCorreo().trim().isEmpty())
            throw new RuntimeException("El correo del evaluador no puede estar vacio");
        if (e.getCorreo().length() > 150)
            throw new RuntimeException("El correo no debe exceder 150 caracteres");
        if (e.getTipoDocumento() == null)
            throw new RuntimeException("El evaluador no tiene tipo de documento");
        if (e.getNumeroDocumento() == null || e.getNumeroDocumento().trim().isEmpty())
            throw new RuntimeException("El numero de documento no puede estar vacio");
        if (e.getNumeroDocumento().length() > 20)
            throw new RuntimeException("El numero de documento no debe exceder 20 caracteres");
        if (e.getTelefono() != null && e.getTelefono().length() > 20)
            throw new RuntimeException("El telefono no debe exceder 20 caracteres");
        if (e.getCargo() == null || e.getCargo().trim().isEmpty())
            throw new RuntimeException("El cargo del evaluador no puede estar vacio");
        if (e.getCargo().length() > 100)
            throw new RuntimeException("El cargo no debe exceder 100 caracteres");
    }

    @Override
    public int insertar(Evaluador e) {
        validar(e);
        try {
            int r = daoEvaluador.insertar(e);
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
    public int modificar(Evaluador e) {
        validar(e);
        if (e.getId() <= 0) throw new RuntimeException("Id de evaluador no valido");
        return daoEvaluador.modificar(e);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de evaluador no valido");
        return daoEvaluador.eliminar(id);
    }

    @Override
    public List<Evaluador> listarTodos() {
        return daoEvaluador.listarTodos();
    }

    @Override
    public Evaluador buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de evaluador no valido");
        return daoEvaluador.buscarPorId(id);
    }
}
