package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.persona.Pais;
import pe.edu.pucp.admitu.persona.boi.IPaisBO;
import pe.edu.pucp.admitu.persona.dao.PaisDAO;
import pe.edu.pucp.admitu.persona.impl.PaisImpl;

import java.util.List;

public class PaisBOImpl implements IPaisBO {

    private PaisDAO daoPais;

    public PaisBOImpl() {
        daoPais = new PaisImpl();
    }

    private void validar(Pais p) {
        if (p == null) throw new RuntimeException("El pais es null");
        if (p.getCodigoIso2() == null || p.getCodigoIso2().trim().isEmpty())
            throw new RuntimeException("El codigo ISO2 del pais no puede estar vacio");
        if (p.getCodigoIso2().trim().length() != 2)
            throw new RuntimeException("El codigo ISO2 debe tener 2 caracteres");
        if (p.getNombre() == null || p.getNombre().trim().isEmpty())
            throw new RuntimeException("El nombre del pais no puede estar vacio");
        if (p.getNombre().length() > 100)
            throw new RuntimeException("El nombre no debe exceder 100 caracteres");
                if (!p.getCodigoIso2().matches("[A-Za-z]{2}"))
            throw new IllegalArgumentException("El código ISO2 debe tener dos letras");
    }

    @Override
    public int insertar(Pais p) {
        validar(p);
        return TransactionContext.ejecutar(() -> daoPais.insertar(p));
    }

    @Override
    public int modificar(Pais p) {
        validar(p);
        if (p.getId() <= 0) throw new RuntimeException("Id de pais no valido");
        return TransactionContext.ejecutar(() -> daoPais.modificar(p));
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de pais no valido");
        return TransactionContext.ejecutar(() -> daoPais.eliminar(id));
    }

    @Override
    public List<Pais> listarTodos() {
        return TransactionContext.ejecutar(() -> daoPais.listarTodos());
    }

    @Override
    public Pais buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de pais no valido");
        return TransactionContext.ejecutar(() -> daoPais.buscarPorId(id));
    }
}
