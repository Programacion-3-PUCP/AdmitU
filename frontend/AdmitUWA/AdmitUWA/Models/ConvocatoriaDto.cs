using System.ComponentModel.DataAnnotations;

namespace AdmitUWA.Models;

public sealed record class ConvocatoriaDto : IValidatableObject
{
    public int Id { get; set; }

    [Required(ErrorMessage = "Ingrese el código de convocatoria.")]
    [StringLength(20)]
    [RegularExpression(@"^[A-Za-z0-9_-]+$", ErrorMessage = "El código contiene caracteres inválidos.")]
    public string CodigoConvocatoria { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el nombre de la convocatoria.")]
    [StringLength(150)]
    public string Nombre { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el período.")]
    [StringLength(20)]
    public string Periodo { get; set; } = "";

    public DateOnly? FechaInicio { get; set; }
    public DateOnly? FechaFin { get; set; }

    [Required]
    public string Estado { get; set; } = "BORRADOR";

    [StringLength(255)]
    public string? Descripcion { get; set; }
    public bool Activo { get; set; } = true;

    public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
    {
        if (FechaInicio is null)
            yield return new ValidationResult("Seleccione la fecha de inicio.", [nameof(FechaInicio)]);
        if (FechaFin is null)
            yield return new ValidationResult("Seleccione la fecha de cierre.", [nameof(FechaFin)]);
        if (FechaInicio is { } inicio && FechaFin is { } fin && fin < inicio)
            yield return new ValidationResult("La fecha de cierre debe ser igual o posterior al inicio.", [nameof(FechaFin)]);
    }
}
