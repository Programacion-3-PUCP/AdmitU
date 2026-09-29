package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Etapa;
import pe.edu.pucp.admitu.configuracion.boi.IEtapaBO;
import pe.edu.pucp.admitu.configuracion.dao.EtapaDAO;
import pe.edu.pucp.admitu.configuracion.impl.EtapaImpl;

import java.util.List;

public class EtapaBOImpl implements IEtapaBO {

    private EtapaDAO daoEtapa;

    public EtapaBOImpl() {
        daoEtapa = new EtapaImpl();
    }

    private void validar(Etapa e) {
        if (e == null) throw new RuntimeException("La etapa es null");
        if (e.getCodigoEtapa() == null || e.getCodigoEtapa().trim().isEmpty())
            throw new RuntimeException("El codigo de etapa no puede estar vacio");
        if (e.getCodigoEtapa().length() > 20)
            throw new RuntimeException("El codigo no debe exceder 20 caracteres");
        if (e.getNombre() == null || e.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre de etapa no puede estar vacio");
        if (e.getNombre().length() > 100)
            throw new RuntimeException("El nombre no debe exceder 100 caracteres");
        if (e.getDescripcion() != null && e.getDescripcion().length() > 255)
            throw new RuntimeException("La descripcion no debe exceder 255 caracteres");
        if (e.getFechaInicio() != null && e.getFechaFin() != null && e.getFechaInicio().isAfter(e.getFechaFin()))
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    @Override
    public int insertar(Etapa e) {
        validar(e);
        try {
            int r = daoEtapa.insertar(e);
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
    public int modificar(Etapa e) {
        validar(e);
        if (e.getId() <= 0) throw new RuntimeException("Id de etapa no valido");
        return daoEtapa.modificar(e);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de etapa no valido");
        return daoEtapa.eliminar(id);
    }

    @Override
    public List<Etapa> listarTodos() {
        return daoEtapa.listarTodos();
    }

    @Override
    public Etapa buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de etapa no valido");
        return daoEtapa.buscarPorId(id);
    }
}
