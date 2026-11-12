package pe.edu.pucp.admitu.postulacion.boi;

import pe.edu.pucp.admitu.bo.IBaseBO;
import pe.edu.pucp.admitu.postulacion.Postulacion;

public interface IPostulacionBO extends IBaseBO<Postulacion> {
    int enviar(pe.edu.pucp.admitu.postulacion.EnvioPostulacion envio);
    int asignarEvaluador(int id,int evaluadorId);
}
