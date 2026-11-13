package pe.edu.pucp.admitu.archivo.dao;
import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
public interface ArchivoDAO {
 ArchivoDigital guardar(ArchivoDigital archivo);
 void guardarCertificado(ArchivoDigital archivo);
 void guardarVoucher(ArchivoDigital archivo);
 List<ArchivoDigital> listarPorPostulante(int id);
 ArchivoDigital buscarPorId(int id);
 int eliminar(int id);
}
