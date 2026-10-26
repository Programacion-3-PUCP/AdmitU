namespace AdmitUWA.Models;

// Contratos de presentación propuestos para la futura API REST Java.
// Los identificadores sustituyen las referencias de objeto de las entidades Java.
public sealed record class PostulacionDto
{
    public int Id { get; set; }
    public int PostulanteId { get; set; }
    public int ConvocatoriaId { get; set; }
    public int ConvocatoriaModalidadId { get; set; }
    public int OfertaCarreraId { get; set; }
    public int EstadoActualId { get; set; }
    public string? EstadoActualNombre { get; set; }
    public DateOnly? FechaRegistro { get; set; }
    public DateOnly? FechaEnvio { get; set; }
    public DateOnly? FechaFinalizacion { get; set; }
    public string CodigoInscripcion { get; set; } = "";
    public string? ObservacionGeneral { get; set; }
}

public sealed record class PagoDto
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public string MedioPago { get; set; } = "";
    public decimal Monto { get; set; }
    public DateOnly? FechaGeneracion { get; set; }
    public DateOnly? FechaPago { get; set; }
    public string CodigoPago { get; set; } = "";
    public string? ReferenciaPasarela { get; set; }
    public string EstadoPago { get; set; } = "PENDIENTE";
    public string? RutaVoucher { get; set; }
}

public sealed record class DocumentoPostulacionDto
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public int RequisitoAplicableId { get; set; }
    public int NumeroVersion { get; set; }
    public string NombreArchivo { get; set; } = "";
    public string TipoArchivo { get; set; } = "";
    public long TamanioArchivo { get; set; }
    public string? RutaArchivo { get; set; }
    public DateOnly? FechaCarga { get; set; }
    public string EstadoDocumento { get; set; } = "";
    public DateOnly? FechaEvaluacion { get; set; }
    public string? ComentarioEvaluacion { get; set; }
}
