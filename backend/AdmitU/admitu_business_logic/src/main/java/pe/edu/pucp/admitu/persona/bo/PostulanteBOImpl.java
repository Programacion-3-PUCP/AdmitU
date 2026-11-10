package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.boi.IPostulanteBO;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;
import pe.edu.pucp.admitu.persona.impl.PostulanteImpl;
import pe.edu.pucp.admitu.persona.Postulante;

import java.util.List;

public class PostulanteBOImpl implements IPostulanteBO {

    private PostulanteDAO daoPostulante;

    public PostulanteBOImpl() {
        daoPostulante = new PostulanteImpl();
    }

    private void validar(Postulante p) {
        if (p == null) throw new IllegalArgumentException("El postulante es null");
        if (p.getFechaNacimiento() == null)
            throw new IllegalArgumentException("El postulante no tiene fecha de nacimiento");
        Validaciones.persona(p.getNombres(), p.getApellidoPaterno(), p.getApellidoMaterno(),
                p.getCorreo(), p.getTipoDocumento(), p.getNumeroDocumento(), p.getTelefono());
        Validaciones.noFutura(p.getFechaNacimiento(), "La fecha de nacimiento");
    }

    @Override
    public int insertar(Postulante p) {
        validar(p);
        try {
            int r = daoPostulante.insertar(p);
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
    public int modificar(Postulante p) {
        validar(p);
        if (p.getId() <= 0) throw new RuntimeException("Id de postulante no valido");
        return daoPostulante.modificar(p);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulante no valido");
        return daoPostulante.eliminar(id);
    }

    @Override
    public List<Postulante> listarTodos() {
        return daoPostulante.listarTodos();
    }

    @Override
    public Postulante buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulante no valido");
        return daoPostulante.buscarPorId(id);
    }
}
