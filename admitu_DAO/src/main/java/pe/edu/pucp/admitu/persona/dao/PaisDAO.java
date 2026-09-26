package pe.edu.pucp.admitu.persona.dao;

import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.persona.Pais;

public interface PaisDAO extends IDAO<Pais> {
    Pais buscarPorCodigoIso2(String codigoIso2);
}
