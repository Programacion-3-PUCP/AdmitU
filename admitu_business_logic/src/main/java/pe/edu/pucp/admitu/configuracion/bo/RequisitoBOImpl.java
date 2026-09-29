package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Requisito;
import pe.edu.pucp.admitu.configuracion.boi.IRequisitoBO;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoDAO;
import pe.edu.pucp.admitu.configuracion.impl.RequisitoImpl;

import java.util.List;

public class RequisitoBOImpl implements IRequisitoBO {

    private RequisitoDAO daoRequisito;

    public RequisitoBOImpl() {
        daoRequisito = new RequisitoImpl();
    }

    private void validar(Requisito r) {
        if (r == null) throw new RuntimeException("El requisito es null");
        if (r.getCodigoRequisito() == null || r.getCodigoRequisito().trim().isEmpty())
            throw new RuntimeException("El codigo de requisito no puede estar vacio");
        if (r.getCodigoRequisito().length() > 20)
            throw new RuntimeException("El codigo no debe exceder 20 caracteres");
        if (r.getNombre() == null || r.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre de requisito no puede estar vacio");
        if (r.getNombre().length() > 150)
            throw new RuntimeException("El nombre no debe exceder 150 caracteres");
        if (r.getDescripcion() != null && r.getDescripcion().length() > 255)
            throw new RuntimeException("La descripcion no debe exceder 255 caracteres");
        if (r.getTipoArchivoRequerido() == null)
            throw new RuntimeException("El requisito no tiene tipo de archivo");
        if (r.getTamanioMaximoBytes() <= 0)
            throw new RuntimeException("El tamanio maximo debe ser mayor que cero");
    }

    @Override
    public int insertar(Requisito r) {
        validar(r);
        try {
            int resultado = daoRequisito.insertar(r);
            TransactionContext.commit();
            return resultado;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("Error: " + ex.getMessage());
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int modificar(Requisito r) {
        validar(r);
        if (r.getId() <= 0) throw new RuntimeException("Id de requisito no valido");
        return daoRequisito.modificar(r);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de requisito no valido");
        return daoRequisito.eliminar(id);
    }

    @Override
    public List<Requisito> listarTodos() {
        return daoRequisito.listarTodos();
    }

    @Override
    public Requisito buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de requisito no valido");
        return daoRequisito.buscarPorId(id);
    }
}
