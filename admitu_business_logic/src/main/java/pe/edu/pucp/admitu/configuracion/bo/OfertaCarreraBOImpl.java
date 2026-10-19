package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.configuracion.OfertaCarrera;
import pe.edu.pucp.admitu.configuracion.boi.IOfertaCarreraBO;
import pe.edu.pucp.admitu.configuracion.dao.OfertaCarreraDAO;
import pe.edu.pucp.admitu.configuracion.impl.OfertaCarreraImpl;

import java.util.List;

public class OfertaCarreraBOImpl implements IOfertaCarreraBO {

    private OfertaCarreraDAO daoOfertaCarrera;

    public OfertaCarreraBOImpl() {
        daoOfertaCarrera = new OfertaCarreraImpl();
    }

    private void validar(OfertaCarrera o) {
        if (o == null) throw new RuntimeException("La oferta de carrera es null");
        if (o.getConvocatoria() == null || o.getConvocatoria().getId() <= 0)
            throw new RuntimeException("La oferta debe pertenecer a una convocatoria valida");
        if (o.getCarrera() == null || o.getCarrera().getId() <= 0)
            throw new RuntimeException("La oferta debe pertenecer a una carrera valida");
        if (o.getCantidadVacantes() < 0)
            throw new RuntimeException("La cantidad de vacantes no puede ser negativa");
        Validaciones.positivo(o.getCantidadVacantes(), "Cantidad de vacantes");
    }

    @Override
    public int insertar(OfertaCarrera o) {
        validar(o);
        return TransactionContext.ejecutar(() -> daoOfertaCarrera.insertar(o));
    }

    @Override
    public int modificar(OfertaCarrera o) {
        validar(o);
        if (o.getId() <= 0) throw new RuntimeException("Id de oferta de carrera no valido");
        return TransactionContext.ejecutar(() -> daoOfertaCarrera.modificar(o));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de oferta de carrera no valido");
        return TransactionContext.ejecutar(() -> daoOfertaCarrera.eliminar(id));
    }

    @Override
    public List<OfertaCarrera> listarTodos() {
        return TransactionContext.ejecutar(() -> daoOfertaCarrera.listarTodos());
    }

    @Override
    public OfertaCarrera buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de oferta de carrera no valido");
        return TransactionContext.ejecutar(() -> daoOfertaCarrera.buscarPorId(id));
    }
}
