package pe.edu.pucp.admitu.postulacion.dao;

import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.postulacion.Postulacion;

public interface PostulacionDAO extends IDAO<Postulacion> {
    void validarEnvio(int id,int sedeId);
    int asignarEvaluador(int id,int evaluadorId);
}
