using System.ComponentModel.DataAnnotations;

namespace AdmitUWA.Models;

public sealed record class CarreraDto : IValidatableObject
{
    public int Id { get; set; }
    public int FacultadId { get; set; }

    [Required(ErrorMessage = "Ingrese el código de la carrera.")]
    [StringLength(20)]
    [RegularExpression(@"^[A-Za-z0-9_-]+$", ErrorMessage = "El código contiene caracteres inválidos.")]
    public string CodigoCarrera { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el nombre de la carrera.")]
    [StringLength(150)]
    public string Nombre { get; set; } = "";

    public bool Activo { get; set; } = true;

    public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
    {
        if (FacultadId <= 0)
            yield return new ValidationResult("Seleccione una facultad.", [nameof(FacultadId)]);
    }
}
