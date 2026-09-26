package pe.edu.pucp.admitu.BO;

import java.util.List;

public interface IBaseBO <T>{
    int insertar(T objeto) throws RuntimeException;
    int modificar(T objeto) throws RuntimeException;
    int eliminar(int idObjeto) throws RuntimeException ;
    List<T> listarTodos() throws RuntimeException;
    T buscarPorId(int idObjeto) throws RuntimeException;
}
