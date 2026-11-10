package pe.edu.pucp.admitu.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pe.edu.pucp.admitu.persona.AntecedenteAcademico;
import pe.edu.pucp.admitu.persona.bo.AntecedenteAcademicoBOImpl;
import pe.edu.pucp.admitu.persona.boi.IAntecedenteAcademicoBO;

@Path("AntecedenteAcademicoRS")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AntecedenteAcademicoRS {

    private final IAntecedenteAcademicoBO boAntecedente;

    public AntecedenteAcademicoRS() {
        boAntecedente = new AntecedenteAcademicoBOImpl();
    }

    @GET
    @Path("listarTodosAntecedentesAcademicos")
    public List<AntecedenteAcademico> listarTodosAntecedentesAcademicos() {
        return boAntecedente.listarTodos();
    }

    @POST
    @Path("insertarAntecedenteAcademico")
    public int insertarAntecedenteAcademico(AntecedenteAcademico antecedente) {
        return boAntecedente.insertar(antecedente);
    }

    @GET
    @Path("buscarAntecedenteAcademicoPorId/{id}")
    public AntecedenteAcademico buscarAntecedenteAcademicoPorId(@PathParam("id") int idAntecedente) {
        return boAntecedente.buscarPorId(idAntecedente);
    }

    @PUT
    @Path("modificarAntecedenteAcademico")
    public int modificarAntecedenteAcademico(AntecedenteAcademico antecedente) {
        return boAntecedente.modificar(antecedente);
    }

    @DELETE
    @Path("eliminarAntecedenteAcademico/{id}")
    public int eliminarAntecedenteAcademico(@PathParam("id") int idAntecedente) {
        return boAntecedente.eliminar(idAntecedente);
    }
}
