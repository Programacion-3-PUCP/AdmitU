package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
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
        if (e == null) throw new IllegalArgumentException("El evaluador es null");
        Validaciones.persona(e.getNombres(), e.getApellidoPaterno(), e.getApellidoMaterno(),
                e.getCorreo(), e.getTipoDocumento(), e.getNumeroDocumento(), e.getTelefono());
        Validaciones.texto(e.getCargo(), "Cargo del evaluador", 100);
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
