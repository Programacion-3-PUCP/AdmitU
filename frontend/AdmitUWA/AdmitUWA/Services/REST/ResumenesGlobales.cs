using System.Net.Http.Json;

namespace AdmitUWA.Services.REST;

// Contrato explícito de la vista global: sin personas, pagos, archivos ni texto libre.
public sealed class ExpedienteResumenDto
{
    public int Id { get; set; }
    public string CodigoInscripcion { get; set; } = "";
    public int ConvocatoriaId { get; set; }
    public int ModalidadElegidaId { get; set; }
    public int CarreraElegidaId { get; set; }
    public int EstadoActualId { get; set; }
    public int? EvaluadorId { get; set; }
    public DateOnly? FechaRegistro { get; set; }
    public DateOnly? FechaEnvio { get; set; }
    public DateOnly? FechaFinalizacion { get; set; }
    public List<ObservacionResumenDto> Observaciones { get; set; } = [];
}

public sealed class ObservacionResumenDto
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public int RequisitoAplicableId { get; set; }
    public int? EvaluadorId { get; set; }
    public string TipoObservacion { get; set; } = "";
    public string EstadoObservacion { get; set; } = "";
    public DateOnly? FechaObservacion { get; set; }
    public DateOnly? FechaSubsanacion { get; set; }
}

public partial class AdmitURestClient
{
    public async Task<List<ExpedienteResumenDto>> ListarResumenesGlobalesAsync(CancellationToken cancelacion = default)
    {
        if (!Autenticado || Rol is not ("admin" or "evaluador")) return [];
        await _lecturas.WaitAsync(cancelacion).ConfigureAwait(false);
        try
        {
            using var response = await _httpClient.GetAsync("PostulacionRS/listarResumenesGlobales", cancelacion).ConfigureAwait(false);
            response.EnsureSuccessStatusCode();
            return await response.Content.ReadFromJsonAsync<List<ExpedienteResumenDto>>(_jsonOptions, cancelacion).ConfigureAwait(false) ?? [];
        }
        finally { _lecturas.Release(); }
    }
}
