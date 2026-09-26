package pe.edu.pucp.admitu.dao;

import java.util.List;

public interface IDAO<T> {
    boolean insertar(T objeto);
    boolean actualizar(T objeto);
    boolean eliminar(int idObjeto);
    T buscarPorId(int idObjeto);
    List<T> listar();
}
