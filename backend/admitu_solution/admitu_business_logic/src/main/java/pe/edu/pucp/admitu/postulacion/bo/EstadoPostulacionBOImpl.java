package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.boi.IEstadoPostulacionBO;
import pe.edu.pucp.admitu.postulacion.dao.EstadoPostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.EstadoPostulacionImpl;

import java.util.List;

public class EstadoPostulacionBOImpl implements IEstadoPostulacionBO {

    private EstadoPostulacionDAO daoEstado;

    public EstadoPostulacionBOImpl() {
        daoEstado = new EstadoPostulacionImpl();
    }

    private void validar(EstadoPostulacion e) {
        if (e == null) throw new IllegalArgumentException("El estado de postulacion es null");
        Validaciones.codigo(e.getCodigo(), "Codigo del estado", 20);
        Validaciones.texto(e.getNombre(), "Nombre del estado", 100);
        Validaciones.longitud(e.getDescripcion(), "Descripcion", 255);
    }

    @Override
    public int insertar(EstadoPostulacion e) {
        validar(e);
        try {
            int r = daoEstado.insertar(e);
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
    public int modificar(EstadoPostulacion e) {
        validar(e);
        if (e.getId() <= 0) throw new RuntimeException("Id de estado de postulacion no valido");
        return daoEstado.modificar(e);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de estado de postulacion no valido");
        return daoEstado.eliminar(id);
    }

    @Override
    public List<EstadoPostulacion> listarTodos() {
        return daoEstado.listarTodos();
    }

    @Override
    public EstadoPostulacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de estado de postulacion no valido");
        return daoEstado.buscarPorId(id);
    }
}
