package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.boi.IConvocatoriaBO;
import pe.edu.pucp.admitu.configuracion.dao.ConvocatoriaDAO;
import pe.edu.pucp.admitu.configuracion.impl.ConvocatoriaImpl;
import pe.edu.pucp.admitu.configuracion.Convocatoria;

import java.util.List;

public class ConvocatoriaBOImpl implements IConvocatoriaBO {

    private ConvocatoriaDAO daoConvocatoria;

    public ConvocatoriaBOImpl() {
        daoConvocatoria = new ConvocatoriaImpl();
    }

    private void validar(Convocatoria c) {
        if (c == null) throw new RuntimeException("La convocatoria es null");
        if (c.getCodigoConvocatoria() == null || c.getCodigoConvocatoria().trim().isEmpty())
            throw new RuntimeException("El codigo de convocatoria no puede estar vacio");
        if (c.getCodigoConvocatoria().length() > 20)
            throw new RuntimeException("El codigo no debe exceder 20 caracteres");
        if (c.getNombre() == null || c.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre no puede estar vacio");
        if (c.getNombre().length() > 150)
            throw new RuntimeException("El nombre no debe exceder 150 caracteres");
        if (c.getFechaInicio() != null && c.getFechaFin() != null
                && c.getFechaInicio().isAfter(c.getFechaFin()))
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha fin");
        if (c.getEstado() == null) throw new RuntimeException("La convocatoria no tiene estado");
    }

    @Override
    public int insertar(Convocatoria c) {
        validar(c);
        try {
            int r = daoConvocatoria.insertar(c);
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
    public int modificar(Convocatoria c) {
        validar(c);
        if (c.getId() <= 0) throw new RuntimeException("Id de convocatoria no valido");
        return daoConvocatoria.modificar(c);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de convocatoria no valido");
        return daoConvocatoria.eliminar(id);
    }

    @Override
    public List<Convocatoria> listarTodos() {
        return daoConvocatoria.listarTodos();
    }

    @Override
    public Convocatoria buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de convocatoria no valido");
        return daoConvocatoria.buscarPorId(id);
    }
}
