using System.Globalization;

namespace AdmitU.Web.Data;

public static class Formato
{
    private static readonly CultureInfo Es = Crear();

    private static CultureInfo Crear()
    {
        try { return CultureInfo.GetCultureInfo("es-PE"); }
        catch (CultureNotFoundException) { return CultureInfo.InvariantCulture; }
    }

    public static string Fecha(DateOnly fecha) => fecha.ToString("dd MMM yyyy", Es);
    public static string Fecha(DateOnly? fecha) => fecha is { } f ? Fecha(f) : "—";
    public static string Mes(DateOnly fecha) => fecha.ToString("MMM", Es);
    public static string FechaCorta(DateOnly fecha) => fecha.ToString("dd MMM", Es);
    public static string FechaHora(DateTime fecha) => fecha.ToString("dd MMM, HH:mm", Es);
    public static string Soles(decimal monto) => "S/ " + monto.ToString("N2", CultureInfo.InvariantCulture);
    public static string Numero(int n) => n.ToString("N0", CultureInfo.InvariantCulture);
    public static string Decimal(double n) => n.ToString("0.##", CultureInfo.InvariantCulture);

    public static string Tamanio(int kb) => kb >= 1024
        ? (kb / 1024.0).ToString("0.0", CultureInfo.InvariantCulture) + " MB"
        : kb + " KB";

    public static string Relativo(DateTime fecha)
    {
        var ahora = DatosDemo.Hoy.ToDateTime(new TimeOnly(18, 0));
        var delta = ahora - fecha;
        if (delta.TotalMinutes < 60) return $"hace {Math.Max(1, (int)delta.TotalMinutes)} min";
        if (delta.TotalHours < 24) return $"hace {(int)delta.TotalHours} h";
        return $"hace {(int)delta.TotalDays} d";
    }
}
