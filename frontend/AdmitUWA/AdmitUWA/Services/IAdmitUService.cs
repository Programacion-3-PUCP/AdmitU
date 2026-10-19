using AdmitUWA.Models;

namespace AdmitUWA.Services;

public interface IAdmitUService
{
    Task<List<PostulanteDto>> ListarPostulantesAsync();
    Task<PostulanteDto?> ObtenerPostulanteAsync(int id);
    Task GuardarPostulanteAsync(PostulanteDto modelo);
    Task EliminarPostulanteAsync(int id);

    Task<List<FacultadDto>> ListarFacultadesAsync();
    Task<FacultadDto?> ObtenerFacultadAsync(int id);
    Task GuardarFacultadAsync(FacultadDto modelo);
    Task DesactivarFacultadAsync(int id);

    Task<List<CarreraDto>> ListarCarrerasAsync();
    Task<CarreraDto?> ObtenerCarreraAsync(int id);
    Task GuardarCarreraAsync(CarreraDto modelo);
    Task DesactivarCarreraAsync(int id);

    Task<List<ConvocatoriaDto>> ListarConvocatoriasAsync();
    Task<ConvocatoriaDto?> ObtenerConvocatoriaAsync(int id);
    Task GuardarConvocatoriaAsync(ConvocatoriaDto modelo);
    Task DesactivarConvocatoriaAsync(int id);

    Task<List<ModalidadDto>> ListarModalidadesAsync();
    Task<ModalidadDto?> ObtenerModalidadAsync(int id);
    Task GuardarModalidadAsync(ModalidadDto modelo);
    Task DesactivarModalidadAsync(int id);

    Task<List<PostulacionDto>> ListarPostulacionesAsync();
    Task<List<PagoDto>> ListarPagosAsync();
    Task AprobarPagoAsync(int id);
    Task<List<DocumentoPostulacionDto>> ListarDocumentosAsync();
}
