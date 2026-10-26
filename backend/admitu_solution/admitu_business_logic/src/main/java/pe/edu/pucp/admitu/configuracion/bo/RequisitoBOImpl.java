package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
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
        if (r == null) throw new IllegalArgumentException("El requisito es null");
        Validaciones.codigo(r.getCodigoRequisito(), "Codigo de requisito", 20);
        Validaciones.texto(r.getNombre(), "Nombre de requisito", 150);
        Validaciones.longitud(r.getDescripcion(), "Descripcion", 255);
        if (r.getTipoArchivoRequerido() == null)
            throw new IllegalArgumentException("El requisito no tiene tipo de archivo");
        Validaciones.positivo(r.getTamanioMaximoBytes(), "El tamanio maximo en bytes");
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
