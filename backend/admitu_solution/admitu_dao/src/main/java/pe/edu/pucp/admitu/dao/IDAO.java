package pe.edu.pucp.admitu.dao;
import java.util.List;
public interface IDAO<T> {
    int insertar(T objeto);
    int modificar(T objeto);
    int eliminar(int idObjeto);
    T buscarPorId(int idObjeto);
    List<T> listarTodos();
}
