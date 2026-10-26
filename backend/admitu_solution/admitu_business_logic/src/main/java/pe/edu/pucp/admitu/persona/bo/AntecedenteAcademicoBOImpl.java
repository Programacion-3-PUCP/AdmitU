package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;
import pe.edu.pucp.admitu.persona.boi.IAntecedenteAcademicoBO;
import pe.edu.pucp.admitu.persona.dao.AntecedenteAcademicoDAO;
import pe.edu.pucp.admitu.persona.impl.AntecedenteAcademicoImpl;

import java.time.Year;
import java.util.List;

public class AntecedenteAcademicoBOImpl implements IAntecedenteAcademicoBO {

    private AntecedenteAcademicoDAO daoAntecedente;

    public AntecedenteAcademicoBOImpl() {
        daoAntecedente = new AntecedenteAcademicoImpl();
    }

    private void validar(AntecedenteAcademico a) {
        if (a == null) throw new IllegalArgumentException("El antecedente academico es null");
        if (a.getPostulante() == null || a.getPostulante().getId() <= 0)
            throw new IllegalArgumentException("El antecedente debe pertenecer a un postulante valido");
        if (a.getInstitucion() == null || a.getInstitucion().getId() <= 0)
            throw new IllegalArgumentException("El antecedente debe pertenecer a una institucion valida");
        int anioActual = Year.now().getValue();
        if (a.getAnioInicio() <= 0 || a.getAnioInicio() > anioActual)
            throw new IllegalArgumentException("El anio de inicio no es valido");
        if (a.getAnioFin() <= 0 || a.getAnioFin() > anioActual)
            throw new IllegalArgumentException("El anio de fin no es valido");
        if (a.getAnioFin() < a.getAnioInicio())
            throw new IllegalArgumentException("El anio de fin no puede ser anterior al anio de inicio");
        Validaciones.longitud(a.getDescripcion(), "Descripcion", 500);
    }

    @Override
    public int insertar(AntecedenteAcademico a) {
        validar(a);
        try {
            int r = daoAntecedente.insertar(a);
            TransactionContext.commit();
            return r;
        } catch (Exception ex) {
            TransactionContext.rollback();
            throw new RuntimeException("Error: " + ex.getMessage());
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int modificar(AntecedenteAcademico a) {
        validar(a);
        if (a.getId() <= 0) throw new RuntimeException("Id de antecedente academico no valido");
        return daoAntecedente.modificar(a);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de antecedente academico no valido");
        return daoAntecedente.eliminar(id);
    }

    @Override
    public List<AntecedenteAcademico> listarTodos() {
        return daoAntecedente.listarTodos();
    }

    @Override
    public AntecedenteAcademico buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de antecedente academico no valido");
        return daoAntecedente.buscarPorId(id);
    }
}
