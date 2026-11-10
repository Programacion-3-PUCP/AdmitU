package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
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
        if (e == null) throw new IllegalArgumentException("La etapa es null");
        Validaciones.codigo(e.getCodigoEtapa(), "Codigo de etapa", 20);
        Validaciones.texto(e.getNombre(), "Nombre de etapa", 100);
        Validaciones.longitud(e.getDescripcion(), "Descripcion", 255);
        Validaciones.rango(e.getFechaInicio(), e.getFechaFin(), "la etapa", false);
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
