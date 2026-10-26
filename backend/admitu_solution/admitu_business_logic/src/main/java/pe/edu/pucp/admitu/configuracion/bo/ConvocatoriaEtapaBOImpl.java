package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaEtapa;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaEtapaBO;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaEtapaDAO;
import pe.edu.pucp.admitu.configuracion.impl.ConvocatoriaEtapaImpl;

import java.util.List;

public class ConvocatoriaEtapaBOImpl implements IConvocatoriaEtapaBO {

    private ConvocatoriaEtapaDAO daoConvocatoriaEtapa;

    public ConvocatoriaEtapaBOImpl() {
        daoConvocatoriaEtapa = new ConvocatoriaEtapaImpl();
    }

    private void validar(ConvocatoriaEtapa r) {
        if (r == null) throw new IllegalArgumentException("La relacion convocatoria-etapa es null");
        if (r.getConvocatoria() == null || r.getConvocatoria().getId() <= 0)
            throw new IllegalArgumentException("La relacion debe pertenecer a una convocatoria valida");
        if (r.getEtapa() == null || r.getEtapa().getId() <= 0)
            throw new IllegalArgumentException("La relacion debe pertenecer a una etapa valida");
        Validaciones.rango(r.getFechaInicio(), r.getFechaFin(), "la etapa de la convocatoria", false);
    }

    @Override
    public int insertar(ConvocatoriaEtapa r) {
        validar(r);
        try {
            int resultado = daoConvocatoriaEtapa.insertar(r);
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
    public int modificar(ConvocatoriaEtapa r) {
        validar(r);
        if (r.getId() <= 0) throw new RuntimeException("Id de relacion no valido");
        return daoConvocatoriaEtapa.modificar(r);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de relacion no valido");
        return daoConvocatoriaEtapa.eliminar(id);
    }

    @Override
    public List<ConvocatoriaEtapa> listarTodos() {
        return daoConvocatoriaEtapa.listarTodos();
    }

    @Override
    public ConvocatoriaEtapa buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de relacion no valido");
        return daoConvocatoriaEtapa.buscarPorId(id);
    }
}
