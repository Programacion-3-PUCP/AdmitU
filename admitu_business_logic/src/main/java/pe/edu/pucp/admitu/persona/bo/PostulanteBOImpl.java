package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.persona.boi.IPostulanteBO;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;
import pe.edu.pucp.admitu.persona.impl.PostulanteImpl;
import pe.edu.pucp.admitu.persona.Postulante;

import java.util.List;
import java.time.LocalDate;

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
        if (p.getApoderado() != null && p.getApoderado().getId() <= 0)
            throw new IllegalArgumentException("El apoderado debe existir antes de asociarlo");
        Validaciones.persona(p.getNombres(), p.getApellidoPaterno(), p.getApellidoMaterno(),
                p.getCorreo(), p.getTipoDocumento(), p.getNumeroDocumento(), p.getTelefono());
        Validaciones.noFutura(p.getFechaNacimiento(), "Fecha de nacimiento");
        if (p.getFechaNacimiento().isBefore(LocalDate.now().minusYears(110)))
            throw new IllegalArgumentException("Fecha de nacimiento fuera de rango");
        if (p.isTieneDiscapacidad())
            Validaciones.texto(p.getNumeroCarnetConadis(), "Número CONADIS", 30);
        else if (p.getNumeroCarnetConadis() != null && !p.getNumeroCarnetConadis().isBlank())
            throw new IllegalArgumentException("No corresponde un carné CONADIS");
        if (p.isCorreoValidado() && p.getFechaValidacionCorreo() == null)
            throw new IllegalArgumentException("Falta fecha de validación del correo");
        Validaciones.noFutura(p.getFechaValidacionCorreo(), "Validación de correo");
    }

    @Override
    public int insertar(Postulante p) {
        validar(p);
        return TransactionContext.ejecutar(() -> daoPostulante.insertar(p));
    }

    @Override
    public int modificar(Postulante p) {
        validar(p);
        if (p.getId() <= 0) throw new RuntimeException("Id de postulante no valido");
        return TransactionContext.ejecutar(() -> daoPostulante.modificar(p));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulante no valido");
        return TransactionContext.ejecutar(() -> daoPostulante.eliminar(id));
    }

    @Override
    public List<Postulante> listarTodos() {
        return TransactionContext.ejecutar(() -> daoPostulante.listarTodos());
    }

    @Override
    public Postulante buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de postulante no valido");
        return TransactionContext.ejecutar(() -> daoPostulante.buscarPorId(id));
    }
}
