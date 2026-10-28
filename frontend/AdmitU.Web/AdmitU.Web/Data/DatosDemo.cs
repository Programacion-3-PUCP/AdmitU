using AdmitU.Web.Models;

namespace AdmitU.Web.Data;

// Datos de ejemplo fijos para maquetar la interfaz. No se modifican ni se guardan.
public static partial class DatosDemo
{
    public static readonly DateOnly Hoy = new(2026, 10, 24);

    // Identidad con la que se ingresa en cada área.
    public const int PostulanteActualId = 4;
    public const int EvaluadorActualId = 1;

    public static IReadOnlyList<Facultad> Facultades { get; } =
    [
        new() { Id = 1, Codigo = "FCI", Nombre = "Facultad de Ciencias e Ingeniería" },
        new() { Id = 2, Codigo = "FCS", Nombre = "Facultad de Ciencias Sociales" },
        new() { Id = 3, Codigo = "FAD", Nombre = "Facultad de Arquitectura y Diseño" },
        new() { Id = 4, Codigo = "FDE", Nombre = "Facultad de Derecho" },
        new() { Id = 5, Codigo = "FGA", Nombre = "Facultad de Gestión y Alta Dirección", Activo = false }
    ];

    public static IReadOnlyList<Carrera> Carreras { get; } =
    [
        new() { Id = 1, FacultadId = 1, Codigo = "INF", Nombre = "Ingeniería Informática", Vacantes = 80 },
        new() { Id = 2, FacultadId = 1, Codigo = "IND", Nombre = "Ingeniería Industrial", Vacantes = 70 },
        new() { Id = 3, FacultadId = 1, Codigo = "CIV", Nombre = "Ingeniería Civil", Vacantes = 60 },
        new() { Id = 4, FacultadId = 2, Codigo = "PSI", Nombre = "Psicología", Vacantes = 90 },
        new() { Id = 5, FacultadId = 2, Codigo = "COM", Nombre = "Comunicación Audiovisual", Vacantes = 50 },
        new() { Id = 6, FacultadId = 3, Codigo = "ARQ", Nombre = "Arquitectura", Vacantes = 55 },
        new() { Id = 7, FacultadId = 4, Codigo = "DER", Nombre = "Derecho", Vacantes = 120 },
        new() { Id = 8, FacultadId = 5, Codigo = "ADM", Nombre = "Administración", Vacantes = 65, Activo = false }
    ];

    public static IReadOnlyList<Sede> Sedes { get; } =
    [
        new() { Id = 1, Nombre = "Campus Principal", Ciudad = "Lima", Direccion = "Av. Universitaria 1801, San Miguel" },
        new() { Id = 2, Nombre = "Sede Centro", Ciudad = "Lima", Direccion = "Jr. de la Unión 420, Cercado" },
        new() { Id = 3, Nombre = "Sede Arequipa", Ciudad = "Arequipa", Direccion = "Calle Mercaderes 215, Cercado" },
        new() { Id = 4, Nombre = "Sede Piura", Ciudad = "Piura", Direccion = "Av. Grau 880", Activo = false }
    ];

    public static IReadOnlyList<Modalidad> Modalidades { get; } =
    [
        new() { Id = 1, Codigo = "ORD", Nombre = "Evaluación ordinaria", Descripcion = "Examen general para egresados de secundaria." },
        new() { Id = 2, Codigo = "TOP", Nombre = "Tercio y quinto superior", Descripcion = "Ingreso directo para los mejores promedios del colegio." },
        new() { Id = 3, Codigo = "TRA", Nombre = "Traslado externo", Descripcion = "Postulantes provenientes de otra universidad." },
        new() { Id = 4, Codigo = "DEP", Nombre = "Deportistas calificados", Descripcion = "Acreditación federativa y evaluación deportiva." }
    ];

