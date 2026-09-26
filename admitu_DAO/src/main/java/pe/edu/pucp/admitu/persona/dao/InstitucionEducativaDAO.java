package pe.edu.pucp.admitu.persona.dao;

import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.persona.InstitucionEducativa;

import java.util.List;

public interface InstitucionEducativaDAO extends IDAO<InstitucionEducativa> {
    List<InstitucionEducativa> listarPorPais(int idPais);
}
