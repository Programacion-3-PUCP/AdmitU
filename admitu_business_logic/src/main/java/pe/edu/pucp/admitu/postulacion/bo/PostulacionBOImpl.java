package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionBO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionImpl;
import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.util.List;
import pe.edu.pucp.admitu.pago.dao.PagoDAO;
import pe.edu.pucp.admitu.pago.impl.PagoImpl;

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
        Validaciones.codigo(p.getCodigoInscripcion(), "Código de inscripción", 30);
        Validaciones.longitud(p.getObservacionGeneral(), "Observación", 255);
        Validaciones.rango(p.getFechaRegistro(), p.getFechaEnvio(), "registro/envío", false);
        Validaciones.rango(p.getFechaEnvio(), p.getFechaFinalizacion(), "envío/finalización", false);
    }

    @Override
    public int insertar(Postulacion p) {
        validar(p);
        return TransactionContext.ejecutar(() -> daoPostulacion.insertar(p));
    }

    @Override
    public int modificar(Postulacion p) {
        if (p == null) throw new RuntimeException("La postulacion es null");
        if (p.getId() <= 0) throw new RuntimeException("Id de postulacion no valido");
        if (p.getEstadoActual() == null || p.getEstadoActual().getId() <= 0)
            throw new RuntimeException("La postulacion no tiene estado valido");
        Validaciones.longitud(p.getObservacionGeneral(), "Observación", 255);
        return TransactionContext.ejecutar(() -> daoPostulacion.modificar(p));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulacion no valido");
        return TransactionContext.ejecutar(() -> {
            PagoDAO pagos = new PagoImpl();
            if (pagos.existeAprobadoPorPostulacion(id))
                throw new IllegalArgumentException("No se puede eliminar una postulación con pagos aprobados");
            return daoPostulacion.eliminar(id);
        });
    }

    @Override
    public List<Postulacion> listarTodos() {
        return TransactionContext.ejecutar(() -> daoPostulacion.listarTodos());
    }

    @Override
    public Postulacion buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulacion no valido");
        return TransactionContext.ejecutar(() -> daoPostulacion.buscarPorId(id));
    }
}