    public static IReadOnlyList<Convocatoria> Convocatorias { get; } =
    [
        new()
        {
            Id = 1, Codigo = "ADM-2027-1", Nombre = "Admisión 2027-I", Periodo = "2027-I",
            Inicio = new(2026, 9, 1), Fin = new(2026, 12, 15), Estado = EstadoConvocatoria.Publicada,
            Descripcion = "Proceso de admisión ordinario con ingreso en el primer semestre de 2027."
        },
        new()
        {
            Id = 2, Codigo = "ADM-2027-2", Nombre = "Admisión 2027-II", Periodo = "2027-II",
            Inicio = new(2027, 3, 1), Fin = new(2027, 5, 15), Estado = EstadoConvocatoria.Borrador,
            Descripcion = "Segunda convocatoria del año, en preparación."
        },
        new()
        {
            Id = 3, Codigo = "ADM-2026-2", Nombre = "Admisión 2026-II", Periodo = "2026-II",
            Inicio = new(2026, 3, 1), Fin = new(2026, 5, 15), Estado = EstadoConvocatoria.Cerrada,
            Descripcion = "Proceso finalizado. Resultados publicados."
        }
    ];

    public static IReadOnlyList<Hito> Hitos { get; } =
    [
        new("Cierre de inscripciones", new(2026, 11, 30), "Último día para enviar la postulación."),
        new("Evaluación de admisión", new(2026, 12, 5), "Examen presencial en las sedes habilitadas."),
        new("Publicación de resultados", new(2026, 12, 15), "Lista de admitidos y observados."),
        new("Inicio de matrícula", new(2027, 1, 10), "Matrícula para postulantes admitidos.")
    ];

    public static IReadOnlyList<Postulante> Postulantes { get; } = CrearPostulantes();
    public static IReadOnlyList<Postulacion> Postulaciones { get; } = CrearPostulaciones();
    public static IReadOnlyList<Pago> Pagos { get; } = CrearPagos();
    public static IReadOnlyList<Documento> Documentos { get; } = CrearDocumentos();

    public static IReadOnlyList<Evaluador> Evaluadores { get; } = CrearEvaluadores();

    public static IReadOnlyList<Notificacion> Notificaciones { get; } = CrearNotificaciones();

    // Series para los gráficos del resumen (últimas 8 semanas).
    public static IReadOnlyList<int> PostulacionesPorSemana { get; } = [12, 18, 15, 24, 31, 28, 40, 46];
    public static IReadOnlyList<int> PostulantesPorSemana { get; } = [9, 14, 13, 19, 25, 22, 31, 36];
    public static IReadOnlyList<int> IngresosPorSemana { get; } = [3, 5, 4, 8, 11, 9, 14, 17];

    public static Postulante? Postulante(int id) => Postulantes.FirstOrDefault(x => x.Id == id);
    public static Carrera? Carrera(int id) => Carreras.FirstOrDefault(x => x.Id == id);
    public static Facultad? Facultad(int id) => Facultades.FirstOrDefault(x => x.Id == id);
    public static Modalidad? Modalidad(int id) => Modalidades.FirstOrDefault(x => x.Id == id);
    public static Sede? Sede(int id) => Sedes.FirstOrDefault(x => x.Id == id);
    public static Convocatoria? Convocatoria(int id) => Convocatorias.FirstOrDefault(x => x.Id == id);
    public static Postulacion? Postulacion(int id) => Postulaciones.FirstOrDefault(x => x.Id == id);

