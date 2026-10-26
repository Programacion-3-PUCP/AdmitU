package pe.edu.pucp.admitu.persona.bo;

import pe.edu.pucp.admitu.bo.Validaciones;
import pe.edu.pucp.admitu.config.TransactionContext;
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
        if (p == null) throw new IllegalArgumentException("El pais es null");
        Validaciones.texto(p.getCodigoIso2(), "Codigo ISO2 del pais", 2);
        if (p.getCodigoIso2().trim().length() != 2)
            throw new IllegalArgumentException("El codigo ISO2 debe tener exactamente 2 caracteres");
        Validaciones.texto(p.getNombre(), "Nombre del pais", 100);
    }

    @Override
    public int insertar(Pais p) {
        validar(p);
        try {
            int r = daoPais.insertar(p);
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
    public int modificar(Pais p) {
        validar(p);
        if (p.getId() <= 0) throw new RuntimeException("Id de pais no valido");
        return daoPais.modificar(p);
    }

    @Override
    public int eliminar(int id) {
        if (id <= 0) throw new RuntimeException("Id de pais no valido");
        return daoPais.eliminar(id);
    }

    @Override
    public List<Pais> listarTodos() {
        return daoPais.listarTodos();
    }

    @Override
    public Pais buscarPorId(int id) {
        if (id <= 0) throw new RuntimeException("Id de pais no valido");
        return daoPais.buscarPorId(id);
    }
}
