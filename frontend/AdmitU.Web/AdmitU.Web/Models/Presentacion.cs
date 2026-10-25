namespace AdmitU.Web.Models;

// Texto y color de cada estado: así las pantallas no repiten esta lógica.
public static class Presentacion
{
    public static string Etiqueta(this EstadoPostulacion e) => e switch
    {
        EstadoPostulacion.Borrador => "Borrador",
        EstadoPostulacion.Enviada => "Enviada",
        EstadoPostulacion.EnRevision => "En revisión",
        EstadoPostulacion.Observada => "Observada",
        EstadoPostulacion.Admitida => "Admitida",
        _ => "No admitida"
    };

    public static string Tono(this EstadoPostulacion e) => e switch
    {
        EstadoPostulacion.Borrador => "neutral",
        EstadoPostulacion.Enviada => "info",
        EstadoPostulacion.EnRevision => "violet",
        EstadoPostulacion.Observada => "warning",
        EstadoPostulacion.Admitida => "success",
        _ => "danger"
    };

    public static string Etiqueta(this EstadoPago e) => e switch
    {
        EstadoPago.Pendiente => "Pendiente",
        EstadoPago.Pagado => "Pagado",
        EstadoPago.Observado => "Observado",
        _ => "Anulado"
    };

    public static string Tono(this EstadoPago e) => e switch
    {
        EstadoPago.Pendiente => "warning",
        EstadoPago.Pagado => "success",
        EstadoPago.Observado => "violet",
        _ => "neutral"
    };

    public static string Etiqueta(this EstadoDocumento e) => e.ToString();

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

    public static string Iniciales(string nombre)
    {
        var partes = nombre.Split(' ', StringSplitOptions.RemoveEmptyEntries);
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
