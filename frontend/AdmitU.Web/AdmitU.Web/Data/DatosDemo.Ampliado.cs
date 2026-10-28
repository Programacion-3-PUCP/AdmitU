using AdmitU.Web.Models;

namespace AdmitU.Web.Data;

// Datos de la configuración de la convocatoria y del seguimiento de cada expediente.
// Se crean bajo demanda porque dependen de las listas principales de DatosDemo.
public static partial class DatosDemo
{
    private static IReadOnlyList<Requisito>? requisitos;
    private static IReadOnlyList<Etapa>? etapas;
    private static IReadOnlyList<EtapaConvocatoria>? etapasConvocatoria;
    private static IReadOnlyList<ModalidadConvocatoria>? modalidadesConvocatoria;
    private static IReadOnlyList<OfertaCarrera>? ofertas;
    private static IReadOnlyList<RequisitoAsignado>? requisitosAsignados;
    private static IReadOnlyList<Institucion>? instituciones;
    private static IReadOnlyList<Antecedente>? antecedentes;
    private static IReadOnlyList<Apoderado>? apoderados;
    private static IReadOnlyList<CambioEstado>? historial;
    private static IReadOnlyList<Carne>? carnes;
    private static IReadOnlyList<ObservacionDocumento>? observaciones;

    public static IReadOnlyList<Requisito> Requisitos => requisitos ??=
    [
        new() { Id = 1, Codigo = "DNI", Nombre = "Documento de identidad", Descripcion = "Copia legible de ambas caras.", TipoArchivo = "IMAGEN", TamanioMaximoMb = 2 },
        new() { Id = 2, Codigo = "FOTO", Nombre = "Fotografía tamaño carné", Descripcion = "Fondo blanco, rostro descubierto, formato JPG o PNG.", TipoArchivo = "IMAGEN", TamanioMaximoMb = 1 },
        new() { Id = 3, Codigo = "CERT-EST", Nombre = "Certificado de estudios", Descripcion = "Certificado de los cinco años de secundaria.", TipoArchivo = "PDF", TamanioMaximoMb = 5 },
        new() { Id = 4, Codigo = "CONST-TERCIO", Nombre = "Constancia de tercio superior", Descripcion = "Emitida por la institución educativa.", TipoArchivo = "PDF", TamanioMaximoMb = 3 },
        new() { Id = 5, Codigo = "CONST-NOTAS", Nombre = "Constancia de notas universitarias", Descripcion = "Para postulantes por traslado externo.", TipoArchivo = "PDF", TamanioMaximoMb = 5 },
        new() { Id = 6, Codigo = "CONST-FED", Nombre = "Constancia de la federación deportiva", Descripcion = "Acredita la condición de deportista calificado.", TipoArchivo = "PDF", TamanioMaximoMb = 3 },
        new() { Id = 7, Codigo = "PART-NAC", Nombre = "Partida de nacimiento", Descripcion = "Documento antiguo, ya no se solicita.", TipoArchivo = "CUALQUIERA", TamanioMaximoMb = 3, Activo = false }
    ];

    public static IReadOnlyList<Etapa> Etapas => etapas ??=
    [
        new(1, "INSCRIP", "Inscripción", "El postulante registra y envía su postulación."),
        new(2, "REV-DOC", "Revisión documental", "El evaluador valida documentos y vouchers."),
        new(3, "EXAMEN", "Evaluación de admisión", "Examen presencial en la sede asignada."),
        new(4, "RESULT", "Publicación de resultados", "Lista de admitidos y observados."),
        new(5, "MATRIC", "Matrícula", "Matrícula de los postulantes admitidos.")
    ];

    public static IReadOnlyList<EtapaConvocatoria> EtapasConvocatoria => etapasConvocatoria ??=
    [
        new(1, 1, new(2026, 9, 1), new(2026, 11, 30)),
        new(1, 2, new(2026, 9, 15), new(2026, 12, 3)),
        new(1, 3, new(2026, 12, 5), new(2026, 12, 5)),
        new(1, 4, new(2026, 12, 15), new(2026, 12, 15)),
        new(1, 5, new(2027, 1, 10), new(2027, 2, 15)),
        new(2, 1, new(2027, 3, 1), new(2027, 4, 30))
    ];

