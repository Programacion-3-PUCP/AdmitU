using System.ComponentModel.DataAnnotations;

namespace AdmitUWA.Models;

public sealed record class FacultadDto
{
    public int Id { get; set; }

    [Required(ErrorMessage = "Ingrese el código de la facultad.")]
    [StringLength(20, ErrorMessage = "El código no puede superar 20 caracteres.")]
    [RegularExpression(@"^[A-Za-z0-9_-]+$", ErrorMessage = "El código solo acepta letras, números, guion y guion bajo.")]
    public string Codigo { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el nombre de la facultad.")]
    [StringLength(150, ErrorMessage = "El nombre no puede superar 150 caracteres.")]
    public string Nombre { get; set; } = "";

    public bool Activo { get; set; } = true;
}