    private static List<Postulante> CrearPostulantes()
    {
        string[] nombres = ["Andrea", "Marco", "Valeria", "Luis", "Camila", "Diego", "Sofía", "Mateo", "Lucía", "Gabriel", "Daniela", "Sebastián", "Fernanda", "Joaquín", "Mariana", "Renzo"];
        string[] paternos = ["Salas", "Paredes", "Mendoza", "Vargas", "Rojas", "Castillo", "Torres", "Flores", "Chávez", "Ramos", "Gutiérrez", "Herrera", "Quispe", "Navarro", "Delgado", "Medina"];
        string[] maternos = ["Ríos", "León", "Chávez", "Campos", "Vega", "Ortiz", "Silva", "Cruz", "Luna", "Soto", "Aguilar", "Peña", "Mamani", "Cáceres", "Valdivia", "Ponce"];
        var lista = new List<Postulante>();
        for (int i = 0; i < nombres.Length; i++)
        {
            bool conadis = i == 5 || i == 11;
            lista.Add(new Postulante
            {
                Id = i + 1,
                Nombres = nombres[i],
                ApellidoPaterno = paternos[i],
                ApellidoMaterno = maternos[i],
                TipoDocumento = i == 7 ? "CE" : "DNI",
                NumeroDocumento = i == 7 ? "001482957" : (70_000_000 + i * 1_372_841 % 9_999_999).ToString(),
                Correo = $"{nombres[i].ToLowerInvariant().Replace("í", "i").Replace("á", "a")}.{paternos[i].ToLowerInvariant().Replace("á", "a").Replace("é", "e").Replace("í", "i").Replace("ú", "u")}@example.com",
                Telefono = i % 4 == 3 ? null : $"9{(10_000_000 + i * 7_131_977 % 89_999_999)}",
                FechaNacimiento = new DateOnly(2006 + i % 3, 1 + i % 12, 3 + i * 2 % 25),
                TieneDiscapacidad = conadis,
                CarnetConadis = conadis ? $"CON-{40_000 + i * 113}" : null,
                CorreoValidado = i % 5 != 4
            });
        }
        return lista;
    }

    private static List<Postulacion> CrearPostulaciones()
    {
        EstadoPostulacion[] estados =
        [
            EstadoPostulacion.Admitida, EstadoPostulacion.EnRevision, EstadoPostulacion.Enviada, EstadoPostulacion.Observada,
            EstadoPostulacion.EnRevision, EstadoPostulacion.Borrador, EstadoPostulacion.Admitida, EstadoPostulacion.Enviada,
            EstadoPostulacion.NoAdmitida, EstadoPostulacion.EnRevision, EstadoPostulacion.Observada, EstadoPostulacion.Enviada,
            EstadoPostulacion.Borrador, EstadoPostulacion.EnRevision, EstadoPostulacion.Admitida, EstadoPostulacion.Enviada
        ];
        var lista = new List<Postulacion>();
        for (int i = 0; i < estados.Length; i++)
        {
            var registro = Hoy.AddDays(-(50 - i * 3));
            lista.Add(new Postulacion
            {
                Id = i + 1,
                Codigo = $"POS-2027-{1001 + i}",
                PostulanteId = i + 1,
                ConvocatoriaId = 1,
                CarreraId = (i * 3) % 7 + 1,
                ModalidadId = i % 4 + 1,
                SedeId = i % 3 + 1,
                EvaluadorId = estados[i] == EstadoPostulacion.Borrador ? null : i % 2 == 0 ? 1 : 2 + i / 2 % 3,
                Estado = estados[i],
                FechaRegistro = registro,
                FechaEnvio = estados[i] == EstadoPostulacion.Borrador ? null : registro.AddDays(2),
                Observacion = estados[i] == EstadoPostulacion.Observada
                    ? "El certificado de estudios no es legible. Cárguelo nuevamente en formato PDF."
                    : null
            });
        }
        return lista;
    }

    private static List<Pago> CrearPagos()
    {
        var lista = new List<Pago>();
        foreach (var p in Postulaciones.Where(x => x.Estado != EstadoPostulacion.Borrador))
        {
            var estado = p.Id % 5 == 0 ? EstadoPago.Pendiente
                : p.Id % 7 == 0 ? EstadoPago.Rechazado
                : p.Id % 11 == 0 ? EstadoPago.Vencido
                : p.Id % 4 == 0 ? EstadoPago.Pendiente
                : EstadoPago.Aprobado;
            // El pago se acredita con el comprobante de una transferencia externa; Yape y tarjeta son minoría.
            var medio = p.Id % 6 == 0 ? "Yape" : p.Id % 8 == 0 ? "Tarjeta" : "Transferencia";
            lista.Add(new Pago
            {
                Id = lista.Count + 1,
                Codigo = $"PAG-{20_000 + p.Id * 37}",
                PostulacionId = p.Id,
                Medio = medio,
                Monto = p.ModalidadId == 3 ? 320m : 250m,
                FechaGeneracion = p.FechaEnvio ?? p.FechaRegistro,
                FechaPago = estado == EstadoPago.Aprobado ? (p.FechaEnvio ?? p.FechaRegistro).AddDays(1) : null,
                Estado = estado,
                Voucher = medio == "Transferencia" ? $"voucher_{p.Id:00}.jpg" : null,
                ComentarioVoucher = estado == EstadoPago.Rechazado ? "El monto del voucher no coincide con el derecho de inscripción." : null
            });
        }
        return lista;
    }

