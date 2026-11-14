using AdmitUWA.Models;

namespace AdmitUWA.Data;

/// <summary>
/// Semilla del cascarón: crea un juego de datos de ejemplo nuevo para cada sesión.
/// Las fechas se calculan a partir de "hoy" para que la demostración funcione en cualquier fecha.
/// Se reemplazará por la API Java.
/// </summary>
public sealed class DatosDemo
{
    public const int PostulanteActualId = 4;
    public const int EvaluadorActualId = 1;

    public List<Facultad> Facultades { get; }
    public List<Carrera> Carreras { get; }
    public List<Sede> Sedes { get; }
    public List<Modalidad> Modalidades { get; }
    public List<Convocatoria> Convocatorias { get; }
    public List<OfertaCarrera> Ofertas { get; }
    public List<RequisitoAplicable> Requisitos { get; }
    public List<Postulante> Postulantes { get; }
    public List<Evaluador> Evaluadores { get; }
    public List<Postulacion> Postulaciones { get; } = [];
    public List<Pago> Pagos { get; } = [];
    public List<Voucher> Vouchers { get; } = [];
    public List<Documento> Documentos { get; } = [];
    public List<Observacion> Observaciones { get; } = [];
    public List<MovimientoEstado> Historial { get; } = [];
    public List<Carne> Carnes { get; } = [];
    public List<Antecedente> Antecedentes { get; }
    public List<DocumentoPersonal> DocumentosPersonales { get; }
    public List<Notificacion> Notificaciones { get; }

    private readonly DateOnly hoy;

