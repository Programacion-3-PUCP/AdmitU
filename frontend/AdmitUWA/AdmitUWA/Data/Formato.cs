using System.Globalization;

namespace AdmitUWA.Data;

/// <summary>Formatos de presentación en español del Perú. Toda fecha o monto visible debe pasar por aquí.</summary>
public static class Formato
{
    public static readonly CultureInfo Peru = Crear();

    private static CultureInfo Crear()
    {
        try { return CultureInfo.GetCultureInfo("es-PE"); }
        catch (CultureNotFoundException) { return CultureInfo.InvariantCulture; }
    }

    public static string Fecha(DateOnly fecha) => fecha.ToString("dd MMM yyyy", Peru);
    public static string Fecha(DateOnly? fecha) => fecha is { } f ? Fecha(f) : "No registrada";
    public static string Mes(DateOnly fecha) => fecha.ToString("MMM", Peru);
    public static string FechaCorta(DateOnly fecha) => fecha.ToString("dd MMM", Peru);
    public static string Fecha(DateTime? fecha) => fecha is { } f ? f.ToString("dd MMM yyyy", Peru) : "No registrada";
    public static string FechaHora(DateTime fecha) => fecha.ToString("dd MMM yyyy, HH:mm", Peru);
    public static string Soles(decimal monto) => "S/ " + monto.ToString("N2", CultureInfo.InvariantCulture);
    public static string Numero(int n) => n.ToString("N0", CultureInfo.InvariantCulture);
    public static string Porcentaje(int parte, int total) => total == 0 ? "—" : $"{Math.Round(parte * 100.0 / total):0}%";

    public static string Tamanio(int kb) => kb >= 1024
        ? (kb / 1024.0).ToString("0.0", CultureInfo.InvariantCulture) + " MB"
        : kb + " KB";

    public static string Relativo(DateTime fecha)
    {
        var delta = DateTime.Now - fecha;
        if (delta.TotalMinutes < 1) return "ahora";
        if (delta.TotalMinutes < 60) return $"hace {(int)delta.TotalMinutes} min";
        if (delta.TotalHours < 24) return $"hace {(int)delta.TotalHours} h";
        return $"hace {(int)delta.TotalDays} d";
    }

    /// <summary>Antigüedad en días de algo pendiente, para resaltar lo que lleva más de 48 horas.</summary>
    public static string Antiguedad(DateOnly? desde, DateOnly hoy) => desde is { } fecha ? Antiguedad(fecha, hoy) : "fecha no registrada";

    public static string Antiguedad(DateOnly desde, DateOnly hoy)
    {
        var dias = hoy.DayNumber - desde.DayNumber;
        return dias switch { <= 0 => "hoy", 1 => "hace 1 día", _ => $"hace {dias} días" };
    }
}
