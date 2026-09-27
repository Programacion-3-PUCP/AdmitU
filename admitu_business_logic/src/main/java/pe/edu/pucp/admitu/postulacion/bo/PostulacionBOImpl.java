package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionImpl;
import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.util.List;

public class PostulacionBOImpl implements IPostulacionBO {

    private PostulacionDAO daoPostulacion;

    public PostulacionBOImpl() {
        daoPostulacion = new PostulacionImpl();
    }

    private void validar(Postulacion p) {
        if (p == null) throw new RuntimeException("La postulacion es null");
        if (p.getPostulante() == null || p.getPostulante().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene postulante valido");
        if (p.getConvocatoria() == null || p.getConvocatoria().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene convocatoria valida");
        if (p.getModalidadElegida() == null || p.getModalidadElegida().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene modalidad valida");
        if (p.getCarreraElegida() == null || p.getCarreraElegida().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene oferta de carrera valida");
        if (p.getEstadoActual() == null || p.getEstadoActual().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene estado valido");
        if (p.getCodigoInscripcion() == null || p.getCodigoInscripcion().trim().isEmpty())
            throw new RuntimeException("El codigo de inscripcion no puede estar vacio");
    }

    @Override
    public int insertar(Postulacion p) {
        validar(p);
        try {
            int r = daoPostulacion.insertar(p);
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
    public int modificar(Postulacion p) {
        if (p == null) throw new RuntimeException("La postulacion es null");
        if (p.getId() <= 0) throw new RuntimeException("Id de postulacion no valido");
        if (p.getEstadoActual() == null || p.getEstadoActual().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene estado valido");
        return daoPostulacion.modificar(p);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulacion no valido");
        return daoPostulacion.eliminar(id);
    }

    @Override
    public List<Postulacion> listarTodos() {
        return daoPostulacion.listarTodos();
    }

    @Override
    public Postulacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulacion no valido");
        return daoPostulacion.buscarPorId(id);
    }
}