    private static List<Documento> CrearDocumentos()
    {
        var lista = new List<Documento>();
        foreach (var p in Postulaciones.Where(x => x.Estado != EstadoPostulacion.Borrador))
        {
            var estadoBase = p.Estado switch
            {
                EstadoPostulacion.Enviada => EstadoDocumento.Pendiente,
                EstadoPostulacion.Observada => EstadoDocumento.Observado,
                EstadoPostulacion.NoAdmitida => EstadoDocumento.Rechazado,
                _ => EstadoDocumento.Aprobado
            };
            lista.Add(new Documento
            {
                Id = lista.Count + 1, PostulacionId = p.Id, Requisito = "Documento de identidad",
                Archivo = $"dni_{p.PostulanteId:00}.pdf", TamanioKb = 420 + p.Id * 31,
                FechaCarga = p.FechaEnvio ?? p.FechaRegistro,
                Estado = p.Estado == EstadoPostulacion.Enviada ? EstadoDocumento.Pendiente : EstadoDocumento.Aprobado
            });
            lista.Add(new Documento
            {
                Id = lista.Count + 1, PostulacionId = p.Id, Requisito = "Certificado de estudios",
                Archivo = $"certificado_{p.PostulanteId:00}.pdf", TamanioKb = 1_100 + p.Id * 83,
                FechaCarga = p.FechaEnvio ?? p.FechaRegistro, Estado = estadoBase,
                Comentario = estadoBase == EstadoDocumento.Observado ? p.Observacion : null
            });
        }
        return lista;
    }

    private static List<Evaluador> CrearEvaluadores()
    {
        (string Nombre, string Correo, string Especialidad, bool Activo)[] base0 =
        [
            ("Dra. Carmen Villanueva", "c.villanueva@admitu.edu.pe", "Ingeniería", true),
            ("Mg. Rodrigo Salazar", "r.salazar@admitu.edu.pe", "Ciencias Sociales", true),
            ("Dr. Álvaro Quispe", "a.quispe@admitu.edu.pe", "Derecho", true),
            ("Mg. Lucía Fernández", "l.fernandez@admitu.edu.pe", "Arquitectura", true),
            ("Dr. Mateo Ríos", "m.rios@admitu.edu.pe", "Ingeniería", true),
            ("Mg. Paola Herrera", "p.herrera@admitu.edu.pe", "Psicología", false)
        ];
        var lista = new List<Evaluador>();
        for (int i = 0; i < base0.Length; i++)
        {
            var mias = Postulaciones.Where(p => p.EvaluadorId == i + 1).ToList();
            lista.Add(new Evaluador
            {
                Id = i + 1, Nombre = base0[i].Nombre, Correo = base0[i].Correo, Especialidad = base0[i].Especialidad, Activo = base0[i].Activo,
                Asignadas = mias.Count,
                Evaluadas = mias.Count(p => p.Estado is EstadoPostulacion.Admitida or EstadoPostulacion.NoAdmitida or EstadoPostulacion.Observada)
            });
        }
        return lista;
    }

