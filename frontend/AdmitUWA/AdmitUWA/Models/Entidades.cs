using System.ComponentModel.DataAnnotations;

namespace AdmitUWA.Models;

// Estados del expediente: el evaluador decide Apta / Observada / NoApta;
// Admitida y NoAdmitida son el resultado de admisión que se publica al cierre.
public enum EstadoPostulacion { Borrador, Enviada, EnRevision, Observada, Apta, NoApta, Admitida, NoAdmitida }
public enum EstadoPago { Generado, Pendiente, Aprobado, Rechazado, Vencido }
public enum EstadoDocumento { Pendiente, Aprobado, Observado, Rechazado }
public enum EstadoConvocatoria { Borrador, Publicada, Cerrada }
public enum EstadoVoucher { Pendiente, Aprobado, Rechazado }
public enum EstadoObservacion { Abierta, Subsanada }
public enum TipoObservacion { Faltante, Ilegible, Vencido, Otro }
public enum EstadoCarne { Vigente, Anulado }
public enum FormatoArchivo { Pdf, Imagen, Cualquiera }
public enum RolUsuario { Administrador, Evaluador, Postulante }

public class Facultad
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")]
    [StringLength(10, ErrorMessage = "El código admite como máximo {1} caracteres.")]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la facultad.")]
    [StringLength(100, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Carrera
{
    public int Id { get; set; }
    [Range(1, int.MaxValue, ErrorMessage = "Seleccione la facultad.")]
    public int FacultadId { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")]
    [StringLength(10, ErrorMessage = "El código admite como máximo {1} caracteres.")]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la carrera.")]
    [StringLength(100, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Sede
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código de la sede.")]
    [StringLength(20, ErrorMessage = "El código admite como máximo {1} caracteres.")]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la sede.")]
    [StringLength(100, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    [StringLength(60, ErrorMessage = "La ciudad admite como máximo {1} caracteres.")]
    public string Ciudad { get; set; } = "Lima";
    [Required(ErrorMessage = "Ingrese la dirección.")]
    [StringLength(150, ErrorMessage = "La dirección admite como máximo {1} caracteres.")]
    public string Direccion { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Modalidad
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")]
    [StringLength(10, ErrorMessage = "El código admite como máximo {1} caracteres.")]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la modalidad.")]
    [StringLength(100, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    [StringLength(250, ErrorMessage = "La descripción admite como máximo {1} caracteres.")]
    public string Descripcion { get; set; } = "";
    [Range(typeof(decimal), "0", "5000", ErrorMessage = "Ingrese un derecho de inscripción entre S/ 0 y S/ 5 000.")]
    public decimal DerechoInscripcion { get; set; } = 250m;
    public bool Activo { get; set; } = true;
}

public class Convocatoria : IValidatableObject
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")]
    [StringLength(20, ErrorMessage = "El código admite como máximo {1} caracteres.")]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la convocatoria.")]
    [StringLength(100, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el periodo.")]
    [StringLength(10, ErrorMessage = "El periodo admite como máximo {1} caracteres.")]
    public string Periodo { get; set; } = "";
    public DateOnly Inicio { get; set; } = DateOnly.FromDateTime(DateTime.Today);
    public DateOnly Fin { get; set; } = DateOnly.FromDateTime(DateTime.Today).AddMonths(2);
    public DateOnly? CierreInscripciones { get; set; }
    public DateOnly? LimiteEvaluacion { get; set; }
    public EstadoConvocatoria Estado { get; set; } = EstadoConvocatoria.Borrador;
    [StringLength(300, ErrorMessage = "La descripción admite como máximo {1} caracteres.")]
    public string Descripcion { get; set; } = "";

    public IEnumerable<ValidationResult> Validate(ValidationContext contexto)
    {
        if (Fin <= Inicio)
            yield return new ValidationResult("La fecha de resultados debe ser posterior a la de apertura.", [nameof(Fin)]);
        if (CierreInscripciones is { } cierre && (cierre < Inicio || cierre > Fin))
            yield return new ValidationResult("El cierre de inscripciones debe estar entre la apertura y los resultados.", [nameof(CierreInscripciones)]);
        if (LimiteEvaluacion is { } limite && (limite < (CierreInscripciones ?? Inicio) || limite > Fin))
            yield return new ValidationResult("El fin de la evaluación debe estar entre el cierre de inscripciones y los resultados.", [nameof(LimiteEvaluacion)]);
    }
}

// Vacantes que una convocatoria ofrece para cada carrera.
public class OfertaCarrera
{
    public int Id { get; set; }
    public int ConvocatoriaId { get; set; }
    public int CarreraId { get; set; }
    public int Vacantes { get; set; }
}

public class RequisitoAplicable
{
    public int Id { get; set; }
    public int RequisitoId { get; set; }
    public int ConvocatoriaModalidadId { get; set; }
    public int? OrdenPresentacion { get; set; } = 1;
    [Range(1, int.MaxValue, ErrorMessage = "Seleccione una convocatoria.")]
    public int ConvocatoriaId { get; set; }
    [Range(1, int.MaxValue, ErrorMessage = "Seleccione una modalidad.")]
    public int ModalidadId { get; set; }
    [Required(ErrorMessage = "Ingrese el nombre del requisito.")]
    [StringLength(120, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    public FormatoArchivo Formato { get; set; } = FormatoArchivo.Pdf;
    public bool Obligatorio { get; set; } = true;
    public bool Activo { get; set; } = true;
}

public class Postulante
{
    public int Id { get; set; }
    public string Nombres { get; set; } = "";
    public string ApellidoPaterno { get; set; } = "";
    public string ApellidoMaterno { get; set; } = "";
    public string TipoDocumento { get; set; } = "DNI";
    public string NumeroDocumento { get; set; } = "";
    public string Correo { get; set; } = "";
    public string? Telefono { get; set; }
    public DateOnly? FechaNacimiento { get; set; }
    public bool TieneDiscapacidad { get; set; }
    public string? CarnetConadis { get; set; }
    public bool CorreoValidado { get; set; }
    public string NombreCompleto => $"{Nombres} {ApellidoPaterno} {ApellidoMaterno}".Trim();
}

public class Postulacion
{
    public int Id { get; set; }
    public string Codigo { get; set; } = "";
    public int PostulanteId { get; set; }
    public int ConvocatoriaId { get; set; }
    public int CarreraId { get; set; }
    public int ModalidadId { get; set; }
    public int SedeId { get; set; }
    public int? EvaluadorId { get; set; }
    public EstadoPostulacion Estado { get; set; }
    public DateOnly FechaRegistro { get; set; }
    public DateOnly? FechaEnvio { get; set; }
    public string? Observacion { get; set; }
}

public class Pago
{
    public int Id { get; set; }
    public string Codigo { get; set; } = "";
    public int PostulacionId { get; set; }
    public string Medio { get; set; } = "Transferencia";
    public decimal Monto { get; set; }
    public DateOnly FechaGeneracion { get; set; }
    public DateOnly? FechaAprobacion { get; set; }
    public EstadoPago Estado { get; set; }
}

public class Voucher
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public string CodigoOperacion { get; set; } = "";
    public string Archivo { get; set; } = "";
    public decimal Monto { get; set; }
    public DateOnly FechaRegistro { get; set; }
    public EstadoVoucher Estado { get; set; } = EstadoVoucher.Pendiente;
    public DateOnly? FechaRevision { get; set; }
    public string? MotivoRechazo { get; set; }
}

public class Documento
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public int RequisitoAplicableId { get; set; }
    public string Requisito { get; set; } = "";
    public string Archivo { get; set; } = "";
    public string RutaArchivo { get; set; } = "";
    public string Tipo { get; set; } = "PDF";
    public int TamanioKb { get; set; }
    public int Version { get; set; } = 1;
    public DateOnly FechaCarga { get; set; }
    public EstadoDocumento Estado { get; set; }
    public string? Comentario { get; set; }
}

public class Observacion
{
    public int Id { get; set; }
    public int DocumentoId { get; set; }
    public int? EvaluadorId { get; set; }
    public TipoObservacion Tipo { get; set; } = TipoObservacion.Ilegible;
    public string Descripcion { get; set; } = "";
    public EstadoObservacion Estado { get; set; } = EstadoObservacion.Abierta;
    public DateOnly Fecha { get; set; }
    public DateOnly? FechaSubsanacion { get; set; }
    public string? ComentarioSubsanacion { get; set; }
}

public class MovimientoEstado
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public EstadoPostulacion? Anterior { get; set; }
    public EstadoPostulacion Nuevo { get; set; }
    public string Motivo { get; set; } = "";
    public string Responsable { get; set; } = "";
    public DateTime Fecha { get; set; }
}

public class Carne
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public string Codigo { get; set; } = "";
    public int SedeId { get; set; }
    public string Aula { get; set; } = "";
    public DateOnly FechaEmision { get; set; }
    public EstadoCarne Estado { get; set; } = EstadoCarne.Vigente;
    public bool Automatico { get; set; } = true;
    public string? Motivo { get; set; }
}

public class Antecedente
{
    public int Id { get; set; }
    public int PostulanteId { get; set; }
    public int InstitucionId { get; set; }
    public int AnioInicio { get; set; } = 2020;
    public int AnioFin { get; set; } = 2024;
    public string Tipo { get; set; } = "Certificado de notas";
    public string Institucion { get; set; } = "";
    public string Archivo { get; set; } = "";
    public DateOnly Fecha { get; set; }
}

public class DocumentoPersonal
{
    public int Id { get; set; }
    public int PostulanteId { get; set; }
    public string Tipo { get; set; } = "DNI";
    public string Archivo { get; set; } = "";
    public DateOnly Fecha { get; set; }
}

public class Evaluador
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el nombre.")]
    [StringLength(100, ErrorMessage = "El nombre admite como máximo {1} caracteres.")]
    public string Nombre { get; set; } = "";
    public string TipoDocumento { get; set; } = "DNI";
    [Required(ErrorMessage = "Ingrese el número de documento.")]
    [StringLength(20, ErrorMessage = "El documento admite como máximo {1} caracteres.")]
    public string NumeroDocumento { get; set; } = "40009988";
    [StringLength(20, ErrorMessage = "El teléfono admite como máximo {1} caracteres.")]
    public string Telefono { get; set; } = "987654321";
    [Required(ErrorMessage = "Ingrese el correo.")]
    [EmailAddress(ErrorMessage = "Ingrese un correo válido.")]
    public string Correo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese la especialidad.")]
    [StringLength(60, ErrorMessage = "La especialidad admite como máximo {1} caracteres.")]
    public string Especialidad { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Notificacion
{
    public int Id { get; set; }
    public string Titulo { get; set; } = "";
    public string Detalle { get; set; } = "";
    public RolUsuario Rol { get; set; } = RolUsuario.Administrador;
    public string Tipo { get; set; } = "Sistema";
    public string Medio { get; set; } = "Correo";
    public DateTime Fecha { get; set; }
    public bool Leida { get; set; }
}

public record Hito(string Nombre, DateOnly Fecha, string Detalle);
