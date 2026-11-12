namespace AdmitUWA.Models;

// Texto, color y clasificación de cada estado: las pantallas no repiten esta lógica.
public static class Presentacion
{
    public static string Etiqueta(this EstadoPostulacion e) => e switch
    {
        EstadoPostulacion.Borrador => "Borrador",
        EstadoPostulacion.Enviada => "Enviada",
        EstadoPostulacion.EnRevision => "En revisión",
        EstadoPostulacion.Observada => "Observada",
        EstadoPostulacion.Apta => "Apta",
        EstadoPostulacion.NoApta => "No apta",
        EstadoPostulacion.Admitida => "Admitida",
        _ => "No admitida"
    };

    public static string Tono(this EstadoPostulacion e) => e switch
    {
        EstadoPostulacion.Borrador => "neutral",
        EstadoPostulacion.Enviada => "info",
        EstadoPostulacion.EnRevision => "violet",
        EstadoPostulacion.Observada => "warning",
        EstadoPostulacion.Apta => "success",
        EstadoPostulacion.NoApta => "danger",
        EstadoPostulacion.Admitida => "success",
        _ => "danger"
    };

    /// <summary>Pendiente de la decisión del evaluador.</summary>
    public static bool EsPendiente(this EstadoPostulacion e) => e is EstadoPostulacion.Enviada or EstadoPostulacion.EnRevision;

    /// <summary>El evaluador ya emitió su decisión final sobre el expediente (Observada no lo es: espera subsanación).</summary>
    public static bool EsResuelta(this EstadoPostulacion e) =>
        e is EstadoPostulacion.Apta or EstadoPostulacion.NoApta or EstadoPostulacion.Admitida or EstadoPostulacion.NoAdmitida;

    public static string Etiqueta(this EstadoPago e) => e switch
    {
        EstadoPago.Generado => "Generado",
        EstadoPago.Pendiente => "Por validar",
        EstadoPago.Aprobado => "Aprobado",
        EstadoPago.Rechazado => "Rechazado",
        _ => "Vencido"
    };

    public static string Tono(this EstadoPago e) => e switch
    {
        EstadoPago.Aprobado => "success",
        EstadoPago.Pendiente => "warning",
        EstadoPago.Rechazado => "danger",
        _ => "neutral"
    };

    public static string Etiqueta(this EstadoDocumento e) => e switch
    {
        EstadoDocumento.Pendiente => "Por revisar",
        EstadoDocumento.Aprobado => "Aprobado",
        EstadoDocumento.Observado => "Observado",
        _ => "Rechazado"
    };

    public static string Tono(this EstadoDocumento e) => e switch
    {
        EstadoDocumento.Pendiente => "info",
        EstadoDocumento.Aprobado => "success",
        EstadoDocumento.Observado => "warning",
        _ => "danger"
    };

    public static string Etiqueta(this EstadoConvocatoria e) => e switch
    {
        EstadoConvocatoria.Borrador => "Borrador",
        EstadoConvocatoria.Publicada => "En curso",
        _ => "Cerrada"
    };

    public static string Tono(this EstadoConvocatoria e) => e switch
    {
        EstadoConvocatoria.Borrador => "neutral",
        EstadoConvocatoria.Publicada => "success",
        _ => "info"
    };

    public static string Etiqueta(this EstadoVoucher e) => e switch
    {
        EstadoVoucher.Pendiente => "Por validar",
        EstadoVoucher.Aprobado => "Aprobado",
        _ => "Rechazado"
    };

    public static string Tono(this EstadoVoucher e) => e switch
    {
        EstadoVoucher.Pendiente => "warning",
        EstadoVoucher.Aprobado => "success",
        _ => "danger"
    };

    public static string Etiqueta(this EstadoObservacion e) => e == EstadoObservacion.Abierta ? "Por subsanar" : "Subsanada";
    public static string Tono(this EstadoObservacion e) => e == EstadoObservacion.Abierta ? "warning" : "success";

    public static string Etiqueta(this TipoObservacion e) => e switch
    {
        TipoObservacion.Faltante => "Información faltante",
        TipoObservacion.Ilegible => "Ilegible",
        TipoObservacion.Vencido => "Vencido",
        _ => "Otro"
    };

    public static string Etiqueta(this EstadoCarne e) => e == EstadoCarne.Vigente ? "Vigente" : "Anulado";
    public static string Tono(this EstadoCarne e) => e == EstadoCarne.Vigente ? "success" : "neutral";

    public static string Etiqueta(this FormatoArchivo e) => e switch
    {
        FormatoArchivo.Pdf => "PDF",
        FormatoArchivo.Imagen => "Imagen (JPG o PNG)",
        _ => "PDF o imagen"
    };

    public static string Extensiones(this FormatoArchivo e) => e switch
    {
        FormatoArchivo.Pdf => ".pdf",
        FormatoArchivo.Imagen => ".jpg,.jpeg,.png",
        _ => ".pdf,.jpg,.jpeg,.png"
    };

    public static string Iniciales(string nombre)
    {
        var partes = nombre.Split(' ', StringSplitOptions.RemoveEmptyEntries)
            .Where(p => !p.EndsWith('.'))
            .ToArray();
        return partes.Length switch
        {
            0 => "?",
            1 => partes[0][..1].ToUpperInvariant(),
            _ => (partes[0][..1] + partes[1][..1]).ToUpperInvariant()
        };
    }

    public static int Matiz(string texto)
    {
        int h = 0;
        foreach (var c in texto) h = (h * 31 + c) % 360;
        return h;
    }
}