    private static List<Notificacion> CrearNotificaciones()
    {
        var b = Hoy.ToDateTime(new TimeOnly(17, 40));
        return
        [
            // Administrador
            new() { Id = 1, Rol = "Administrador", Titulo = "Nueva postulación recibida", Detalle = "Renzo Medina Ponce envió su postulación a Derecho.", Tipo = "Postulación", Medio = "Sistema", Fecha = b.AddMinutes(-12) },
            new() { Id = 2, Rol = "Administrador", Titulo = "Pago confirmado", Detalle = "Se acreditó el pago PAG-20074 por S/ 250.00.", Tipo = "Pago", Medio = "Sistema", Fecha = b.AddHours(-1) },
            new() { Id = 3, Rol = "Administrador", Titulo = "Documento observado", Detalle = "El certificado de Camila Rojas fue observado por el evaluador.", Tipo = "Documento", Medio = "Correo", Fecha = b.AddHours(-3) },
            new() { Id = 4, Rol = "Administrador", Titulo = "Recordatorio de cierre", Detalle = "Quedan 37 días para el cierre de inscripciones.", Tipo = "Convocatoria", Medio = "Correo", Fecha = b.AddHours(-7), Leida = true },
            new() { Id = 5, Rol = "Administrador", Titulo = "Evaluador asignado", Detalle = "Dra. Carmen Villanueva recibió 6 nuevas postulaciones.", Tipo = "Postulación", Medio = "Sistema", Fecha = b.AddDays(-1), Leida = true },
            new() { Id = 6, Rol = "Administrador", Titulo = "Correo validado", Detalle = "Sofía Torres Silva confirmó su correo electrónico.", Tipo = "Postulante", Medio = "Correo", Fecha = b.AddDays(-1).AddHours(-2), Leida = true },
            new() { Id = 7, Rol = "Administrador", Titulo = "Pago pendiente", Detalle = "Tres postulaciones aún no registran el pago del derecho de inscripción.", Tipo = "Pago", Medio = "SMS", Fecha = b.AddDays(-2), Leida = true },
            new() { Id = 8, Rol = "Administrador", Titulo = "Convocatoria publicada", Detalle = "Admisión 2027-I ya está visible para los postulantes.", Tipo = "Convocatoria", Medio = "Sistema", Fecha = b.AddDays(-52), Leida = true },

            // Postulante
            new() { Id = 20, Rol = "Postulante", Titulo = "Tu postulación fue observada", Detalle = "El certificado de estudios no es legible. Cárgalo nuevamente en formato PDF.", Tipo = "Documento", Medio = "Correo", Fecha = b.AddHours(-2) },
            new() { Id = 21, Rol = "Postulante", Titulo = "Pago acreditado", Detalle = "Recibimos el pago de tu derecho de inscripción.", Tipo = "Pago", Medio = "Sistema", Fecha = b.AddDays(-1), Leida = true },
            new() { Id = 22, Rol = "Postulante", Titulo = "Postulación enviada", Detalle = "Tu postulación a Ingeniería Civil fue enviada correctamente.", Tipo = "Postulación", Medio = "Correo", Fecha = b.AddDays(-39), Leida = true },
            new() { Id = 23, Rol = "Postulante", Titulo = "Cierre de inscripciones", Detalle = "Las inscripciones cierran el 30 de noviembre.", Tipo = "Convocatoria", Medio = "Correo", Fecha = b.AddDays(-3), Leida = true },

            // Evaluador
            new() { Id = 40, Rol = "Evaluador", Titulo = "Nuevas postulaciones asignadas", Detalle = "Se te asignaron 3 postulaciones de Ingeniería.", Tipo = "Postulación", Medio = "Sistema", Fecha = b.AddMinutes(-35) },
            new() { Id = 41, Rol = "Evaluador", Titulo = "Documento reemplazado", Detalle = "Un postulante cargó una nueva versión de su certificado.", Tipo = "Documento", Medio = "Sistema", Fecha = b.AddHours(-4) },
            new() { Id = 42, Rol = "Evaluador", Titulo = "Fecha límite de evaluación", Detalle = "Las evaluaciones deben completarse antes del 4 de diciembre.", Tipo = "Convocatoria", Medio = "Correo", Fecha = b.AddDays(-2), Leida = true }
        ];
    }
}
