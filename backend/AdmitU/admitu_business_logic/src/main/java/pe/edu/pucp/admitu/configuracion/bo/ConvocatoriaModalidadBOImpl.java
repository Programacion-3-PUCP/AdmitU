package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaModalidadBO;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaModalidadDAO;
import pe.edu.pucp.admitu.configuracion.impl.ConvocatoriaModalidadImpl;

import java.util.List;

public class ConvocatoriaModalidadBOImpl implements IConvocatoriaModalidadBO {

    private ConvocatoriaModalidadDAO daoConvocatoriaModalidad;

    public ConvocatoriaModalidadBOImpl() {
        daoConvocatoriaModalidad = new ConvocatoriaModalidadImpl();
    }

    private void validar(ConvocatoriaModalidad r) {
        if (r == null) throw new IllegalArgumentException("La relacion convocatoria-modalidad es null");
        if (r.getConvocatoria() == null || r.getConvocatoria().getId() <= 0)
            throw new IllegalArgumentException("La relacion debe pertenecer a una convocatoria valida");
        if (r.getModalidad() == null || r.getModalidad().getId() <= 0)
            throw new IllegalArgumentException("La relacion debe pertenecer a una modalidad valida");
        Validaciones.monto(r.getCostoInscripcion(), "Costo de inscripcion", true);
        Validaciones.longitud(r.getObservacion(), "Observacion", 255);
    }

    @Override
    public int insertar(ConvocatoriaModalidad r) {
        validar(r);
        try {
            int resultado = daoConvocatoriaModalidad.insertar(r);
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
    public int modificar(ConvocatoriaModalidad r) {
        validar(r);
        if (r.getId() <= 0) throw new RuntimeException("Id de relacion no valido");
        return daoConvocatoriaModalidad.modificar(r);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de relacion no valido");
        return daoConvocatoriaModalidad.eliminar(id);
    }

    @Override
    public List<ConvocatoriaModalidad> listarTodos() {
        return daoConvocatoriaModalidad.listarTodos();
    }

    @Override
    public ConvocatoriaModalidad buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de relacion no valido");
        return daoConvocatoriaModalidad.buscarPorId(id);
    }
}
