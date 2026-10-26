using System.ComponentModel.DataAnnotations;

namespace AdmitUWA.Models;

public sealed record class PostulanteDto : IValidatableObject
{
    public int Id { get; set; }

    [Required(ErrorMessage = "Ingrese los nombres.")]
    [StringLength(100)]
    public string Nombres { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el apellido paterno.")]
    [StringLength(100)]
    public string ApellidoPaterno { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el apellido materno.")]
    [StringLength(100)]
    public string ApellidoMaterno { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el correo electrónico.")]
    [EmailAddress(ErrorMessage = "Ingrese un correo electrónico válido.")]
    [StringLength(150)]
    public string Correo { get; set; } = "";

    [Required]
    public string TipoDocumento { get; set; } = "DNI";

    [Required(ErrorMessage = "Ingrese el número de documento.")]
    public string NumeroDocumento { get; set; } = "";

    public string? Telefono { get; set; }
    public int? ApoderadoId { get; set; }
    public DateOnly? FechaNacimiento { get; set; }
    public bool TieneDiscapacidad { get; set; }
    [StringLength(30)]
    public string? NumeroCarnetConadis { get; set; }
    public bool CorreoValidado { get; set; }
    public DateOnly? FechaValidacionCorreo { get; set; }

    public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
    {
        string patronDocumento = TipoDocumento switch
        {
            "DNI" => @"^[0-9]{8}$",
            "CE" => @"^[A-Za-z0-9]{9,12}$",
            "PAS" => @"^[A-Za-z0-9]{6,12}$",
            "CPP" => @"^[A-Za-z0-9]{8,15}$",
            _ => @"^$"
        };
        if (!System.Text.RegularExpressions.Regex.IsMatch(NumeroDocumento ?? "", patronDocumento))
            yield return new ValidationResult("Formato de documento no válido para el tipo seleccionado.", [nameof(NumeroDocumento)]);

        if (!string.IsNullOrWhiteSpace(Telefono) &&
            !System.Text.RegularExpressions.Regex.IsMatch(Telefono, @"^\+?[0-9][0-9 ]{5,18}[0-9]$"))
            yield return new ValidationResult("Ingrese un teléfono válido.", [nameof(Telefono)]);

        if (FechaNacimiento is not { } fecha)
            yield return new ValidationResult("La fecha de nacimiento es obligatoria.", [nameof(FechaNacimiento)]);
        else
        {
            if (fecha > DateOnly.FromDateTime(DateTime.Today))
                yield return new ValidationResult("La fecha de nacimiento no puede ser futura.", [nameof(FechaNacimiento)]);
            if (fecha < DateOnly.FromDateTime(DateTime.Today.AddYears(-110)))
                yield return new ValidationResult("Fecha de nacimiento fuera de rango.", [nameof(FechaNacimiento)]);
        }

        if (TieneDiscapacidad && string.IsNullOrWhiteSpace(NumeroCarnetConadis))
            yield return new ValidationResult("Ingrese el número de carné CONADIS cuando corresponda.", [nameof(NumeroCarnetConadis)]);
        if (!TieneDiscapacidad && !string.IsNullOrWhiteSpace(NumeroCarnetConadis))
            yield return new ValidationResult("No corresponde un carné CONADIS.", [nameof(NumeroCarnetConadis)]);
        if (CorreoValidado && FechaValidacionCorreo is null)
            yield return new ValidationResult("Falta la fecha de validación del correo.", [nameof(FechaValidacionCorreo)]);
        if (FechaValidacionCorreo is { } validacion && validacion > DateOnly.FromDateTime(DateTime.Today))
            yield return new ValidationResult("La validación de correo no puede tener una fecha futura.", [nameof(FechaValidacionCorreo)]);
    }
}
