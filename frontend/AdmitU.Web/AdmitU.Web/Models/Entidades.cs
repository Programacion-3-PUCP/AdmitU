using System.ComponentModel.DataAnnotations;
using AdmitU.Web.Data;

namespace AdmitU.Web.Models;

public enum EstadoPostulacion { Borrador, Enviada, EnRevision, Observada, Admitida, NoAdmitida }
public enum EstadoPago { Generado, Pendiente, Aprobado, Rechazado, Vencido }
public enum TipoObservacion { Faltante, Ilegible, Vencido, Otro }
public enum EstadoObservacion { Pendiente, Subsanada }
public enum EstadoDocumento { Pendiente, Aprobado, Observado, Rechazado }
public enum EstadoConvocatoria { Borrador, Publicada, Cerrada }

public class Facultad
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")] [StringLength(10)]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la facultad.")] [StringLength(100)]
    public string Nombre { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Carrera
{
    public int Id { get; set; }
    public int FacultadId { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")] [StringLength(10)]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la carrera.")] [StringLength(100)]
    public string Nombre { get; set; } = "";
    [Range(1, 1000, ErrorMessage = "Las vacantes deben estar entre 1 y 1000.")]
    public int Vacantes { get; set; } = 40;
    public bool Activo { get; set; } = true;
}

public class Sede
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el nombre de la sede.")] [StringLength(100)]
    public string Nombre { get; set; } = "";
    [Required(ErrorMessage = "Ingrese la ciudad.")] [StringLength(60)]
    public string Ciudad { get; set; } = "";
    [Required(ErrorMessage = "Ingrese la dirección.")] [StringLength(150)]
    public string Direccion { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Modalidad
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")] [StringLength(10)]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la modalidad.")] [StringLength(100)]
    public string Nombre { get; set; } = "";
    [StringLength(250)]
    public string Descripcion { get; set; } = "";
    public bool Activo { get; set; } = true;
}

public class Convocatoria
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")] [StringLength(20)]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre de la convocatoria.")] [StringLength(100)]
    public string Nombre { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el periodo.")] [StringLength(10)]
    public string Periodo { get; set; } = "";
    public DateOnly Inicio { get; set; } = DatosDemo.Hoy;
    public DateOnly Fin { get; set; } = DatosDemo.Hoy.AddMonths(2);
    public EstadoConvocatoria Estado { get; set; } = EstadoConvocatoria.Borrador;
    [StringLength(300)]
    public string Descripcion { get; set; } = "";
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
    public string Medio { get; set; } = "";
    public decimal Monto { get; set; }
    public DateOnly FechaGeneracion { get; set; }
    public DateOnly? FechaPago { get; set; }
    public EstadoPago Estado { get; set; }
    public string? Voucher { get; set; }
    public string? ComentarioVoucher { get; set; }
}

public class Documento
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public string Requisito { get; set; } = "";
    public string Archivo { get; set; } = "";
    public string Tipo { get; set; } = "PDF";
    public int TamanioKb { get; set; }
    public DateOnly FechaCarga { get; set; }
    public EstadoDocumento Estado { get; set; }
    public string? Comentario { get; set; }
}

public class Evaluador
{
    public int Id { get; set; }
    public string Nombre { get; set; } = "";
    public string Correo { get; set; } = "";
    public string Especialidad { get; set; } = "";
    public int Asignadas { get; set; }
    public int Evaluadas { get; set; }
    public bool Activo { get; set; } = true;
}

public class Notificacion
{
    public int Id { get; set; }
    public string Titulo { get; set; } = "";
    public string Detalle { get; set; } = "";
    public string Rol { get; set; } = "Administrador";
    public string Tipo { get; set; } = "Sistema";
    public string Medio { get; set; } = "Correo";
    public DateTime Fecha { get; set; }
    public bool Leida { get; set; }
}

public record Hito(string Nombre, DateOnly Fecha, string Detalle);

public class Requisito
{
    public int Id { get; set; }
    [Required(ErrorMessage = "Ingrese el código.")] [StringLength(20)]
    public string Codigo { get; set; } = "";
    [Required(ErrorMessage = "Ingrese el nombre del requisito.")] [StringLength(150)]
    public string Nombre { get; set; } = "";
    [StringLength(255)]
    public string Descripcion { get; set; } = "";
    public string TipoArchivo { get; set; } = "PDF";
    [Range(1, 50, ErrorMessage = "El tamaño máximo debe estar entre 1 y 50 MB.")]
    public int TamanioMaximoMb { get; set; } = 5;
    public bool Activo { get; set; } = true;
}

public record Etapa(int Id, string Codigo, string Nombre, string Descripcion);
public record EtapaConvocatoria(int ConvocatoriaId, int EtapaId, DateOnly Inicio, DateOnly Fin);
public record ModalidadConvocatoria(int ConvocatoriaId, int ModalidadId, decimal Costo, string? Observacion);
public record OfertaCarrera(int ConvocatoriaId, int CarreraId, int Vacantes);
public record RequisitoAsignado(int ConvocatoriaId, int ModalidadId, int RequisitoId, bool Obligatorio, int Orden);

public class Institucion
{
    public int Id { get; set; }
    public string Pais { get; set; } = "Perú";
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string Tipo { get; set; } = "COLEGIO";
}

public class Antecedente
{
    public int Id { get; set; }
    public int PostulanteId { get; set; }
    public int InstitucionId { get; set; }
    public int? AnioInicio { get; set; }
    public int? AnioFin { get; set; }
    public string Descripcion { get; set; } = "";
}

public class Apoderado
{
    public int PostulanteId { get; set; }
    public string Nombres { get; set; } = "";
    public string Apellidos { get; set; } = "";
    public string Parentesco { get; set; } = "PADRE";
    public string Documento { get; set; } = "";
    public string Correo { get; set; } = "";
    public string? Telefono { get; set; }
    public string NombreCompleto => $"{Nombres} {Apellidos}".Trim();
}

public record CambioEstado(int PostulacionId, string? Anterior, string Actual, DateOnly Fecha, string Responsable, string Motivo);

public class Carne
{
    public int Id { get; set; }
    public int PostulacionId { get; set; }
    public int SedeId { get; set; }
    public string Codigo { get; set; } = "";
    public DateOnly FechaGeneracion { get; set; }
    public DateOnly VigenciaInicio { get; set; }
    public DateOnly VigenciaFin { get; set; }
    public string Aula { get; set; } = "";
    public bool Anulado { get; set; }
    public string? MotivoAnulacion { get; set; }
}

public class ObservacionDocumento
{
    public int Id { get; set; }
    public int DocumentoId { get; set; }
    public int EvaluadorId { get; set; }
    public TipoObservacion Tipo { get; set; }
    public string Descripcion { get; set; } = "";
    public DateOnly Fecha { get; set; }
    public EstadoObservacion Estado { get; set; }
    public DateOnly? FechaSubsanacion { get; set; }
    public string? ComentarioSubsanacion { get; set; }
}
