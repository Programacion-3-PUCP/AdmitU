package pe.edu.pucp.admitu.archivo.boi;
import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
public interface IArchivoBO {
 ArchivoDigital guardar(ArchivoDigital archivo);
 ArchivoDigital guardarEnTransaccion(ArchivoDigital archivo);
 ArchivoDigital guardarCertificado(ArchivoDigital archivo);
 ArchivoDigital guardarVoucher(ArchivoDigital archivo);
 List<ArchivoDigital> listarPorPostulante(int id);
 ArchivoDigital buscarPorId(int id);
 int eliminar(int id);
}
