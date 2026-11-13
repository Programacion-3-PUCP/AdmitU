using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace AdmitUWA.Services.REST;

// DTOs alineados 1:1 con las clases Java de admitu_domain serializadas por Jakarta JSON-B (camelCase)
public class FacultadDto
{
    public int Id { get; set; }
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class CarreraDto
{
    public int Id { get; set; }
    public FacultadDto? Facultad { get; set; }
    public string CodigoCarrera { get; set; } = "";
    public string Nombre { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class ModalidadDto
{
    public int Id { get; set; }
    public string CodigoModalidad { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string? Descripcion { get; set; }
    public bool RequiereColegio { get; set; } = true;
    public bool RequiereUniversidad { get; set; }
    public bool Activo { get; set; } = true;
}

public class SedeDto
{
    public int Id { get; set; }
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string? Direccion { get; set; }
    public bool Activo { get; set; } = true;
}

public class ConvocatoriaDto
{
    public int Id { get; set; }
    public string CodigoConvocatoria { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string Periodo { get; set; } = "";
    public DateOnly? FechaInicio { get; set; }
    public DateOnly? FechaFin { get; set; }
    public string Estado { get; set; } = "BORRADOR";
    public string? Descripcion { get; set; }
    public bool Activo { get; set; } = true;
}

public class OfertaCarreraDto
{
    public int Id { get; set; }
    public ConvocatoriaDto? Convocatoria { get; set; }
    public CarreraDto? Carrera { get; set; }
    public int CantidadVacantes { get; set; }
}

public class ConvocatoriaModalidadDto
{
    public int Id { get; set; }
    public ConvocatoriaDto? Convocatoria { get; set; }
    public ModalidadDto? Modalidad { get; set; }
    public double CostoInscripcion { get; set; }
    public string? Observacion { get; set; }
}

public class RequisitoDto
{
    public int Id { get; set; }
    public string CodigoRequisito { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string? Descripcion { get; set; }
    public string TipoArchivoRequerido { get; set; } = "PDF";
    public int TamanioMaximoBytes { get; set; } = 5242880;
    public bool Activo { get; set; } = true;
}

public class RequisitoConvocatoriaModalidadDto
{
    public int Id { get; set; }
    public ConvocatoriaModalidadDto? ConvocatoriaModalidad { get; set; }
    public RequisitoDto? Requisito { get; set; }
    public bool Obligatorio { get; set; } = true;
    public int? OrdenPresentacion { get; set; } = 1;
}

public class PostulanteDto
{
    public int Id { get; set; }
    public string Nombres { get; set; } = "";
    public string ApellidoPaterno { get; set; } = "";
    public string? ApellidoMaterno { get; set; }
    public string Correo { get; set; } = "";
    public string TipoDocumento { get; set; } = "DNI";
    public string NumeroDocumento { get; set; } = "";
    public string? Telefono { get; set; }
    public DateOnly? FechaNacimiento { get; set; }
    public bool TieneDiscapacidad { get; set; }
    public string? NumeroCarnetConadis { get; set; }
    public bool CorreoValidado { get; set; }
}

public class EvaluadorDto
{
    public int Id { get; set; }
    public string Nombres { get; set; } = "";
    public string ApellidoPaterno { get; set; } = "";
    public string? ApellidoMaterno { get; set; }
    public string Correo { get; set; } = "";
    public string TipoDocumento { get; set; } = "DNI";
    public string NumeroDocumento { get; set; } = "";
    public string? Telefono { get; set; }
    public string Cargo { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class PaisDto
{
    public int Id { get; set; }
    public string CodigoIso2 { get; set; } = "PE";
    public string Nombre { get; set; } = "Perú";
    public bool Activo { get; set; } = true;
}

public class InstitucionEducativaDto
{
    public int Id { get; set; }
    public PaisDto? Pais { get; set; }
    public string CodigoExterno { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string TipoInstitucion { get; set; } = "COLEGIO";
    public bool Activo { get; set; } = true;
}

public class AntecedenteAcademicoDto
{
    public int Id { get; set; }
    public PostulanteDto? Postulante { get; set; }
    public InstitucionEducativaDto? Institucion { get; set; }
    public int AnioInicio { get; set; }
    public int AnioFin { get; set; }
    public string? Descripcion { get; set; }
    public bool Activo { get; set; } = true;
}

public class EstadoPostulacionDto
{
    public int Id { get; set; }
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string? Descripcion { get; set; }
}

public class PostulacionDto
{
    public int? SedeId { get; set; }
    public int? EvaluadorId { get; set; }
    public int Id { get; set; }
    public PostulanteDto? Postulante { get; set; }
    public ConvocatoriaDto? Convocatoria { get; set; }
    public ConvocatoriaModalidadDto? ModalidadElegida { get; set; }
    public OfertaCarreraDto? CarreraElegida { get; set; }
    public EstadoPostulacionDto? EstadoActual { get; set; }
    public DateOnly? FechaRegistro { get; set; }
    public DateOnly? FechaEnvio { get; set; }
    public DateOnly? FechaFinalizacion { get; set; }
    public string CodigoInscripcion { get; set; } = "";
    public string? ObservacionGeneral { get; set; }
}

public class PostulacionHistorialDto
{
    public int Id { get; set; }
    public PostulacionDto? Postulacion { get; set; }
    public EstadoPostulacionDto? EstadoAnterior { get; set; }
    public EstadoPostulacionDto? EstadoActual { get; set; }
    public DateOnly? FechaCambio { get; set; }
    public string ResponsableCambio { get; set; } = "";
    public string MotivoCambio { get; set; } = "";
}

public class DocumentoPostulacionDto
{
    public int Id { get; set; }
    public PostulacionDto? Postulacion { get; set; }
    public RequisitoConvocatoriaModalidadDto? RequisitoAplicable { get; set; }
    public int NumeroVersion { get; set; } = 1;
    public string NombreArchivo { get; set; } = "";
    public string TipoArchivo { get; set; } = "PDF";
    public long TamanioArchivo { get; set; } = 102400;
    public string RutaArchivo { get; set; } = "";
    public bool ArchivoActivo { get; set; }
    public DateOnly? FechaCarga { get; set; }
    public string EstadoDocumento { get; set; } = "PENDIENTE";
    public DateOnly? FechaEvaluacion { get; set; }
    public string? ComentarioEvaluacion { get; set; }
}

public class DocumentoObservacionDto
{
    public int Id { get; set; }
    public DocumentoPostulacionDto? Documento { get; set; }
    public EvaluadorDto? Evaluador { get; set; }
    public string TipoObservacion { get; set; } = "ILEGIBLE";
    public string Descripcion { get; set; } = "";
    public DateOnly? FechaObservacion { get; set; }
    public string EstadoObservacion { get; set; } = "PENDIENTE";
    public DateOnly? FechaSubsanacion { get; set; }
    public string? ComentarioSubsanacion { get; set; }
}

public class CarnePostulanteDto
{
    public int Id { get; set; }
    public PostulacionDto? Postulacion { get; set; }
    public SedeDto? Sede { get; set; }
    public string CodigoCarne { get; set; } = "";
    public DateOnly? FechaGeneracion { get; set; }
    public DateOnly? FechaInicioVigencia { get; set; }
    public DateOnly? FechaFinVigencia { get; set; }
    public string? AulaExamen { get; set; }
}

public class PagoDto
{
    public int Id { get; set; }
    public PostulacionDto? Postulacion { get; set; }
    public string MedioPago { get; set; } = "TRANSFERENCIA";
    public double Monto { get; set; }
    public DateOnly? FechaGeneracion { get; set; }
    public DateOnly? FechaPago { get; set; }
    public string CodigoPago { get; set; } = "";
    public string? ReferenciaPasarela { get; set; }
    public string EstadoPago { get; set; } = "PENDIENTE";
    public string? RutaVoucher { get; set; }
}

public partial class AdmitURestClient
{
    private readonly HttpClient _httpClient;
    private readonly JsonSerializerOptions _jsonOptions;
    private readonly SemaphoreSlim _lecturas = new(6, 6);

    private static readonly Dictionary<string, string> RutasListar = new(StringComparer.OrdinalIgnoreCase)
    {
        ["AntecedenteAcademicoRS"] = "listarTodosAntecedentesAcademicos",
        ["CarnePostulanteRS"] = "listarTodosCarnesPostulante",
        ["CarreraRS"] = "listarTodasCarreras",
        ["ConvocatoriaModalidadRS"] = "listarTodasConvocatoriasModalidad",
        ["ConvocatoriaRS"] = "listarTodasConvocatorias",
        ["DocumentoObservacionRS"] = "listarTodasDocumentoObservaciones",
        ["DocumentoPostulacionRS"] = "listarTodosDocumentosPostulacion",
        ["EstadoPostulacionRS"] = "listarTodosEstadosPostulacion",
        ["EvaluadorRS"] = "listarTodosEvaluadores",
        ["FacultadRS"] = "listarTodasFacultades",
        ["InstitucionEducativaRS"] = "listarTodasInstitucionesEducativas",
        ["ModalidadRS"] = "listarTodasModalidades",
        ["OfertaCarreraRS"] = "listarTodasOfertasCarrera",
        ["PagoRS"] = "listarTodosPagos",
        ["PaisRS"] = "listarTodosPaises",
        ["PostulacionHistorialRS"] = "listarTodosPostulacionHistorial",
        ["PostulacionRS"] = "listarTodasPostulaciones",
        ["PostulanteRS"] = "listarTodosPostulantes",
        ["RequisitoConvocatoriaModalidadRS"] = "listarTodosRequisitosConvocatoriaModalidad",
        ["RequisitoRS"] = "listarTodosRequisitos",
        ["SedeRS"] = "listarTodasSedes"
    };

    private static string ObtenerNombreEntidad(string recurso) =>
        recurso.EndsWith("RS", StringComparison.OrdinalIgnoreCase) ? recurso[..^2] : recurso;

    private static string ObtenerRutaListar(string recurso) =>
        RutasListar.TryGetValue(recurso, out var ruta) ? ruta : $"listarTodos{ObtenerNombreEntidad(recurso)}s";

    public AdmitURestClient(HttpClient httpClient, IHttpContextAccessor contexto)
    {
        _httpClient = httpClient;
        ActualizarSesion(contexto.HttpContext?.User);
        _jsonOptions = new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true,
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull
        };
    }

    /// <summary>Lecturas no bloqueantes, acotadas por sesión para evitar ráfagas de conexiones JDBC.</summary>
    public async Task<List<T>> ListarTodosAsync<T>(string recurso, CancellationToken cancelacion = default)
    {
        if (!Autenticado && !RecursosPublicos.Contains(recurso)) return [];
        if (Rol == "postulante" && recurso == "EvaluadorRS") return [];
        await _lecturas.WaitAsync(cancelacion).ConfigureAwait(false);
        try
        {
            using var response = await _httpClient.GetAsync($"{recurso}/{ObtenerRutaListar(recurso)}", cancelacion).ConfigureAwait(false);
            response.EnsureSuccessStatusCode();
            return await response.Content.ReadFromJsonAsync<List<T>>(_jsonOptions, cancelacion).ConfigureAwait(false) ?? [];
        }
        finally { _lecturas.Release(); }
    }

    public List<T> ListarTodos<T>(string recurso)
    {
        if (!Autenticado && !RecursosPublicos.Contains(recurso)) return [];
        if (Rol == "postulante" && recurso == "EvaluadorRS") return [];
        var url = $"{recurso}/{ObtenerRutaListar(recurso)}";
        var response = _httpClient.GetAsync(url).GetAwaiter().GetResult();
        response.EnsureSuccessStatusCode();
        var json = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
        return JsonSerializer.Deserialize<List<T>>(json, _jsonOptions) ?? [];
    }

    public T? ObtenerPorId<T>(string recurso, int id)
    {
        var entidad = ObtenerNombreEntidad(recurso);
        var url = $"{recurso}/buscar{entidad}PorId/{id}";
        var response = _httpClient.GetAsync(url).GetAwaiter().GetResult();
        if (!response.IsSuccessStatusCode) return default;
        var json = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
        if (string.IsNullOrWhiteSpace(json)) return default;
        return JsonSerializer.Deserialize<T>(json, _jsonOptions);
    }

    public T? Insertar<T>(string recurso, T entidad)
    {
        var nombreEntidad = ObtenerNombreEntidad(recurso);
        var url = $"{recurso}/insertar{nombreEntidad}";
        var payload = JsonSerializer.Serialize(entidad, _jsonOptions);
        using var content = new StringContent(payload, Encoding.UTF8, "application/json");
        var response = _httpClient.PostAsync(url, content).GetAwaiter().GetResult();
        var body = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
        if (!response.IsSuccessStatusCode)
            throw new InvalidOperationException($"Error POST {url} ({(int)response.StatusCode}): {body}");

        if (int.TryParse(body.Trim(), out var idGenerado))
        {
            if (idGenerado <= 0)
                throw new InvalidOperationException($"No se pudo insertar en {url}: resultado {idGenerado}");
            if (entidad is not null)
            {
                var propId = typeof(T).GetProperty("Id");
                if (propId is not null && propId.CanWrite)
                {
                    propId.SetValue(entidad, idGenerado);
                }
            }
            return entidad;
        }
        return JsonSerializer.Deserialize<T>(body, _jsonOptions);
    }

    public T? Modificar<T>(string recurso, T entidad)
    {
        var nombreEntidad = ObtenerNombreEntidad(recurso);
        var url = $"{recurso}/modificar{nombreEntidad}";
        var payload = JsonSerializer.Serialize(entidad, _jsonOptions);
        using var content = new StringContent(payload, Encoding.UTF8, "application/json");
        var response = _httpClient.PutAsync(url, content).GetAwaiter().GetResult();
        var body = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
        if (!response.IsSuccessStatusCode)
            throw new InvalidOperationException($"Error PUT {url} ({(int)response.StatusCode}): {body}");

        if (int.TryParse(body.Trim(), out var filasAfectadas))
        {
            if (filasAfectadas <= 0)
                throw new InvalidOperationException($"No se pudo modificar en {url}: resultado {filasAfectadas}");
            return entidad;
        }
        return JsonSerializer.Deserialize<T>(body, _jsonOptions);
    }

    public bool Eliminar(string recurso, int id)
    {
        var nombreEntidad = ObtenerNombreEntidad(recurso);
        var url = $"{recurso}/eliminar{nombreEntidad}/{id}";
        var response = _httpClient.DeleteAsync(url).GetAwaiter().GetResult();
        if (!response.IsSuccessStatusCode) return false;
        var body = response.Content.ReadAsStringAsync().GetAwaiter().GetResult();
        if (int.TryParse(body.Trim(), out var filasAfectadas))
        {
            return filasAfectadas > 0;
        }
        return true;
    }
}