    public static IReadOnlyList<ModalidadConvocatoria> ModalidadesConvocatoria => modalidadesConvocatoria ??=
    [
        new(1, 1, 250m, null), new(1, 2, 250m, "Requiere promedio dentro del tercio superior."),
        new(1, 3, 320m, "Incluye evaluación de convalidación de cursos."), new(1, 4, 250m, null),
        new(2, 1, 270m, null), new(2, 2, 270m, null),
        new(3, 1, 240m, null), new(3, 3, 300m, null)
    ];

    public static IReadOnlyList<OfertaCarrera> Ofertas => ofertas ??=
        Carreras.Where(c => c.Activo).Select(c => new OfertaCarrera(1, c.Id, c.Vacantes))
            .Concat(Carreras.Where(c => c.Activo && c.Id <= 4).Select(c => new OfertaCarrera(2, c.Id, c.Vacantes / 2)))
            .ToList();

    public static IReadOnlyList<RequisitoAsignado> RequisitosAsignados => requisitosAsignados ??=
    [
        new(1, 1, 1, true, 1), new(1, 1, 2, true, 2), new(1, 1, 3, true, 3),
        new(1, 2, 1, true, 1), new(1, 2, 2, true, 2), new(1, 2, 3, true, 3), new(1, 2, 4, true, 4),
        new(1, 3, 1, true, 1), new(1, 3, 2, true, 2), new(1, 3, 5, true, 3),
        new(1, 4, 1, true, 1), new(1, 4, 2, true, 2), new(1, 4, 6, true, 3), new(1, 4, 3, false, 4),
        new(2, 1, 1, true, 1), new(2, 1, 2, true, 2), new(2, 1, 3, true, 3),
        new(2, 2, 1, true, 1), new(2, 2, 2, true, 2), new(2, 2, 3, true, 3), new(2, 2, 4, true, 4)
    ];

    public static IReadOnlyList<Institucion> Instituciones => instituciones ??=
    [
        new() { Id = 1, Codigo = "COL-001", Nombre = "I.E. Santa Rosa de Lima", Tipo = "COLEGIO" },
        new() { Id = 2, Codigo = "COL-002", Nombre = "Colegio Nacional Mariscal Castilla", Tipo = "COLEGIO" },
        new() { Id = 3, Codigo = "COL-003", Nombre = "Colegio San Agustín", Tipo = "COLEGIO" },
        new() { Id = 4, Codigo = "UNI-001", Nombre = "Universidad Nacional de Ingeniería", Tipo = "UNIVERSIDAD" },
        new() { Id = 5, Codigo = "UNI-002", Nombre = "Universidad Peruana Cayetano Heredia", Tipo = "UNIVERSIDAD" },
        new() { Id = 6, Codigo = "COL-004", Nombre = "Lycée Franco-Péruvien", Pais = "Francia", Tipo = "COLEGIO" }
    ];

    public static IReadOnlyList<Antecedente> Antecedentes => antecedentes ??=
        Postulantes.Take(10).SelectMany((p, i) => new[]
        {
            new Antecedente { PostulanteId = p.Id, InstitucionId = i % 3 + 1, AnioInicio = 2019, AnioFin = 2023, Descripcion = "Educación secundaria completa." },
            new Antecedente { PostulanteId = p.Id, InstitucionId = 4 + i % 2, AnioInicio = 2024, AnioFin = 2025, Descripcion = i % 2 == 0 ? "Ciclo preuniversitario." : "Estudios generales, dos ciclos." }
        }.Take(i % 3 == 0 ? 1 : 2)).Select((a, n) => { a.Id = n + 1; return a; }).ToList();

    public static IReadOnlyList<Apoderado> Apoderados => apoderados ??=
        Postulantes.Where(p => p.Id % 3 == 0 || p.Id == PostulanteActualId).Select(p => new Apoderado
        {
            PostulanteId = p.Id,
            Nombres = p.Id % 2 == 0 ? "Rosa María" : "Jorge Luis",
            Apellidos = $"{p.ApellidoPaterno} {p.ApellidoMaterno}",
            Parentesco = p.Id % 2 == 0 ? "MADRE" : "PADRE",
            Documento = (40_000_000 + p.Id * 1_113_227 % 9_999_999).ToString(),
            Correo = $"apoderado.{p.Id}@example.com",
            Telefono = $"9{20_000_000 + p.Id * 4_117_333 % 79_999_999}"
        }).ToList();

    public static Apoderado? ApoderadoDe(int postulanteId) => Apoderados.FirstOrDefault(a => a.PostulanteId == postulanteId);

    public static IReadOnlyList<CambioEstado> Historial => historial ??= CrearHistorial();

