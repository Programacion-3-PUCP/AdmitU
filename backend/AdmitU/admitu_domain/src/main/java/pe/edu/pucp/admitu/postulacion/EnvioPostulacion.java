package pe.edu.pucp.admitu.postulacion;

import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;

public class EnvioPostulacion {
    private Postulacion postulacion;
    private int sedeId;
    private List<ArchivoDigital> documentos;

    public EnvioPostulacion() {
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public int getSedeId() {
        return sedeId;
    }

    public void setSedeId(int sedeId) {
        this.sedeId = sedeId;
    }

    public List<ArchivoDigital> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<ArchivoDigital> documentos) {
        this.documentos = documentos;
    }
}