    public DatosDemo(DateOnly hoy)
    {
        this.hoy = hoy;
        Facultades =
        [
            new() { Id = 1, Codigo = "FCI", Nombre = "Facultad de Ciencias e Ingeniería" },
            new() { Id = 2, Codigo = "FCS", Nombre = "Facultad de Ciencias Sociales" },
            new() { Id = 3, Codigo = "FAD", Nombre = "Facultad de Arquitectura y Diseño" },
            new() { Id = 4, Codigo = "FDE", Nombre = "Facultad de Derecho" },
            new() { Id = 5, Codigo = "FGA", Nombre = "Facultad de Gestión y Alta Dirección", Activo = false }
        ];
        Carreras =
        [
            new() { Id = 1, FacultadId = 1, Codigo = "INF", Nombre = "Ingeniería Informática" },
            new() { Id = 2, FacultadId = 1, Codigo = "IND", Nombre = "Ingeniería Industrial" },
            new() { Id = 3, FacultadId = 1, Codigo = "CIV", Nombre = "Ingeniería Civil" },
            new() { Id = 4, FacultadId = 2, Codigo = "PSI", Nombre = "Psicología" },
            new() { Id = 5, FacultadId = 2, Codigo = "COM", Nombre = "Comunicación Audiovisual" },
            new() { Id = 6, FacultadId = 3, Codigo = "ARQ", Nombre = "Arquitectura" },
            new() { Id = 7, FacultadId = 4, Codigo = "DER", Nombre = "Derecho" },
            new() { Id = 8, FacultadId = 5, Codigo = "ADM", Nombre = "Administración", Activo = false }
        ];
        Sedes =
        [
            new() { Id = 1, Nombre = "Campus Principal", Ciudad = "Lima", Direccion = "Av. Universitaria 1801, San Miguel" },
            new() { Id = 2, Nombre = "Sede Centro", Ciudad = "Lima", Direccion = "Jr. de la Unión 420, Cercado" },
            new() { Id = 3, Nombre = "Sede Arequipa", Ciudad = "Arequipa", Direccion = "Calle Mercaderes 215, Cercado" },
            new() { Id = 4, Nombre = "Sede Piura", Ciudad = "Piura", Direccion = "Av. Grau 880", Activo = false }
        ];
        Modalidades =
        [
            new() { Id = 1, Codigo = "ORD", Nombre = "Evaluación ordinaria", Descripcion = "Examen general para egresados de secundaria.", DerechoInscripcion = 250m },
            new() { Id = 2, Codigo = "TOP", Nombre = "Tercio y quinto superior", Descripcion = "Ingreso directo para los mejores promedios del colegio.", DerechoInscripcion = 250m },
            new() { Id = 3, Codigo = "TRA", Nombre = "Traslado externo", Descripcion = "Postulantes provenientes de otra universidad.", DerechoInscripcion = 320m },
            new() { Id = 4, Codigo = "DEP", Nombre = "Deportistas calificados", Descripcion = "Acreditación federativa y evaluación deportiva.", DerechoInscripcion = 250m }
        ];
        Convocatorias =
        [
            new()
            {
                Id = 1, Codigo = "ADM-2027-1", Nombre = "Admisión 2027-I", Periodo = "2027-I",
                Inicio = hoy.AddDays(-67), CierreInscripciones = hoy.AddDays(23), LimiteEvaluacion = hoy.AddDays(27), Fin = hoy.AddDays(38),
                Estado = EstadoConvocatoria.Publicada,
                Descripcion = "Proceso de admisión ordinario con ingreso en el primer semestre de 2027."
            },
            new()
            {
                Id = 2, Codigo = "ADM-2027-2", Nombre = "Admisión 2027-II", Periodo = "2027-II",
                Inicio = hoy.AddDays(114), CierreInscripciones = hoy.AddDays(170), LimiteEvaluacion = hoy.AddDays(176), Fin = hoy.AddDays(190),
                Estado = EstadoConvocatoria.Borrador,
                Descripcion = "Segunda convocatoria del año, en preparación."
            },
            new()
            {
                Id = 3, Codigo = "ADM-2026-2", Nombre = "Admisión 2026-II", Periodo = "2026-II",
                Inicio = hoy.AddDays(-250), CierreInscripciones = hoy.AddDays(-196), LimiteEvaluacion = hoy.AddDays(-190), Fin = hoy.AddDays(-176),
                Estado = EstadoConvocatoria.Cerrada,
                Descripcion = "Proceso finalizado. Resultados publicados."
            }
        ];

        int[] vacantesActual = [80, 70, 60, 90, 50, 55, 120];
        int[] vacantesAnterior = [70, 60, 50, 80, 40, 50, 100];
        Ofertas = [];
        for (int i = 0; i < 7; i++)
        {
            Ofertas.Add(new() { Id = Ofertas.Count + 1, ConvocatoriaId = 1, CarreraId = i + 1, Vacantes = vacantesActual[i] });
            Ofertas.Add(new() { Id = Ofertas.Count + 1, ConvocatoriaId = 3, CarreraId = i + 1, Vacantes = vacantesAnterior[i] });
        }

        Requisitos = [];
        foreach (var conv in new[] { 1, 3 })
        {
            foreach (var m in Modalidades)
            {
                AgregarRequisito(conv, m.Id, "Documento de identidad", FormatoArchivo.Cualquiera);
                AgregarRequisito(conv, m.Id, "Certificado de estudios", FormatoArchivo.Pdf);
                AgregarRequisito(conv, m.Id, "Fotografía tamaño carné", FormatoArchivo.Imagen);
                var especial = m.Id switch
                {
                    2 => "Constancia de orden de mérito",
                    3 => "Certificado universitario de notas",
                    4 => "Acreditación federativa",
                    _ => null
                };
                if (especial is not null) AgregarRequisito(conv, m.Id, especial, FormatoArchivo.Pdf);
            }
        }

        Postulantes = CrearPostulantes();
        Evaluadores =
        [
            new() { Id = 1, Nombre = "Dra. Carmen Villanueva", Correo = "c.villanueva@admitu.edu.pe", Especialidad = "Ingeniería" },
            new() { Id = 2, Nombre = "Mg. Rodrigo Salazar", Correo = "r.salazar@admitu.edu.pe", Especialidad = "Ciencias Sociales" },
            new() { Id = 3, Nombre = "Dr. Álvaro Quispe", Correo = "a.quispe@admitu.edu.pe", Especialidad = "Derecho" },
            new() { Id = 4, Nombre = "Mg. Lucía Fernández", Correo = "l.fernandez@admitu.edu.pe", Especialidad = "Arquitectura" },
            new() { Id = 5, Nombre = "Dr. Mateo Ríos", Correo = "m.rios@admitu.edu.pe", Especialidad = "Ingeniería" },
            new() { Id = 6, Nombre = "Mg. Paola Herrera", Correo = "p.herrera@admitu.edu.pe", Especialidad = "Psicología", Activo = false }
        ];

        CrearProcesoActual();
        CrearProcesoAnterior();

        Antecedentes =
        [
            new() { Id = 1, PostulanteId = PostulanteActualId, Tipo = "Certificado de notas", Institucion = "I.E. San Martín de Porres", Archivo = "certificado_notas.pdf", Fecha = hoy.AddDays(-40) }
        ];
        DocumentosPersonales =
        [
            new() { Id = 1, PostulanteId = PostulanteActualId, Tipo = "DNI", Archivo = "dni_luis_vargas.jpg", Fecha = hoy.AddDays(-40) },
            new() { Id = 2, PostulanteId = PostulanteActualId, Tipo = "Fotografía", Archivo = "foto_luis_vargas.png", Fecha = hoy.AddDays(-40) }
        ];
        Notificaciones = CrearNotificaciones();
    }

