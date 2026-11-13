package pe.edu.pucp.admitu.postulacion;
import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
public class EnvioPostulacion {
 private Postulacion postulacion;private int sedeId;private List<ArchivoDigital> documentos;
 public Postulacion getPostulacion(){return postulacion;}public void setPostulacion(Postulacion p){postulacion=p;}
 public int getSedeId(){return sedeId;}public void setSedeId(int id){sedeId=id;}
 public List<ArchivoDigital> getDocumentos(){return documentos;}public void setDocumentos(List<ArchivoDigital> list){documentos=list;}
}
