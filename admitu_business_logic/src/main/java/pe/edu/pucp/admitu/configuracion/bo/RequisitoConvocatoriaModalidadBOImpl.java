package pe.edu.pucp.admitu.configuracion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.configuracion.RequisitoConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.boi.IRequisitoConvocatoriaModalidadBO;
import pe.edu.pucp.admitu.configuracion.dao.RequisitoConvocatoriaModalidadDAO;
import pe.edu.pucp.admitu.configuracion.impl.RequisitoConvocatoriaModalidadImpl;

import java.util.List;

public class RequisitoConvocatoriaModalidadBOImpl implements IRequisitoConvocatoriaModalidadBO {

    private RequisitoConvocatoriaModalidadDAO daoRelacion;

    public RequisitoConvocatoriaModalidadBOImpl() {
        daoRelacion = new RequisitoConvocatoriaModalidadImpl();
    }

    private void validar(RequisitoConvocatoriaModalidad r) {
        if (r == null) throw new RuntimeException("La relacion requisito-modalidad es null");
        if (r.getConvocatoriaModalidad() == null || r.getConvocatoriaModalidad().getId() <= 0)
            throw new RuntimeException("La relacion debe pertenecer a una convocatoria-modalidad valida");
        if (r.getRequisito() == null || r.getRequisito().getId() <= 0)
            throw new RuntimeException("La relacion debe pertenecer a un requisito valido");
        if (r.isObligatorio() && r.getOrdenPresentacion() == null)
            throw new RuntimeException("Un requisito obligatorio necesita orden de presentacion");
        if (r.getOrdenPresentacion() != null && r.getOrdenPresentacion() <= 0)
            throw new RuntimeException("El orden de presentacion debe ser mayor que cero");
                if (r.getOrdenPresentacion() != null) Validaciones.positivo(r.getOrdenPresentacion(), "Orden de presentación");
    }

    @Override
    public int insertar(RequisitoConvocatoriaModalidad r) {
        validar(r);
        return TransactionContext.ejecutar(() -> daoRelacion.insertar(r));
    }

    @Override
    public int modificar(RequisitoConvocatoriaModalidad r) {
        validar(r);
        if (r.getId() <= 0) throw new RuntimeException("Id de relacion no valido");
        return TransactionContext.ejecutar(() -> daoRelacion.modificar(r));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de relacion no valido");
        return TransactionContext.ejecutar(() -> daoRelacion.eliminar(id));
    }

    @Override
    public List<RequisitoConvocatoriaModalidad> listarTodos() {
        return TransactionContext.ejecutar(() -> daoRelacion.listarTodos());
    }

    @Override
    public RequisitoConvocatoriaModalidad buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de relacion no valido");
        return TransactionContext.ejecutar(() -> daoRelacion.buscarPorId(id));
    }
}
