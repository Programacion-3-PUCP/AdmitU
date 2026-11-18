package pe.edu.pucp.admitu.seguridad.bo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.persona.Evaluador;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.dao.EvaluadorDAO;
import pe.edu.pucp.admitu.persona.dao.PostulanteDAO;
import pe.edu.pucp.admitu.persona.impl.EvaluadorImpl;
import pe.edu.pucp.admitu.persona.impl.PostulanteImpl;
import pe.edu.pucp.admitu.seguridad.CuentaUsuario;
import pe.edu.pucp.admitu.seguridad.SesionUsuario;
import pe.edu.pucp.admitu.seguridad.boi.IAutenticacionBO;
import pe.edu.pucp.admitu.seguridad.dao.CuentaUsuarioDAO;
import pe.edu.pucp.admitu.seguridad.impl.CuentaUsuarioImpl;

public class AutenticacionBOImpl implements IAutenticacionBO {

    private static final String HASH_INEXISTENTE =
            "pbkdf2_sha256$600000$AAAAAAAAAAAAAAAAAAAAAA==$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private CuentaUsuarioDAO daoCuentaUsuario;
    private PostulanteDAO daoPostulante;
    private EvaluadorDAO daoEvaluador;

    public AutenticacionBOImpl() {
        daoCuentaUsuario = new CuentaUsuarioImpl();
        daoPostulante = new PostulanteImpl();
        daoEvaluador = new EvaluadorImpl();
    }

    @Override
    public SesionUsuario ingresar(String correo, String clave) {
        if (correo == null || correo.length() > 150 || clave == null || clave.length() > 128) {
            throw new SecurityException("Credenciales incorrectas");
        }

        CuentaUsuario cuenta = daoCuentaUsuario.buscarCorreo(correo.trim().toLowerCase());
        if (cuenta == null) {
            ClaveSegura.verificar(clave, HASH_INEXISTENTE);
            throw new SecurityException("Credenciales incorrectas");
        }
        if (!cuenta.isActivo() || cuenta.isBloqueada()) {
            throw new SecurityException("Credenciales incorrectas o cuenta temporalmente bloqueada");
        }

        boolean valido = ClaveSegura.verificar(clave, cuenta.getClaveHash());
        daoCuentaUsuario.intento(cuenta.getId(), valido);
        if (!valido) {
            throw new SecurityException("Credenciales incorrectas");
        }

        String token = ClaveSegura.token();
        String huella = ClaveSegura.huella(token);
        daoCuentaUsuario.crearSesion(huella, cuenta.getId());

        SesionUsuario sesion = daoCuentaUsuario.buscarSesion(huella);
        if (sesion == null) {
            throw new SecurityException("Cuenta no disponible");
        }
        sesion.setToken(token);
        return sesion;
    }

    @Override
    public int registrar(Postulante postulante, String clave) {
        if (postulante == null) {
            throw new IllegalArgumentException("Datos del postulante requeridos");
        }
        String hash = ClaveSegura.crear(clave);
        Validaciones.persona(
                postulante.getNombres(),
                postulante.getApellidoPaterno(),
                postulante.getApellidoMaterno(),
                postulante.getCorreo(),
                postulante.getTipoDocumento(),
                postulante.getNumeroDocumento(),
                postulante.getTelefono()
        );

        LocalDate hoyLima = LocalDate.now(ZoneId.of("America/Lima"));
        if (postulante.getFechaNacimiento() == null || postulante.getFechaNacimiento().isAfter(hoyLima)) {
            throw new IllegalArgumentException("Fecha de nacimiento no válida");
        }
        if (postulante.isTieneDiscapacidad()
                && (postulante.getNumeroCarnetConadis() == null || postulante.getNumeroCarnetConadis().trim().isEmpty())) {
            throw new IllegalArgumentException("Indica el carné CONADIS");
        }

        try {
            int id = daoPostulante.insertar(postulante);
            daoCuentaUsuario.crearCuenta(id, postulante.getCorreo(), "postulante", hash);
            TransactionContext.commit();
            return id;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error al registrar la cuenta: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public int registrarEvaluador(Evaluador evaluador, String clave) {
        if (evaluador == null) {
            throw new IllegalArgumentException("Datos del evaluador requeridos");
        }
        String hash = ClaveSegura.crear(clave);
        Validaciones.persona(
                evaluador.getNombres(),
                evaluador.getApellidoPaterno(),
                evaluador.getApellidoMaterno(),
                evaluador.getCorreo(),
                evaluador.getTipoDocumento(),
                evaluador.getNumeroDocumento(),
                evaluador.getTelefono()
        );
        Validaciones.texto(evaluador.getCargo(), "Cargo del evaluador", 100);

        try {
            int id = daoEvaluador.insertar(evaluador);
            daoCuentaUsuario.crearCuenta(id, evaluador.getCorreo(), "evaluador", hash);
            TransactionContext.commit();
            return id;
        } catch (Exception ex) {
            TransactionContext.rollback();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error al registrar el evaluador y su cuenta: " + ex.getMessage(), ex);
        } finally {
            TransactionContext.close();
        }
    }

    @Override
    public SesionUsuario autenticar(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            return null;
        }
        return daoCuentaUsuario.buscarSesion(ClaveSegura.huella(token));
    }

    @Override
    public void salir(String token) {
        if (token != null && token.length() == 43) {
            daoCuentaUsuario.eliminarSesion(ClaveSegura.huella(token));
        }
    }

    @Override
    public Set<Integer> idsAutorizados(String recurso, int persona) {
        return daoCuentaUsuario.ids(recurso, persona);
    }
}