    private void AgregarRequisito(int conv, int modalidad, string nombre, FormatoArchivo formato) =>
        Requisitos.Add(new() { Id = Requisitos.Count + 1, ConvocatoriaId = conv, ModalidadId = modalidad, Nombre = nombre, Formato = formato });

    private List<Postulante> CrearPostulantes()
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
                Correo = $"{SinTildes(nombres[i])}.{SinTildes(paternos[i])}@example.com",
                Telefono = i % 4 == 3 ? null : $"9{(10_000_000 + i * 7_131_977 % 89_999_999)}",
                FechaNacimiento = new DateOnly(2006 + i % 3, 1 + i % 12, 3 + i * 2 % 25),
                TieneDiscapacidad = conadis,
                CarnetConadis = conadis ? $"CON-{40_000 + i * 113}" : null,
                CorreoValidado = i % 5 != 4
            });
        }
        return lista;
    }

    private static string SinTildes(string texto) => texto.ToLowerInvariant()
        .Replace("á", "a").Replace("é", "e").Replace("í", "i").Replace("ó", "o").Replace("ú", "u");

    private void CrearProcesoActual()
    {
        (EstadoPostulacion Estado, EstadoPago? Pago)[] casos =
        [
            (EstadoPostulacion.Apta, EstadoPago.Aprobado),
            (EstadoPostulacion.EnRevision, EstadoPago.Aprobado),
            (EstadoPostulacion.Enviada, EstadoPago.Pendiente),
            (EstadoPostulacion.Observada, EstadoPago.Pendiente),
            (EstadoPostulacion.EnRevision, EstadoPago.Pendiente),
            (EstadoPostulacion.Borrador, null),
            (EstadoPostulacion.Apta, EstadoPago.Aprobado),
            (EstadoPostulacion.Enviada, EstadoPago.Generado),
            (EstadoPostulacion.NoApta, EstadoPago.Aprobado),
            (EstadoPostulacion.EnRevision, EstadoPago.Aprobado),
            (EstadoPostulacion.Observada, EstadoPago.Rechazado),
            (EstadoPostulacion.Enviada, EstadoPago.Pendiente),
            (EstadoPostulacion.Borrador, null),
            (EstadoPostulacion.EnRevision, EstadoPago.Aprobado),
            (EstadoPostulacion.Apta, EstadoPago.Aprobado),
            (EstadoPostulacion.Enviada, EstadoPago.Generado)
        ];
        for (int i = 0; i < casos.Length; i++)
        {
            var (estado, estadoPago) = casos[i];
            var registro = hoy.AddDays(-(50 - i * 3));
            var p = new Postulacion
            {
                Id = Postulaciones.Count + 1,
                Codigo = $"POS-2027-{1001 + i}",
                PostulanteId = i + 1,
                ConvocatoriaId = 1,
                CarreraId = (i * 3) % 7 + 1,
                ModalidadId = i % 4 + 1,
                SedeId = i % 3 + 1,
                EvaluadorId = estado == EstadoPostulacion.Borrador ? null : i % 2 == 0 ? 1 : 2 + i / 2 % 3,
                Estado = estado,
                FechaRegistro = registro,
                FechaEnvio = estado == EstadoPostulacion.Borrador ? null : registro.AddDays(2),
                Observacion = estado == EstadoPostulacion.Observada
                    ? "El certificado de estudios no es legible. Cárguelo nuevamente en formato PDF."
                    : estado == EstadoPostulacion.NoApta ? "El certificado de estudios no corresponde al postulante." : null
            };
            Postulaciones.Add(p);
            if (estado == EstadoPostulacion.Borrador) continue;
            CrearDocumentos(p);
            CrearPago(p, estadoPago!.Value);
            CrearHistorial(p);
            // Carnés emitidos automáticamente; el caso i = 14 queda como apto sin carné (control).
            if (estado == EstadoPostulacion.Apta && estadoPago == EstadoPago.Aprobado && i != 14)
            {
                Carnes.Add(new Carne
                {
                    Id = Carnes.Count + 1, PostulacionId = p.Id, Codigo = $"CAR-2027-I-{Carnes.Count + 1:0000}",
                    SedeId = p.SedeId, Aula = $"A-{101 + p.Id % 12}", FechaEmision = p.FechaEnvio!.Value.AddDays(6)
                });
            }
        }
    }

    private void CrearProcesoAnterior()
    {
        var conv = Convocatorias.First(c => c.Id == 3);
        for (int i = 0; i < 8; i++)
        {
            var registro = conv.Inicio!.Value.AddDays(5 + i * 4);
            var final = i % 3 == 2 ? EstadoPostulacion.NoAdmitida : EstadoPostulacion.Admitida;
            var p = new Postulacion
            {
                Id = Postulaciones.Count + 1,
                Codigo = $"POS-2026-{2001 + i}",
                PostulanteId = 9 + i,
                ConvocatoriaId = conv.Id,
                CarreraId = (i * 2) % 7 + 1,
                ModalidadId = i % 2 + 1,
                SedeId = i % 3 + 1,
                EvaluadorId = i % 2 == 0 ? 1 : 3,
                Estado = final,
                FechaRegistro = registro,
                FechaEnvio = registro.AddDays(1)
            };
            Postulaciones.Add(p);
            CrearDocumentos(p);
            CrearPago(p, EstadoPago.Aprobado);
            CrearHistorial(p);
        }
    }

    private void CrearDocumentos(Postulacion p)
    {
        var requisitos = Requisitos.Where(r => r.ConvocatoriaId == p.ConvocatoriaId && r.ModalidadId == p.ModalidadId && r.Obligatorio).ToList();
        for (int k = 0; k < requisitos.Count; k++)
        {
            var r = requisitos[k];
            bool esCertificado = r.Nombre == "Certificado de estudios";
            var estado = p.Estado switch
            {
                EstadoPostulacion.Enviada => EstadoDocumento.Pendiente,
                EstadoPostulacion.EnRevision => k == 0 ? EstadoDocumento.Aprobado : EstadoDocumento.Pendiente,
                EstadoPostulacion.Observada => esCertificado ? EstadoDocumento.Observado : EstadoDocumento.Aprobado,
                EstadoPostulacion.NoApta => esCertificado ? EstadoDocumento.Rechazado : EstadoDocumento.Aprobado,
                _ => EstadoDocumento.Aprobado
            };
            var extension = r.Formato == FormatoArchivo.Imagen ? "jpg" : "pdf";
            var doc = new Documento
            {
                Id = Documentos.Count + 1,
                PostulacionId = p.Id,
                Requisito = r.Nombre,
                Archivo = $"{SinTildes(r.Nombre.Split(' ')[0])}_{p.PostulanteId:00}.{extension}",
                Tipo = extension.ToUpperInvariant(),
                TamanioKb = 380 + (p.Id * 37 + k * 211) % 1400,
                FechaCarga = p.FechaEnvio ?? p.FechaRegistro,
                Estado = estado,
                Comentario = estado is EstadoDocumento.Observado or EstadoDocumento.Rechazado ? p.Observacion : null
            };
            Documentos.Add(doc);
            if (estado == EstadoDocumento.Observado)
            {
                Observaciones.Add(new Observacion
                {
                    Id = Observaciones.Count + 1, DocumentoId = doc.Id, EvaluadorId = p.EvaluadorId,
                    Tipo = TipoObservacion.Ilegible, Descripcion = p.Observacion ?? "", Fecha = doc.FechaCarga!.Value.AddDays(4)
                });
            }
        }
    }

    private void CrearPago(Postulacion p, EstadoPago estado)
    {
        var monto = Modalidades.First(m => m.Id == p.ModalidadId).DerechoInscripcion;
        var envio = (p.FechaEnvio ?? p.FechaRegistro)!.Value;
        var pago = new Pago
        {
            Id = Pagos.Count + 1,
            Codigo = $"PAG-{20_000 + p.Id * 37}",
            PostulacionId = p.Id,
            Monto = monto,
            FechaGeneracion = envio,
            FechaAprobacion = estado == EstadoPago.Aprobado ? envio.AddDays(2) : null,
            Estado = estado
        };
        Pagos.Add(pago);
        if (estado == EstadoPago.Generado) return;
        Vouchers.Add(new Voucher
        {
            Id = Vouchers.Count + 1,
            PostulacionId = p.Id,
            CodigoOperacion = $"TRF-{p.Id:000}-{(p.Id * 7919) % 100000:00000}",
            Archivo = $"voucher_{p.Codigo.ToLowerInvariant()}.{(p.Id % 2 == 0 ? "pdf" : "jpg")}",
            Monto = monto,
            FechaRegistro = envio.AddDays(1),
            Estado = estado switch
            {
                EstadoPago.Aprobado => EstadoVoucher.Aprobado,
                EstadoPago.Rechazado => EstadoVoucher.Rechazado,
                _ => EstadoVoucher.Pendiente
            },
            FechaRevision = estado is EstadoPago.Aprobado or EstadoPago.Rechazado ? envio.AddDays(2) : null,
            MotivoRechazo = estado == EstadoPago.Rechazado ? "El monto del voucher no coincide con el derecho de inscripción." : null
        });
    }

    private void CrearHistorial(Postulacion p)
    {
        var envio = (p.FechaEnvio ?? p.FechaRegistro)!.Value.ToDateTime(new TimeOnly(10, 15));
        var postulante = Postulantes.First(x => x.Id == p.PostulanteId).NombreCompleto;
        var evaluador = Evaluadores.FirstOrDefault(e => e.Id == p.EvaluadorId)?.Nombre ?? "Sistema";
        Movimiento(p, EstadoPostulacion.Borrador, EstadoPostulacion.Enviada, "Postulación enviada", postulante, envio);
        if (p.Estado == EstadoPostulacion.Enviada) return;
        Movimiento(p, EstadoPostulacion.Enviada, EstadoPostulacion.EnRevision, "Inicio de la revisión documental", evaluador, envio.AddDays(2));
        if (p.Estado == EstadoPostulacion.EnRevision) return;
        var decision = p.Estado is EstadoPostulacion.Admitida or EstadoPostulacion.NoAdmitida ? EstadoPostulacion.Apta : p.Estado;
        Movimiento(p, EstadoPostulacion.EnRevision, decision, p.Observacion ?? "Expediente completo y conforme", evaluador, envio.AddDays(4));
        if (decision != p.Estado)
            Movimiento(p, decision, p.Estado, "Publicación de resultados", "Sistema", envio.AddDays(40));
    }

    private void Movimiento(Postulacion p, EstadoPostulacion anterior, EstadoPostulacion nuevo, string motivo, string responsable, DateTime fecha) =>
        Historial.Add(new MovimientoEstado
        {
            Id = Historial.Count + 1, PostulacionId = p.Id, Anterior = anterior, Nuevo = nuevo,
            Motivo = motivo, Responsable = responsable, Fecha = fecha
        });

    private List<Notificacion> CrearNotificaciones()
    {
        var b = DateTime.Now;
        return
        [
            new() { Id = 1, Rol = RolUsuario.Administrador, Titulo = "Nueva postulación recibida", Detalle = "Renzo Medina Ponce envió su postulación a Derecho.", Tipo = "Postulación", Medio = "Sistema", Fecha = b.AddMinutes(-32) },
            new() { Id = 2, Rol = RolUsuario.Administrador, Titulo = "Voucher por validar", Detalle = "Se registró un voucher de S/ 250.00 para POS-2027-1012.", Tipo = "Pago", Medio = "Sistema", Fecha = b.AddHours(-1) },
            new() { Id = 3, Rol = RolUsuario.Administrador, Titulo = "Documento observado", Detalle = "El certificado de Luis Vargas Campos fue observado por la evaluadora.", Tipo = "Documento", Medio = "Correo", Fecha = b.AddHours(-3) },
            new() { Id = 4, Rol = RolUsuario.Administrador, Titulo = "Recordatorio de cierre", Detalle = "Quedan 23 días para el cierre de inscripciones.", Tipo = "Convocatoria", Medio = "Correo", Fecha = b.AddHours(-7), Leida = true },
            new() { Id = 5, Rol = RolUsuario.Administrador, Titulo = "Correo validado", Detalle = "Sofía Torres Silva confirmó su correo electrónico.", Tipo = "Postulante", Medio = "Correo", Fecha = b.AddDays(-1), Leida = true },
            new() { Id = 20, Rol = RolUsuario.Postulante, Titulo = "Tu postulación fue observada", Detalle = "El certificado de estudios no es legible. Cárgalo nuevamente en formato PDF.", Tipo = "Documento", Medio = "Correo", Fecha = b.AddHours(-2) },
            new() { Id = 21, Rol = RolUsuario.Postulante, Titulo = "Voucher recibido", Detalle = "Registramos tu voucher. Te avisaremos cuando sea validado.", Tipo = "Pago", Medio = "Sistema", Fecha = b.AddDays(-1), Leida = true },
            new() { Id = 22, Rol = RolUsuario.Postulante, Titulo = "Postulación enviada", Detalle = "Tu postulación a Ingeniería Civil fue enviada correctamente.", Tipo = "Postulación", Medio = "Correo", Fecha = b.AddDays(-39), Leida = true },
            new() { Id = 40, Rol = RolUsuario.Evaluador, Titulo = "Nuevas postulaciones asignadas", Detalle = "Se te asignaron 3 postulaciones de Ingeniería.", Tipo = "Postulación", Medio = "Sistema", Fecha = b.AddMinutes(-35) },
            new() { Id = 41, Rol = RolUsuario.Evaluador, Titulo = "Documento reemplazado", Detalle = "Un postulante cargó una nueva versión de su certificado.", Tipo = "Documento", Medio = "Sistema", Fecha = b.AddHours(-4) },
            new() { Id = 42, Rol = RolUsuario.Evaluador, Titulo = "Fecha límite de evaluación", Detalle = "Las evaluaciones de expedientes deben completarse en 27 días.", Tipo = "Convocatoria", Medio = "Correo", Fecha = b.AddDays(-2), Leida = true }
        ];
    }
}
