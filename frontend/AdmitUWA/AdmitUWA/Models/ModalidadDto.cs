using System.ComponentModel.DataAnnotations;

namespace AdmitUWA.Models;

public sealed record class ModalidadDto
{
    public int Id { get; set; }

    [Required(ErrorMessage = "Ingrese el código de modalidad.")]
    [StringLength(20)]
    [RegularExpression(@"^[A-Za-z0-9_-]+$", ErrorMessage = "El código contiene caracteres inválidos.")]
    public string CodigoModalidad { get; set; } = "";

    [Required(ErrorMessage = "Ingrese el nombre de modalidad.")]
    [StringLength(100)]
    public string Nombre { get; set; } = "";

    [StringLength(255)]
    public string? Descripcion { get; set; }
    public bool RequiereColegio { get; set; }
    public bool RequiereUniversidad { get; set; }
    public bool Activo { get; set; } = true;
}
