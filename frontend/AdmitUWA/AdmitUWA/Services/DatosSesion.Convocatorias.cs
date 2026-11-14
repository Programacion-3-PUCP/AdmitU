using AdmitUWA.Models;

namespace AdmitUWA.Services;

public sealed partial class DatosSesion
{
    // MySQL INSERTAR_POSTULACION usa las mismas fechas inclusivas y día de Lima.
    public bool InscripcionesAbiertas(Convocatoria convocatoria) =>
        InscripcionesAbiertas(convocatoria, Hoy);

    public static bool InscripcionesAbiertas(Convocatoria convocatoria, DateOnly diaLima) =>
        convocatoria.Estado == EstadoConvocatoria.Publicada
        && (convocatoria.Inicio is null || diaLima >= convocatoria.Inicio)
        && (convocatoria.Fin is null || diaLima <= convocatoria.Fin);

    public decimal? TarifaInscripcion(int convocatoriaId, int modalidadId) =>
        _convModalidades.FirstOrDefault(cm => cm.Convocatoria?.Id == convocatoriaId
            && cm.Modalidad?.Id == modalidadId) is { } tarifa
            ? (decimal)tarifa.CostoInscripcion : null;

    public string TarifaInscripcionTexto(int convocatoriaId, int modalidadId) =>
        TarifaInscripcion(convocatoriaId, modalidadId) is { } monto
            ? Data.Formato.Soles(monto) : "Tarifa no configurada";
}
