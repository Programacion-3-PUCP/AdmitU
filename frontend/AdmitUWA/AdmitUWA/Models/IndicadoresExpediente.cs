namespace AdmitUWA.Models;

/// <summary>Definiciones compartidas por el resumen y sus filtros; coinciden con la guarda SQL de APTA.</summary>
public static class IndicadoresExpediente
{
    public static bool EstadoApto(EstadoPostulacion estado) =>
        estado is EstadoPostulacion.Apta or EstadoPostulacion.Finalizada or EstadoPostulacion.Admitida;

    public static bool Enviada(Postulacion p) => p.Estado != EstadoPostulacion.Borrador && p.FechaEnvio.HasValue;

    public static bool DocumentosCompletos(Postulacion p, IEnumerable<RequisitoAplicable> requisitos,
        IEnumerable<Documento> documentos)
    {
        // La asociación obligatoria sigue vigente aunque se haya desactivado el catálogo del requisito,
        // como en MODIFICAR_POSTULACION. Una modalidad sin requisitos obligatorios satisface la regla.
        var obligatorios = requisitos.Where(r => r.ConvocatoriaId == p.ConvocatoriaId
            && r.ModalidadId == p.ModalidadId && r.Obligatorio);
        var aprobados = documentos.Where(d => d.PostulacionId == p.Id
            && d.Estado == EstadoDocumento.Aprobado && d.ArchivoActivo)
            .Select(d => d.RequisitoAplicableId).ToHashSet();
        return obligatorios.All(r => aprobados.Contains(r.Id));
    }
}
