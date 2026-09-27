package pe.edu.pucp.admitu.persona.bo;

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
        if (p == null) throw new RuntimeException("El postulante es null");
        if (p.getNombres() == null || p.getNombres().trim().isEmpty())
            throw new RuntimeException("El postulante no tiene nombres");
        if (p.getNombres().length() > 100)
            throw new RuntimeException("Los nombres no deben exceder 100 caracteres");
        if (p.getApellidoPaterno() == null || p.getApellidoPaterno().trim().isEmpty())
            throw new RuntimeException("El postulante no tiene apellido paterno");
        if (p.getCorreo() == null || p.getCorreo().trim().isEmpty())
            throw new RuntimeException("El postulante no tiene correo");
        if (p.getTipoDocumento() == null)
            throw new RuntimeException("El postulante no tiene tipo de documento");
        if (p.getNumeroDocumento() == null || p.getNumeroDocumento().trim().isEmpty())
            throw new RuntimeException("El postulante no tiene numero de documento");
        if (p.getFechaNacimiento() == null)
            throw new RuntimeException("El postulante no tiene fecha de nacimiento");
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
