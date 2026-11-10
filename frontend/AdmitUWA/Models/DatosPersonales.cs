using System.ComponentModel.DataAnnotations;
using System.Text.RegularExpressions;

namespace AdmitUWA.Models;

/// <summary>
/// Formulario de datos personales del postulante. Lo usan el registro y "Mi perfil",
/// así ambos aplican exactamente las mismas reglas.
/// </summary>
public partial class DatosPersonales : IValidatableObject
{
    [Required(ErrorMessage = "Ingresa tus nombres.")]
    [StringLength(100, ErrorMessage = "Los nombres admiten como máximo {1} caracteres.")]
    public string Nombres { get; set; } = "";

    [Required(ErrorMessage = "Ingresa tu apellido paterno.")]
    [StringLength(100, ErrorMessage = "El apellido admite como máximo {1} caracteres.")]
    public string ApellidoPaterno { get; set; } = "";

    [Required(ErrorMessage = "Ingresa tu apellido materno.")]
    [StringLength(100, ErrorMessage = "El apellido admite como máximo {1} caracteres.")]
    public string ApellidoMaterno { get; set; } = "";

    public string TipoDocumento { get; set; } = "DNI";

    [Required(ErrorMessage = "Ingresa tu número de documento.")]
    public string NumeroDocumento { get; set; } = "";

    [Required(ErrorMessage = "Ingresa tu correo electrónico.")]
    [EmailAddress(ErrorMessage = "Ingresa un correo válido.")]
    public string Correo { get; set; } = "";

    public string? Telefono { get; set; }

    public DateOnly? FechaNacimiento { get; set; }

    public bool TieneDiscapacidad { get; set; }

    public string? CarnetConadis { get; set; }

    public static readonly (string Valor, string Texto)[] TiposDocumento =
        [("DNI", "DNI"), ("CE", "Carné de extranjería"), ("PAS", "Pasaporte")];

    public IEnumerable<ValidationResult> Validate(ValidationContext contexto)
    {
        var (patron, ayuda) = TipoDocumento switch
        {
            "DNI" => (DniRegex(), "El DNI tiene 8 dígitos."),
            "CE" => (CeRegex(), "El carné de extranjería tiene de 9 a 12 caracteres alfanuméricos."),
            _ => (PasaporteRegex(), "El pasaporte tiene de 6 a 12 caracteres alfanuméricos.")
        };
        if (!string.IsNullOrWhiteSpace(NumeroDocumento) && !patron.IsMatch(NumeroDocumento.Trim()))
            yield return new ValidationResult(ayuda, [nameof(NumeroDocumento)]);

        var hoy = DateOnly.FromDateTime(DateTime.Today);
        if (FechaNacimiento is null)
            yield return new ValidationResult("Ingresa tu fecha de nacimiento.", [nameof(FechaNacimiento)]);
        else if (FechaNacimiento > hoy)
            yield return new ValidationResult("La fecha de nacimiento no puede ser futura.", [nameof(FechaNacimiento)]);
        else if (FechaNacimiento > hoy.AddYears(-14))
            yield return new ValidationResult("Debes tener al menos 14 años para postular.", [nameof(FechaNacimiento)]);

        if (!string.IsNullOrWhiteSpace(Telefono) && !TelefonoRegex().IsMatch(Telefono.Trim()))
            yield return new ValidationResult("Ingresa un teléfono válido (solo números, de 6 a 15 dígitos).", [nameof(Telefono)]);

        if (TieneDiscapacidad && string.IsNullOrWhiteSpace(CarnetConadis))
            yield return new ValidationResult("Ingresa el número de tu carné CONADIS.", [nameof(CarnetConadis)]);
    }

    public static DatosPersonales Desde(Postulante p) => new()
    {
        Nombres = p.Nombres, ApellidoPaterno = p.ApellidoPaterno, ApellidoMaterno = p.ApellidoMaterno,
        TipoDocumento = p.TipoDocumento, NumeroDocumento = p.NumeroDocumento, Correo = p.Correo,
        Telefono = p.Telefono, FechaNacimiento = p.FechaNacimiento,
        TieneDiscapacidad = p.TieneDiscapacidad, CarnetConadis = p.CarnetConadis
    };

    [GeneratedRegex("^[0-9]{8}$")] private static partial Regex DniRegex();
    [GeneratedRegex("^[A-Za-z0-9]{9,12}$")] private static partial Regex CeRegex();
    [GeneratedRegex("^[A-Za-z0-9]{6,12}$")] private static partial Regex PasaporteRegex();
    [GeneratedRegex(@"^\+?[0-9 ]{6,18}$")] private static partial Regex TelefonoRegex();
}
