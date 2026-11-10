package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
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
        if (c == null) throw new IllegalArgumentException("La convocatoria es null");
        if (c.getEstado() == null) throw new IllegalArgumentException("La convocatoria no tiene estado");
        Validaciones.codigo(c.getCodigoConvocatoria(), "Codigo de convocatoria", 20);
        Validaciones.texto(c.getNombre(), "Nombre de convocatoria", 150);
        Validaciones.rango(c.getFechaInicio(), c.getFechaFin(), "la convocatoria", false);
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