    public static IReadOnlyList<Carne> Carnes => carnes ??= CrearCarnes();

    public static IReadOnlyList<ObservacionDocumento> Observaciones => observaciones ??= CrearObservaciones();

    private static List<CambioEstado> CrearHistorial()
    {
        var lista = new List<CambioEstado>();
        foreach (var p in Postulaciones)
        {
            string quien = Evaluadores.FirstOrDefault(e => e.Id == p.EvaluadorId)?.Nombre ?? "Oficina de admisión";
            lista.Add(new(p.Id, null, "Borrador", p.FechaRegistro, "Sistema", "Postulación creada por el postulante."));
            if (p.Estado == EstadoPostulacion.Borrador) continue;
            var envio = p.FechaEnvio ?? p.FechaRegistro;
            lista.Add(new(p.Id, "Borrador", "Enviada", envio, "Postulante", "Postulación enviada con todos sus documentos."));
            if (p.Estado == EstadoPostulacion.Enviada) continue;
            lista.Add(new(p.Id, "Enviada", "En revisión", envio.AddDays(2), quien, "Inicio de la revisión documental."));
            switch (p.Estado)
            {
                case EstadoPostulacion.Observada:
                    lista.Add(new(p.Id, "En revisión", "Observada", envio.AddDays(5), quien, p.Observacion ?? "Documento observado."));
                    break;
                case EstadoPostulacion.Admitida:
                    lista.Add(new(p.Id, "En revisión", "Admitida", envio.AddDays(30), quien, "Cumple requisitos y alcanzó vacante."));
                    break;
                case EstadoPostulacion.NoAdmitida:
                    lista.Add(new(p.Id, "En revisión", "No admitida", envio.AddDays(30), quien, "No alcanzó vacante en la carrera elegida."));
                    break;
            }
        }
        return lista;
    }

    private static List<Carne> CrearCarnes()
    {
        var lista = new List<Carne>();
        foreach (var p in Postulaciones.Where(x => x.Estado is EstadoPostulacion.EnRevision or EstadoPostulacion.Admitida or EstadoPostulacion.NoAdmitida))
        {
            var pago = Pagos.FirstOrDefault(x => x.PostulacionId == p.Id);
            if (pago?.Estado != EstadoPago.Aprobado) continue;
            lista.Add(new Carne
            {
                Id = lista.Count + 1,
                PostulacionId = p.Id,
                SedeId = p.SedeId,
                Codigo = $"CAR-{30_000 + p.Id * 17}",
                FechaGeneracion = pago.FechaPago ?? Hoy,
                VigenciaInicio = new(2026, 12, 1),
                VigenciaFin = new(2026, 12, 6),
                Aula = $"A-{200 + p.Id * 3}",
                Anulado = p.Estado == EstadoPostulacion.NoAdmitida,
                MotivoAnulacion = p.Estado == EstadoPostulacion.NoAdmitida ? "Anulado por inconsistencia en los datos del postulante." : null
            });
        }
        return lista;
    }

    private static List<ObservacionDocumento> CrearObservaciones()
    {
        var lista = new List<ObservacionDocumento>();
        foreach (var d in Documentos.Where(x => x.Estado == EstadoDocumento.Observado))
        {
            var p = Postulacion(d.PostulacionId)!;
            lista.Add(new ObservacionDocumento
            {
                Id = lista.Count + 1, DocumentoId = d.Id, EvaluadorId = p.EvaluadorId ?? 1, Tipo = TipoObservacion.Ilegible,
                Descripcion = d.Comentario ?? "El documento no es legible.", Fecha = d.FechaCarga.AddDays(3), Estado = EstadoObservacion.Pendiente
            });
        }
        foreach (var d in Documentos.Where(x => x.Estado == EstadoDocumento.Aprobado && x.Requisito == "Certificado de estudios").Take(2))
        {
            var p = Postulacion(d.PostulacionId)!;
            lista.Add(new ObservacionDocumento
            {
                Id = lista.Count + 1, DocumentoId = d.Id, EvaluadorId = p.EvaluadorId ?? 1, Tipo = TipoObservacion.Vencido,
                Descripcion = "La constancia adjunta superaba el plazo de vigencia.", Fecha = d.FechaCarga.AddDays(2),
                Estado = EstadoObservacion.Subsanada, FechaSubsanacion = d.FechaCarga.AddDays(4),
                ComentarioSubsanacion = "Se cargó una constancia reciente."
            });
        }
        return lista;
    }
}
