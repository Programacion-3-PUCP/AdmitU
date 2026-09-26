package pe.edu.pucp.admitu.persona.dao;

import pe.edu.pucp.admitu.dao.IDAO;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;

import java.util.List;

public interface AntecedenteAcademicoDAO extends IDAO<AntecedenteAcademico> {
    List<AntecedenteAcademico> listarPorPostulante(int idPostulante);
}
