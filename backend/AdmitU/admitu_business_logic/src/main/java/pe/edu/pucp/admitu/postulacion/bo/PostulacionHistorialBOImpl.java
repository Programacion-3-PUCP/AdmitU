package pe.edu.pucp.admitu.postulacion.bo;

import pe.edu.pucp.admitu.postulacion.PostulacionHistorial;
import pe.edu.pucp.admitu.postulacion.boi.IPostulacionHistorialBO;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionHistorialDAO;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionHistorialImpl;

import java.util.List;

public class PostulacionHistorialBOImpl implements IPostulacionHistorialBO {

    private PostulacionHistorialDAO daoHistorial;

    public PostulacionHistorialBOImpl() {
        daoHistorial = new PostulacionHistorialImpl();
    }



    @Override
    public int insertar(PostulacionHistorial h) {
        throw new UnsupportedOperationException("El historial es inmutable; se genera únicamente por transiciones del servidor");
    }

    @Override
    public int modificar(PostulacionHistorial h) {
        throw new UnsupportedOperationException("El historial es inmutable; se genera únicamente por transiciones del servidor");
    }

    @Override
    public int eliminar(int id) {
        throw new UnsupportedOperationException("El historial es inmutable; se genera únicamente por transiciones del servidor");
    }

    @Override
    public List<PostulacionHistorial> listarTodos() {
        return daoHistorial.listarTodos();
    }

    @Override
    public PostulacionHistorial buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de historial no valido");
        return daoHistorial.buscarPorId(id);
    }
}
