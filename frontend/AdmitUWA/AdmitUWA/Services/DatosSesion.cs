using AdmitUWA.Data;
using AdmitUWA.Models;
using AdmitUWA.Services.REST;

namespace AdmitUWA.Services;

/// <summary>
/// Fuente de datos de la sesión conectada en tiempo real a los servicios web RESTful de Java (admitu_services en GlassFish 8 -> MySQL RDS).
/// Mantiene la misma interfaz pública para las páginas Razor y sincroniza cada operación CRUD con el backend.
/// </summary>
public sealed partial class DatosSesion
{
    public const string Sistema = "Sistema";

    private readonly AdmitURestClient? _api;
    private readonly DatosDemo _demo;
    private readonly Microsoft.AspNetCore.Components.Authorization.AuthenticationStateProvider _autenticacion;
    private Task? _inicializacion;

    // Cachés auxiliares de tablas puente del backend para mapeo exacto de llaves foráneas
    private List<ConvocatoriaModalidadDto> _convModalidades = [];
    private List<OfertaCarreraDto> _ofertasDto = [];
    private List<RequisitoDto> _requisitosBaseDto = [];
    private List<InstitucionEducativaDto> _institucionesDto = [];

    public DatosSesion(AdmitURestClient api, Microsoft.AspNetCore.Components.Authorization.AuthenticationStateProvider autenticacion)
    {
        _api = api;
        _autenticacion = autenticacion;
        _demo = new DatosDemo(Hoy);
        Facultades.Clear(); Carreras.Clear(); Sedes.Clear(); Modalidades.Clear(); Convocatorias.Clear(); Ofertas.Clear(); Requisitos.Clear();
        Postulantes.Clear(); Evaluadores.Clear(); Postulaciones.Clear(); Pagos.Clear(); Vouchers.Clear(); Documentos.Clear(); Observaciones.Clear();
        Historial.Clear(); Carnes.Clear(); Antecedentes.Clear(); DocumentosPersonales.Clear(); Notificaciones.Clear();
    }

    public string? ErrorCarga { get; private set; }
    public bool CargaCompletada { get; private set; }
    public long UltimaCargaMilisegundos { get; private set; }

    // Una tarea por scope/circuito: varios componentes comparten la misma carga,
    // nunca las listas ni la identidad de otro usuario.
    public Task InicializarAsync() => _inicializacion ??= InicializarSesionAsync();

    private async Task InicializarSesionAsync()
    {
        try
        {
            var estado = await _autenticacion.GetAuthenticationStateAsync();
            _api!.ActualizarSesion(estado.User);
            PostulanteActualId = _api.Rol == "postulante" ? _api.PersonaId : 0;
            EvaluadorActualId = _api.Rol == "evaluador" ? _api.PersonaId : 0;
            await CargarDesdeBackendAsync();
        }
        catch (Exception ex) when (ex is HttpRequestException or System.Text.Json.JsonException or TaskCanceledException)
        {
            ErrorCarga = "No fue posible cargar los datos del servidor o la sesión expiró. Intenta recargar o vuelve a ingresar.";
        }
        finally { CargaCompletada = true; }
    }

    /// <summary>Se dispara cuando cambia algo que afecta a contadores o menús.</summary>
    public event Action? Cambio;

    public DateOnly Hoy => DateOnly.FromDateTime(TimeZoneInfo.ConvertTimeBySystemTimeZoneId(DateTimeOffset.UtcNow, "America/Lima").DateTime);
    public int PostulanteActualId { get; private set; }
    public int EvaluadorActualId { get; private set; }
    public string NombreAdministrador => "Admin Admisión";

    public List<Facultad> Facultades => _demo.Facultades;
    public List<Carrera> Carreras => _demo.Carreras;
    public List<Sede> Sedes => _demo.Sedes;
    public List<Modalidad> Modalidades => _demo.Modalidades;
    public List<Convocatoria> Convocatorias => _demo.Convocatorias;
    public List<OfertaCarrera> Ofertas => _demo.Ofertas;
    public List<RequisitoAplicable> Requisitos => _demo.Requisitos;
    public List<Postulante> Postulantes => _demo.Postulantes;
    public List<Evaluador> Evaluadores => _demo.Evaluadores;
    public List<Postulacion> Postulaciones => _demo.Postulaciones;
    public List<Pago> Pagos => _demo.Pagos;
    public List<Voucher> Vouchers => _demo.Vouchers;
    public List<Documento> Documentos => _demo.Documentos;
    public List<Observacion> Observaciones => _demo.Observaciones;
    public List<MovimientoEstado> Historial => _demo.Historial;
    public List<Carne> Carnes => _demo.Carnes;
    public List<Antecedente> Antecedentes => _demo.Antecedentes;
    public List<DocumentoPersonal> DocumentosPersonales => _demo.DocumentosPersonales;
    public List<Notificacion> Notificaciones => _demo.Notificaciones;

    // =========================================================================
    // SINCRONIZACIÓN CON BACKEND JAVA REST (GlassFish 8 + MySQL RDS)
    // =========================================================================
    // Compatibilidad con las acciones CRUD sincrónicas existentes. La carga inicial
    // y las operaciones con archivos usan el método asíncrono directamente.
    public void CargarDesdeBackend() => CargarDesdeBackendAsync().GetAwaiter().GetResult();

    public async Task CargarDesdeBackendAsync()
    {
        if (_api is null) return;
        var reloj = System.Diagnostics.Stopwatch.StartNew();

        // 1. Carga paralela desde los endpoints REST de GlassFish 8
        var tFac = _api.ListarTodosAsync<FacultadDto>("FacultadRS");
        var tCar = _api.ListarTodosAsync<CarreraDto>("CarreraRS");
        var tSed = _api.ListarTodosAsync<SedeDto>("SedeRS");
        var tConv = _api.ListarTodosAsync<ConvocatoriaDto>("ConvocatoriaRS");
        var tOfe = _api.ListarTodosAsync<OfertaCarreraDto>("OfertaCarreraRS");
        var tCm = _api.ListarTodosAsync<ConvocatoriaModalidadDto>("ConvocatoriaModalidadRS");
        var tMod = _api.ListarTodosAsync<ModalidadDto>("ModalidadRS");
        var tReq = _api.ListarTodosAsync<RequisitoDto>("RequisitoRS");
        var tRcm = _api.ListarTodosAsync<RequisitoConvocatoriaModalidadDto>("RequisitoConvocatoriaModalidadRS");
        var tPost = _api.ListarTodosAsync<PostulanteDto>("PostulanteRS");
        var tEval = _api.ListarTodosAsync<EvaluadorDto>("EvaluadorRS");
        var tInst = _api.ListarTodosAsync<InstitucionEducativaDto>("InstitucionEducativaRS");
        var tAnt = _api.ListarTodosAsync<AntecedenteAcademicoDto>("AntecedenteAcademicoRS");
        var tCarne = _api.ListarTodosAsync<CarnePostulanteDto>("CarnePostulanteRS");
        var tDoc = _api.ListarTodosAsync<DocumentoPostulacionDto>("DocumentoPostulacionRS");
        var tObs = _api.ListarTodosAsync<DocumentoObservacionDto>("DocumentoObservacionRS");
        var tPos = _api.ListarTodosAsync<PostulacionDto>("PostulacionRS");
        var tHis = _api.ListarTodosAsync<PostulacionHistorialDto>("PostulacionHistorialRS");
        var tPag = _api.ListarTodosAsync<PagoDto>("PagoRS");

        await Task.WhenAll(tFac, tCar, tSed, tConv, tOfe, tCm, tMod, tReq, tRcm, tPost, tEval, tInst, tAnt, tCarne, tDoc, tObs, tPos, tHis, tPag).ConfigureAwait(false);

        var facDtos = await tFac;
        Facultades.Clear();
        foreach (var f in facDtos)
        {
            Facultades.Add(new Facultad
            {
                Id = f.Id,
                Codigo = f.Codigo,
                Nombre = f.Nombre,
                Activo = f.Activo
            });
        }

        // 2. Carreras
        var carDtos = await tCar;
        Carreras.Clear();
        foreach (var c in carDtos)
        {
            Carreras.Add(new Carrera
            {
                Id = c.Id,
                FacultadId = c.Facultad?.Id ?? 1,
                Codigo = c.CodigoCarrera,
                Nombre = c.Nombre,
                Activo = c.Activo
            });
        }

        // 3. Sedes
        var sedeDtos = await tSed;
        Sedes.Clear();
        foreach (var s in sedeDtos)
        {
            Sedes.Add(new Sede
            {
                Id = s.Id,
                Codigo = s.Codigo,
                Nombre = s.Nombre,
                Ciudad = "Lima",
                Direccion = s.Direccion ?? "",
                Activo = s.Activo
            });
        }

        // 4. Convocatorias, Ofertas y ConvocatoriaModalidad
        var convDtos = await tConv;
        Convocatorias.Clear();
        foreach (var c in convDtos.Where(c => c.Activo))
        {
            var ini = c.FechaInicio;
            var fin = c.FechaFin;
            Convocatorias.Add(new Convocatoria
            {
                Id = c.Id,
                Codigo = c.CodigoConvocatoria,
                Nombre = c.Nombre,
                Periodo = c.Periodo,
                Inicio = ini,
                CierreInscripciones = fin,
                LimiteEvaluacion = null,
                Fin = fin,
                Estado = MapEstadoConvocatoria(c.Estado),
                Descripcion = c.Descripcion ?? ""
            });
        }

        _ofertasDto = await tOfe;
        Ofertas.Clear();
        foreach (var o in _ofertasDto)
        {
            Ofertas.Add(new OfertaCarrera
            {
                Id = o.Id,
                ConvocatoriaId = o.Convocatoria?.Id ?? ConvocatoriaPorDefecto,
                CarreraId = o.Carrera?.Id ?? 1,
                Vacantes = o.CantidadVacantes
            });
        }

        _convModalidades = await tCm;

        // 5. Modalidades
        var modDtos = await tMod;
        Modalidades.Clear();
        foreach (var m in modDtos)
        {
            var cm = _convModalidades.FirstOrDefault(x => x.Convocatoria?.Id == ConvocatoriaPorDefecto && x.Modalidad?.Id == m.Id);
            Modalidades.Add(new Modalidad
            {
                Id = m.Id,
                Codigo = m.CodigoModalidad,
                Nombre = m.Nombre,
                Descripcion = m.Descripcion ?? "",
                DerechoInscripcion = cm is not null ? (decimal)cm.CostoInscripcion : 0m,
                Activo = m.Activo
            });
        }

        // 6. Requisitos y RequisitoConvocatoriaModalidad
        _requisitosBaseDto = await tReq;
        var rcmDtos = await tRcm;
        Requisitos.Clear();
        foreach (var rcm in rcmDtos)
        {
            var cm = _convModalidades.FirstOrDefault(x => x.Id == rcm.ConvocatoriaModalidad?.Id);
            var reqBase = rcm.Requisito ?? _requisitosBaseDto.FirstOrDefault(x => x.Id == rcm.Requisito?.Id);
            Requisitos.Add(new RequisitoAplicable
            {
                Id = rcm.Id,
                RequisitoId = reqBase?.Id ?? 0,
                ConvocatoriaModalidadId = cm?.Id ?? rcm.ConvocatoriaModalidad?.Id ?? 1,
                ConvocatoriaId = cm?.Convocatoria?.Id ?? ConvocatoriaPorDefecto,
                ModalidadId = cm?.Modalidad?.Id ?? 1,
                Nombre = reqBase?.Nombre ?? "Documento requerido",
                Formato = MapFormatoArchivo(reqBase?.TipoArchivoRequerido),
                Obligatorio = rcm.Obligatorio,
                OrdenPresentacion = rcm.OrdenPresentacion ?? 1,
                Activo = reqBase?.Activo ?? true
            });
        }

        // 7. Postulantes y Evaluadores
        var postDtos = await tPost;
        Postulantes.Clear();
        foreach (var p in postDtos)
        {
            Postulantes.Add(new Postulante
            {
                Id = p.Id,
                Nombres = p.Nombres,
                ApellidoPaterno = p.ApellidoPaterno,
                ApellidoMaterno = p.ApellidoMaterno ?? "",
                TipoDocumento = p.TipoDocumento,
                NumeroDocumento = p.NumeroDocumento,
                Correo = p.Correo,
                Telefono = p.Telefono,
                FechaNacimiento = p.FechaNacimiento ?? new DateOnly(2007, 1, 1),
                TieneDiscapacidad = p.TieneDiscapacidad,
                CarnetConadis = p.NumeroCarnetConadis,
                CorreoValidado = p.CorreoValidado
            });
        }

        var evalDtos = await tEval;
        Evaluadores.Clear();
        foreach (var e in evalDtos)
        {
            Evaluadores.Add(new Evaluador
            {
                Id = e.Id,
                Nombre = $"{e.Nombres} {e.ApellidoPaterno} {e.ApellidoMaterno}".Trim(),
                Correo = e.Correo,
                TipoDocumento = e.TipoDocumento,
                NumeroDocumento = e.NumeroDocumento,
                Telefono = e.Telefono ?? "",
                Especialidad = e.Cargo,
                Activo = e.Activo
            });
        }

        // 8. Instituciones y Antecedentes Académicos
        _institucionesDto = await tInst;
        var antDtos = await tAnt;
        Antecedentes.Clear();
        foreach (var a in antDtos)
        {
            var inst = a.Institucion ?? _institucionesDto.FirstOrDefault(i => i.Id == a.Institucion?.Id);
            Antecedentes.Add(new Antecedente
            {
                Id = a.Id,
                PostulanteId = a.Postulante?.Id ?? PostulanteActualId,
                InstitucionId = inst?.Id ?? 1,
                Tipo = inst?.TipoInstitucion == "UNIVERSIDAD" ? "Certificado universitario" : "Certificado de notas",
                Institucion = inst?.Nombre ?? "No registrada",
                Archivo = !string.IsNullOrWhiteSpace(a.Descripcion) && a.Descripcion.EndsWith(".pdf", StringComparison.OrdinalIgnoreCase)
                    ? a.Descripcion
                    : "",
                AnioInicio = a.AnioInicio,
                AnioFin = a.AnioFin,
                Fecha = null
            });
        }

        // 9.Carnés (para conocer la sede de cada postulación)
        var carneDtos = await tCarne;
        Carnes.Clear();
        foreach (var c in carneDtos)
        {
            var esAnulado = c.AulaExamen is not null && c.AulaExamen.StartsWith("ANULADO", StringComparison.OrdinalIgnoreCase);
            Carnes.Add(new Carne
            {
                Id = c.Id,
                PostulacionId = c.Postulacion?.Id ?? 1,
                Codigo = c.CodigoCarne,
                SedeId = c.Sede?.Id ?? (Sedes.FirstOrDefault()?.Id ?? 1),
                Aula = c.AulaExamen ?? "No registrada",
                FechaEmision = c.FechaGeneracion,
                InicioVigencia = c.FechaInicioVigencia,
                FinVigencia = c.FechaFinVigencia,
                Estado = esAnulado ? EstadoCarne.Anulado : EstadoCarne.Vigente,
                Automatico = true
            });
        }

        // 10. Documentos y Observaciones
        var docDtos = await tDoc;
        var obsDtos = await tObs;

        Documentos.Clear();
        DocumentosPersonales.Clear();
        foreach (var d in docDtos)
        {
            var reqAplicable = Requisitos.FirstOrDefault(r => r.Id == d.RequisitoAplicable?.Id);
            var reqNombre = d.RequisitoAplicable?.Requisito?.Nombre ?? reqAplicable?.Nombre ?? "Documento";
            Documentos.Add(new Documento
            {
                Id = d.Id,
                PostulacionId = d.Postulacion?.Id ?? 1,
                RequisitoAplicableId = d.RequisitoAplicable?.Id ?? (reqAplicable?.Id ?? 1),
                Requisito = reqNombre,
                Archivo = d.NombreArchivo,
                RutaArchivo = d.RutaArchivo,
                ArchivoActivo = d.ArchivoActivo,
                Tipo = d.TipoArchivo == "IMAGEN" ? "JPG" : "PDF",
                TamanioKb = (int)Math.Max(1, d.TamanioArchivo / 1024),
                Version = d.NumeroVersion,
                FechaCarga = d.FechaCarga,
                Estado = MapEstadoDocumento(d.EstadoDocumento),
                Comentario = d.ComentarioEvaluacion
            });
        }

        Observaciones.Clear();
        foreach (var o in obsDtos)
        {
            Observaciones.Add(new Observacion
            {
                Id = o.Id,
                DocumentoId = o.Documento?.Id ?? 1,
                EvaluadorId = o.Evaluador?.Id ?? EvaluadorActualId,
                Tipo = MapTipoObservacion(o.TipoObservacion),
                Descripcion = o.Descripcion,
                Fecha = o.FechaObservacion,
                Estado = o.EstadoObservacion == "SUBSANADA" ? EstadoObservacion.Subsanada : EstadoObservacion.Abierta,
                FechaSubsanacion = o.FechaSubsanacion,
                ComentarioSubsanacion = o.ComentarioSubsanacion
            });
        }

        // 11. Postulaciones e Historial
        var postulacionDtos = await tPos;
        Postulaciones.Clear();
        foreach (var p in postulacionDtos)
        {
            var oferta = _ofertasDto.FirstOrDefault(o => o.Id == p.CarreraElegida?.Id);
            var cm = _convModalidades.FirstOrDefault(m => m.Id == p.ModalidadElegida?.Id);
            var carnePost = Carnes.FirstOrDefault(c => c.PostulacionId == p.Id);
            var docIds = Documentos.Where(d => d.PostulacionId == p.Id).Select(d => d.Id).ToHashSet();
            var evalObs = Observaciones.FirstOrDefault(o => docIds.Contains(o.DocumentoId))?.EvaluadorId;
            var estadoPost = MapEstadoPostulacion(p.EstadoActual?.Id ?? 1, p.EstadoActual?.Codigo);

            Postulaciones.Add(new Postulacion
            {
                Id = p.Id,
                Codigo = p.CodigoInscripcion,
                PostulanteId = p.Postulante?.Id ?? 1,
                ConvocatoriaId = p.Convocatoria?.Id ?? ConvocatoriaPorDefecto,
                CarreraId = oferta?.Carrera?.Id ?? (Carreras.FirstOrDefault()?.Id ?? 1),
                ModalidadId = cm?.Modalidad?.Id ?? (Modalidades.FirstOrDefault()?.Id ?? 1),
                SedeId = p.SedeId ?? carnePost?.SedeId ?? 0,
                EvaluadorId = p.EvaluadorId,
                Estado = estadoPost,
                FechaRegistro = p.FechaRegistro,
                FechaEnvio = p.FechaEnvio,
                Observacion = p.ObservacionGeneral
            });
        }

        // Poblar DocumentosPersonales a partir de los documentos del postulante en BD
        foreach (var d in Documentos)
        {
            var post = Postulaciones.FirstOrDefault(x => x.Id == d.PostulacionId);
            if (post is null) continue;
            var tipoPersonal = d.Requisito.Contains("DNI", StringComparison.OrdinalIgnoreCase) ? "DNI"
                : d.Requisito.Contains("Foto", StringComparison.OrdinalIgnoreCase) ? "Fotografía" : null;
            if (tipoPersonal is not null && !DocumentosPersonales.Any(dp => dp.PostulanteId == post.PostulanteId && dp.Tipo == tipoPersonal))
            {
                DocumentosPersonales.Add(new DocumentoPersonal
                {
                    Id = d.Id,
                    PostulanteId = post.PostulanteId,
                    Tipo = tipoPersonal,
                    Archivo = d.Archivo,
                    Fecha = d.FechaCarga
                });
            }
        }

        var histDtos = await tHis;
        Historial.Clear();
        foreach (var h in histDtos)
        {
            Historial.Add(new MovimientoEstado
            {
                Id = h.Id,
                PostulacionId = h.Postulacion?.Id ?? 1,
                Anterior = h.EstadoAnterior is not null && h.EstadoAnterior.Id > 0
                    ? MapEstadoPostulacion(h.EstadoAnterior.Id, h.EstadoAnterior.Codigo)
                    : null,
                Nuevo = MapEstadoPostulacion(h.EstadoActual?.Id ?? 1, h.EstadoActual?.Codigo),
                Fecha = h.FechaCambio?.ToDateTime(TimeOnly.MinValue),
                Responsable = string.IsNullOrWhiteSpace(h.ResponsableCambio) ? "No registrado" : h.ResponsableCambio,
                Motivo = h.MotivoCambio
            });
        }

        // 12. Pagos y Vouchers
        var pagoDtos = await tPag;
        Pagos.Clear();
        Vouchers.Clear();
        foreach (var pg in pagoDtos.OrderBy(x => x.Id))
        {
            var postId = pg.Postulacion?.Id ?? 1;
            var estadoPago = MapEstadoPago(pg.EstadoPago);

            // Actualizar o agregar el resumen de pago de la postulación
            var existente = Pagos.FirstOrDefault(x => x.PostulacionId == postId);
            if (existente is null)
            {
                Pagos.Add(new Pago
                {
                    Id = pg.Id,
                    Codigo = pg.CodigoPago,
                    PostulacionId = postId,
                    Monto = (decimal)pg.Monto,
                    FechaGeneracion = pg.FechaGeneracion,
                    FechaAprobacion = pg.FechaPago,
                    Estado = estadoPago
                });
            }
            else if (pg.EstadoPago != "VENCIDO")
            {
                existente.Id = pg.Id;
                existente.Codigo = pg.CodigoPago;
                existente.Monto = (decimal)pg.Monto;
                existente.FechaAprobacion = pg.FechaPago;
                existente.Estado = estadoPago;
            }

            // Si tiene referencia o voucher y no está eliminado/vencido ni solo generado, agregar a Vouchers
            if (pg.EstadoPago != "VENCIDO" && pg.EstadoPago != "GENERADO" &&
                (!string.IsNullOrWhiteSpace(pg.ReferenciaPasarela) || !string.IsNullOrWhiteSpace(pg.RutaVoucher)))
            {
                Vouchers.Add(new Voucher
                {
                    Id = pg.Id,
                    PostulacionId = postId,
                    CodigoOperacion = !string.IsNullOrWhiteSpace(pg.ReferenciaPasarela) ? pg.ReferenciaPasarela : pg.CodigoPago,
                    Monto = (decimal)pg.Monto,
                    Archivo = !string.IsNullOrWhiteSpace(pg.RutaVoucher) ? Path.GetFileName(pg.RutaVoucher) : "",
                    FechaRegistro = pg.FechaGeneracion,
                    FechaRevision = pg.FechaPago,
                    Estado = pg.EstadoPago switch
                    {
                        "APROBADO" => EstadoVoucher.Aprobado,
                        "RECHAZADO" => EstadoVoucher.Rechazado,
                        _ => EstadoVoucher.Pendiente
                    },
                    MotivoRechazo = pg.EstadoPago == "RECHAZADO" ? "El comprobante fue observado o rechazado en validación." : null
                });
            }
        }

        reloj.Stop();
        UltimaCargaMilisegundos = reloj.ElapsedMilliseconds;
        ErrorCarga = null;
    }

    // ---- Mapeadores de enumeraciones ---------------------------------------
    private static EstadoConvocatoria MapEstadoConvocatoria(string? estado) =>
        (estado ?? "").ToUpperInvariant() switch
        {
            "PUBLICADA" => EstadoConvocatoria.Publicada,
            "CERRADA" => EstadoConvocatoria.Cerrada,
            _ => EstadoConvocatoria.Borrador
        };

    private static string MapEstadoConvocatoriaToDb(EstadoConvocatoria estado) =>
        estado switch
        {
            EstadoConvocatoria.Publicada => "PUBLICADA",
            EstadoConvocatoria.Cerrada => "CERRADA",
            _ => "BORRADOR"
        };

    private static FormatoArchivo MapFormatoArchivo(string? tipo) =>
        (tipo ?? "").ToUpperInvariant() switch
        {
            "IMAGEN" => FormatoArchivo.Imagen,
            "CUALQUIERA" => FormatoArchivo.Cualquiera,
            _ => FormatoArchivo.Pdf
        };

    private static string MapFormatoArchivoToDb(FormatoArchivo f) =>
        f switch
        {
            FormatoArchivo.Imagen => "IMAGEN",
            FormatoArchivo.Cualquiera => "CUALQUIERA",
            _ => "PDF"
        };

    private static EstadoDocumento MapEstadoDocumento(string? estado) =>
        (estado ?? "").ToUpperInvariant() switch
        {
            "APROBADO" => EstadoDocumento.Aprobado,
            "OBSERVADO" or "RECHAZADO" => EstadoDocumento.Observado,
            _ => EstadoDocumento.Pendiente
        };

    private static TipoObservacion MapTipoObservacion(string? tipo) =>
        (tipo ?? "").ToUpperInvariant() switch
        {
            "FALTANTE" => TipoObservacion.Faltante,
            "VENCIDO" => TipoObservacion.Vencido,
            "OTRO" => TipoObservacion.Otro,
            _ => TipoObservacion.Ilegible
        };

    public static string MapTipoObservacionToDb(TipoObservacion tipo) =>
        tipo switch
        {
            TipoObservacion.Faltante => "FALTANTE",
            TipoObservacion.Vencido => "VENCIDO",
            TipoObservacion.Otro => "OTRO",
            _ => "ILEGIBLE"
        };

    // Identificadores oficiales de estado_postulacion en MySQL (LAB11/LAB12).
    private static EstadoPostulacion MapEstadoPostulacion(int id, string? codigo) =>
        id switch
        {
            1 => EstadoPostulacion.Borrador,
            2 => EstadoPostulacion.Enviada,
            3 => EstadoPostulacion.EnRevision,
            4 => EstadoPostulacion.Observada,
            5 => EstadoPostulacion.Apta,
            6 => EstadoPostulacion.Finalizada,
            7 => EstadoPostulacion.NoApta,
            8 => EstadoPostulacion.Anulada,
            _ => throw new InvalidOperationException("Estado de postulación desconocido en el servicio")
        };

    private static int MapEstadoPostulacionToDbId(EstadoPostulacion estado) =>
        estado switch
        {
            EstadoPostulacion.Borrador => 1,
            EstadoPostulacion.Enviada => 2,
            EstadoPostulacion.EnRevision => 3,
            EstadoPostulacion.Observada => 4,
            EstadoPostulacion.Apta => 5,
            EstadoPostulacion.Finalizada or EstadoPostulacion.Admitida => 6,
            EstadoPostulacion.NoApta or EstadoPostulacion.NoAdmitida => 7,
            EstadoPostulacion.Anulada => 8,
            _ => throw new InvalidOperationException("Transición de postulación no reconocida")
        };

    private static EstadoPago MapEstadoPago(string? estado) =>
        (estado ?? "").ToUpperInvariant() switch
        {
            "APROBADO" => EstadoPago.Aprobado,
            "PENDIENTE" => EstadoPago.Pendiente,
            "RECHAZADO" => EstadoPago.Rechazado,
            _ => EstadoPago.Generado
        };

    // ---- Búsquedas -------------------------------------------------------
    public Facultad? Facultad(int id) => Facultades.FirstOrDefault(x => x.Id == id);
    public Carrera? Carrera(int id) => Carreras.FirstOrDefault(x => x.Id == id);
    public Sede? Sede(int id) => Sedes.FirstOrDefault(x => x.Id == id);
    public Modalidad? Modalidad(int id) => Modalidades.FirstOrDefault(x => x.Id == id);
    public Convocatoria? Convocatoria(int id) => Convocatorias.FirstOrDefault(x => x.Id == id);
    public Postulante? Postulante(int id) => Postulantes.FirstOrDefault(x => x.Id == id);
    public Postulacion? Postulacion(int id) => Postulaciones.FirstOrDefault(x => x.Id == id);
    public Evaluador? Evaluador(int? id) => id is null ? null : Evaluadores.FirstOrDefault(x => x.Id == id);
    public Documento? Documento(int id) => Documentos.FirstOrDefault(x => x.Id == id);

    public Postulante? PersonaDe(Postulacion? p) => p is null ? null : Postulante(p.PostulanteId);
    public Postulante? PersonaDePostulacion(int postulacionId) => PersonaDe(Postulacion(postulacionId));
    public Pago? PagoDe(int postulacionId) => Pagos.FirstOrDefault(x => x.PostulacionId == postulacionId);
    public List<Documento> DocumentosDe(int postulacionId) => Documentos.Where(x => x.PostulacionId == postulacionId).ToList();
    public List<Voucher> VouchersDe(int postulacionId) => Vouchers.Where(x => x.PostulacionId == postulacionId).OrderByDescending(x => x.Id).ToList();
    public List<MovimientoEstado> HistorialDe(int postulacionId) => Historial.Where(x => x.PostulacionId == postulacionId).OrderBy(x => x.Fecha).ThenBy(x => x.Id).ToList();
    public Carne? CarneVigenteDe(int postulacionId) => Carnes.FirstOrDefault(x => x.PostulacionId == postulacionId && x.Estado == EstadoCarne.Vigente);
    public List<Observacion> ObservacionesDe(int documentoId) => Observaciones.Where(o => o.DocumentoId == documentoId).OrderByDescending(o => o.Id).ToList();

    public Convocatoria? ConvocatoriaVigente => Convocatorias.FirstOrDefault(c => c.Estado == EstadoConvocatoria.Publicada);

    public int ConvocatoriaPorDefecto => (ConvocatoriaVigente ?? Convocatorias.OrderByDescending(c => c.Inicio).FirstOrDefault())?.Id ?? 0;

    public Postulante PostulanteActual => Postulante(PostulanteActualId) ?? throw new InvalidOperationException("La sesión no identifica un postulante válido.");
    public Evaluador EvaluadorActual => Evaluador(EvaluadorActualId) ?? throw new InvalidOperationException("La sesión no identifica un evaluador válido.");

    public IEnumerable<Postulacion> PostulacionesDeConvocatoria(int convocatoriaId) =>
        Postulaciones.Where(p => convocatoriaId == 0 || p.ConvocatoriaId == convocatoriaId);

    public Postulacion? PostulacionActualDelPostulante() =>
        Postulaciones.Where(p => p.PostulanteId == PostulanteActualId)
            .OrderByDescending(p => p.ConvocatoriaId == ConvocatoriaVigente?.Id)
            .ThenByDescending(p => p.FechaRegistro)
            .FirstOrDefault();

    public int VacantesDe(int convocatoriaId, int carreraId) =>
        Ofertas.FirstOrDefault(o => o.ConvocatoriaId == convocatoriaId && o.CarreraId == carreraId)?.Vacantes ?? 0;

    public List<RequisitoAplicable> RequisitosPara(int convocatoriaId, int modalidadId) =>
        Requisitos.Where(r => r.ConvocatoriaId == convocatoriaId && r.ModalidadId == modalidadId && r.Activo).ToList();

    public List<Hito> HitosDe(Convocatoria c)
    {
        var hitos = new List<Hito>();
        if (c.Inicio is { } inicio) hitos.Add(new("Apertura de inscripciones", inicio, "Inicio del registro de postulaciones."));
        if (c.CierreInscripciones is { } cierre) hitos.Add(new("Cierre de inscripciones", cierre, "Último día para enviar la postulación y el voucher."));
        if (c.LimiteEvaluacion is { } limite) hitos.Add(new("Fin de evaluación de expedientes", limite, "Fecha límite para que los evaluadores resuelvan."));
        return hitos;
    }

    // ---- Reglas del expediente --------------------------------------------
    public bool DocumentosCompletos(int postulacionId)
    {
        var p = Postulacion(postulacionId);
        return p is not null && IndicadoresExpediente.DocumentosCompletos(p, Requisitos, Documentos);
    }

    public bool EsApta(Postulacion p) => IndicadoresExpediente.EstadoApto(p.Estado)
        && DocumentosCompletos(p.Id) && PagoAprobado(p.Id);

    public bool PagoAprobado(int postulacionId) => PagoDe(postulacionId)?.Estado == EstadoPago.Aprobado;

    public string? MotivoNoApta(Postulacion p)
    {
        if (!DocumentosCompletos(p.Id)) return "Cada requisito obligatorio debe tener un archivo activo aprobado.";
        if (!PagoAprobado(p.Id)) return "El pago de inscripción debe estar validado.";
        return null;
    }

    public bool AptaSinCarne(Postulacion p) =>
        p.Estado == EstadoPostulacion.Apta && EsApta(p) && CarneVigenteDe(p.Id) is null;

    // =========================================================================
    // OPERACIONES PERSISTENTES EN BACKEND (RF_01 a RF_14)
    // =========================================================================

    // ---- RF_02: Selección de sesión / Login y Registro de Postulante --------
    [Obsolete("La sesión se establece exclusivamente mediante la autenticación del servidor.")]
    public bool IniciarSesionComo(string rol, string correo) => false;

    public Postulante RegistrarNuevoPostulante(Postulante nuevo, string clave)
    {
        if (_api is not null)
        {
            var dto = new PostulanteDto
            {
                Nombres = nuevo.Nombres,
                ApellidoPaterno = nuevo.ApellidoPaterno,
                ApellidoMaterno = nuevo.ApellidoMaterno,
                Correo = nuevo.Correo,
                TipoDocumento = nuevo.TipoDocumento,
                NumeroDocumento = nuevo.NumeroDocumento,
                Telefono = nuevo.Telefono,
                FechaNacimiento = nuevo.FechaNacimiento,
                TieneDiscapacidad = nuevo.TieneDiscapacidad,
                NumeroCarnetConadis = nuevo.CarnetConadis,
                CorreoValidado = false
            };
            nuevo.Id = _api.RegistrarCuentaAsync(dto, clave).GetAwaiter().GetResult();
        }
        if (nuevo.Id <= 0) nuevo.Id = SiguienteId(Postulantes, x => x.Id);
        Postulantes.Add(nuevo);
        PostulanteActualId = nuevo.Id;
        Notificar(RolUsuario.Administrador, "Nuevo postulante registrado", $"{nuevo.NombreCompleto} creó su cuenta.", "Postulante");
        Avisar();
        return nuevo;
    }

    // ---- RF_01: Catálogos y Convocatorias -----------------------------------
    public void GuardarFacultad(Facultad modelo)
    {
        if (modelo.Id == 0)
        {
            modelo.Codigo = modelo.Codigo.Trim().ToUpperInvariant();
            modelo.Nombre = modelo.Nombre.Trim();
            if (_api is not null)
            {
                var creado = _api.Insertar("FacultadRS", new FacultadDto { Codigo = modelo.Codigo, Nombre = modelo.Nombre, Activo = true });
                if (creado is not null) modelo.Id = creado.Id;
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Facultades, x => x.Id);
            Facultades.Add(modelo);
        }
        else
        {
            var f = Facultades.First(x => x.Id == modelo.Id);
            f.Nombre = modelo.Nombre.Trim();
            if (_api is not null)
            {
                _api.Modificar("FacultadRS", new FacultadDto { Id = f.Id, Codigo = f.Codigo, Nombre = f.Nombre, Activo = f.Activo });
            }
        }
        Avisar();
    }

    public void CambiarEstadoFacultad(Facultad f, bool activo)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        if (activo) _api.Reactivar("FacultadRS", f.Id);
        else if (!_api.Eliminar("FacultadRS", f.Id)) throw new InvalidOperationException("No se pudo dar de baja");
        f.Activo = activo;
        Avisar();
    }

    public void GuardarCarrera(Carrera modelo)
    {
        if (modelo.Id == 0)
        {
            modelo.Codigo = modelo.Codigo.Trim().ToUpperInvariant();
            modelo.Nombre = modelo.Nombre.Trim();
            if (_api is not null)
            {
                var creado = _api.Insertar("CarreraRS", new CarreraDto
                {
                    Facultad = new FacultadDto { Id = modelo.FacultadId },
                    CodigoCarrera = modelo.Codigo,
                    Nombre = modelo.Nombre,
                    Activo = true
                });
                if (creado is not null) modelo.Id = creado.Id;
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Carreras, x => x.Id);
            Carreras.Add(modelo);
        }
        else
        {
            var c = Carreras.First(x => x.Id == modelo.Id);
            c.Nombre = modelo.Nombre.Trim();
            c.FacultadId = modelo.FacultadId;
            if (_api is not null)
            {
                _api.Modificar("CarreraRS", new CarreraDto
                {
                    Id = c.Id,
                    Facultad = new FacultadDto { Id = c.FacultadId },
                    CodigoCarrera = c.Codigo,
                    Nombre = c.Nombre,
                    Activo = c.Activo
                });
            }
        }
        Avisar();
    }

    public void CambiarEstadoCarrera(Carrera c, bool activo)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        if (activo) _api.Reactivar("CarreraRS", c.Id);
        else if (!_api.Eliminar("CarreraRS", c.Id)) throw new InvalidOperationException("No se pudo dar de baja");
        c.Activo = activo;
        Avisar();
    }

    public void GuardarModalidad(Modalidad modelo, int convocatoriaId = 0)
    {
        if (convocatoriaId <= 0) convocatoriaId = ConvocatoriaPorDefecto;
        if (modelo.Id == 0)
        {
            modelo.Codigo = modelo.Codigo.Trim().ToUpperInvariant();
            modelo.Nombre = modelo.Nombre.Trim();
            modelo.Descripcion = modelo.Descripcion.Trim();
            if (_api is not null)
            {
                var creado = _api.Insertar("ModalidadRS", new ModalidadDto
                {
                    CodigoModalidad = modelo.Codigo,
                    Nombre = modelo.Nombre,
                    Descripcion = modelo.Descripcion,
                    RequiereColegio = true,
                    RequiereUniversidad = false,
                    Activo = true
                });
                if (creado is not null) modelo.Id = creado.Id;
                if (convocatoriaId > 0)
                {
                    var cm = _api.Insertar("ConvocatoriaModalidadRS", new ConvocatoriaModalidadDto
                    {
                        Convocatoria = new ConvocatoriaDto { Id = convocatoriaId },
                        Modalidad = new ModalidadDto { Id = modelo.Id },
                        CostoInscripcion = (double)modelo.DerechoInscripcion,
                        Observacion = modelo.Descripcion
                    });
                    if (cm is not null) _convModalidades.Add(cm);
                }
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Modalidades, x => x.Id);
            Modalidades.Add(modelo);
        }
        else
        {
            var m = Modalidades.First(x => x.Id == modelo.Id);
            m.Nombre = modelo.Nombre.Trim();
            m.Descripcion = modelo.Descripcion.Trim();
            m.DerechoInscripcion = modelo.DerechoInscripcion;
            if (_api is not null)
            {
                _api.Modificar("ModalidadRS", new ModalidadDto
                {
                    Id = m.Id,
                    CodigoModalidad = m.Codigo,
                    Nombre = m.Nombre,
                    Descripcion = m.Descripcion,
                    RequiereColegio = true,
                    RequiereUniversidad = false,
                    Activo = m.Activo
                });
                var cm = _convModalidades.FirstOrDefault(x => x.Convocatoria?.Id == convocatoriaId && x.Modalidad?.Id == m.Id);
                if (cm is not null)
                {
                    cm.CostoInscripcion = (double)m.DerechoInscripcion;
                    _api.Modificar("ConvocatoriaModalidadRS", new ConvocatoriaModalidadDto
                    {
                        Id = cm.Id,
                        Convocatoria = new ConvocatoriaDto { Id = cm.Convocatoria?.Id ?? convocatoriaId },
                        Modalidad = new ModalidadDto { Id = m.Id },
                        CostoInscripcion = (double)m.DerechoInscripcion,
                        Observacion = cm.Observacion ?? m.Descripcion
                    });
                }
                else if (convocatoriaId > 0)
                {
                    var creada = _api.Insertar("ConvocatoriaModalidadRS", new ConvocatoriaModalidadDto
                    {
                        Convocatoria = new ConvocatoriaDto { Id = convocatoriaId },
                        Modalidad = new ModalidadDto { Id = m.Id },
                        CostoInscripcion = (double)modelo.DerechoInscripcion,
                        Observacion = m.Descripcion
                    });
                    if (creada is not null) _convModalidades.Add(creada);
                }
            }
        }
        Avisar();
    }

    public void CambiarEstadoModalidad(Modalidad m, bool activo)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        if (activo) _api.Reactivar("ModalidadRS", m.Id);
        else if (!_api.Eliminar("ModalidadRS", m.Id)) throw new InvalidOperationException("No se pudo dar de baja");
        m.Activo = activo;
        Avisar();
    }

    public void GuardarSede(Sede modelo)
    {
        if (modelo.Id == 0)
        {
            modelo.Codigo = string.IsNullOrWhiteSpace(modelo.Codigo)
                ? $"SEDE{SiguienteId(Sedes, x => x.Id):00}"
                : modelo.Codigo.Trim().ToUpperInvariant();
            modelo.Nombre = modelo.Nombre.Trim();
            modelo.Ciudad = modelo.Ciudad.Trim();
            modelo.Direccion = modelo.Direccion.Trim();
            if (_api is not null)
            {
                var creado = _api.Insertar("SedeRS", new SedeDto
                {
                    Codigo = modelo.Codigo,
                    Nombre = modelo.Nombre,
                    Direccion = modelo.Direccion,
                    Activo = true
                });
                if (creado is not null) modelo.Id = creado.Id;
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Sedes, x => x.Id);
            Sedes.Add(modelo);
        }
        else
        {
            var s = Sedes.First(x => x.Id == modelo.Id);
            s.Nombre = modelo.Nombre.Trim();
            s.Ciudad = modelo.Ciudad.Trim();
            s.Direccion = modelo.Direccion.Trim();
            if (_api is not null)
            {
                _api.Modificar("SedeRS", new SedeDto
                {
                    Id = s.Id,
                    Codigo = string.IsNullOrWhiteSpace(s.Codigo) ? $"SEDE{s.Id:00}" : s.Codigo,
                    Nombre = s.Nombre,
                    Direccion = s.Direccion,
                    Activo = s.Activo
                });
            }
        }
        Avisar();
    }

    public void CambiarEstadoSede(Sede s, bool activo)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        if (activo) _api.Reactivar("SedeRS", s.Id);
        else if (!_api.Eliminar("SedeRS", s.Id)) throw new InvalidOperationException("No se pudo dar de baja");
        s.Activo = activo;
        Avisar();
    }

    public void GuardarConvocatoria(Convocatoria modelo)
    {
        if (modelo.Id == 0)
        {
            modelo.Codigo = modelo.Codigo.Trim().ToUpperInvariant();
            modelo.Nombre = modelo.Nombre.Trim();
            modelo.Periodo = modelo.Periodo.Trim();
            modelo.Descripcion = modelo.Descripcion.Trim();
            modelo.Estado = EstadoConvocatoria.Borrador;
            if (_api is not null)
            {
                var creado = _api.Insertar("ConvocatoriaRS", new ConvocatoriaDto
                {
                    CodigoConvocatoria = modelo.Codigo,
                    Nombre = modelo.Nombre,
                    Periodo = modelo.Periodo,
                    FechaInicio = modelo.Inicio,
                    FechaFin = modelo.Fin,
                    Estado = "BORRADOR",
                    Descripcion = modelo.Descripcion,
                    Activo = true
                });
                if (creado is not null) modelo.Id = creado.Id;
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Convocatorias, x => x.Id);
            Convocatorias.Add(modelo);
        }
        else
        {
            var c = Convocatorias.First(x => x.Id == modelo.Id);
            c.Nombre = modelo.Nombre.Trim();
            c.Periodo = modelo.Periodo.Trim();
            c.Inicio = modelo.Inicio;
            c.CierreInscripciones = modelo.CierreInscripciones;
            c.LimiteEvaluacion = modelo.LimiteEvaluacion;
            c.Fin = modelo.Fin;
            c.Descripcion = modelo.Descripcion.Trim();
            if (_api is not null)
            {
                _api.Modificar("ConvocatoriaRS", new ConvocatoriaDto
                {
                    Id = c.Id,
                    CodigoConvocatoria = c.Codigo,
                    Nombre = c.Nombre,
                    Periodo = c.Periodo,
                    FechaInicio = c.Inicio,
                    FechaFin = c.Fin,
                    Estado = MapEstadoConvocatoriaToDb(c.Estado),
                    Descripcion = c.Descripcion,
                    Activo = true
                });
            }
        }
        Avisar();
    }

    public void CambiarEstadoConvocatoria(Convocatoria c, EstadoConvocatoria nuevo)
    {
        c.Estado = nuevo;
        if (_api is not null)
        {
            _api.Modificar("ConvocatoriaRS", new ConvocatoriaDto
            {
                Id = c.Id,
                CodigoConvocatoria = c.Codigo,
                Nombre = c.Nombre,
                Periodo = c.Periodo,
                FechaInicio = c.Inicio,
                FechaFin = c.Fin,
                Estado = MapEstadoConvocatoriaToDb(c.Estado),
                Descripcion = c.Descripcion,
                Activo = true
            });
        }
        Avisar();
    }

    // ---- RF_07: Oferta de Vacantes por Carrera y Convocatoria ---------------
    public void GuardarOfertaVacantes(int convocatoriaId, IEnumerable<(int CarreraId, int Vacantes)> filas)
    {
        foreach (var (carreraId, vacantes) in filas)
        {
            var existenteDto = _ofertasDto.FirstOrDefault(o => o.Convocatoria?.Id == convocatoriaId && o.Carrera?.Id == carreraId);
            var existenteLocal = Ofertas.FirstOrDefault(o => o.ConvocatoriaId == convocatoriaId && o.CarreraId == carreraId);

            if (vacantes > 0)
            {
                if (existenteDto is not null && _api is not null)
                {
                    existenteDto.CantidadVacantes = vacantes;
                    _api.Modificar("OfertaCarreraRS", new OfertaCarreraDto
                    {
                        Id = existenteDto.Id,
                        Convocatoria = new ConvocatoriaDto { Id = convocatoriaId },
                        Carrera = new CarreraDto { Id = carreraId },
                        CantidadVacantes = vacantes
                    });
                }
                else if (_api is not null)
                {
                    var creado = _api.Insertar("OfertaCarreraRS", new OfertaCarreraDto
                    {
                        Convocatoria = new ConvocatoriaDto { Id = convocatoriaId },
                        Carrera = new CarreraDto { Id = carreraId },
                        CantidadVacantes = vacantes
                    });
                    if (creado is not null) _ofertasDto.Add(creado);
                }

                if (existenteLocal is not null)
                {
                    existenteLocal.Vacantes = vacantes;
                }
                else
                {
                    var nuevoId = _ofertasDto.FirstOrDefault(o => o.Convocatoria?.Id == convocatoriaId && o.Carrera?.Id == carreraId)?.Id
                                  ?? SiguienteId(Ofertas, x => x.Id);
                    Ofertas.Add(new OfertaCarrera { Id = nuevoId, ConvocatoriaId = convocatoriaId, CarreraId = carreraId, Vacantes = vacantes });
                }
            }
            else if (existenteLocal is not null)
            {
                Ofertas.Remove(existenteLocal);
            }
        }
        Avisar();
    }

    // ---- RF_04: Configuración de Requisitos por Convocatoria y Modalidad ----
    public void GuardarRequisito(RequisitoAplicable modelo)
    {
        var nombre = modelo.Nombre.Trim();
        if (modelo.Id == 0)
        {
            int cmId = ObtenerOCrearConvocatoriaModalidadId(modelo.ConvocatoriaId, modelo.ModalidadId);
            int reqId = 0;
            if (_api is not null)
            {
                var codigoReq = $"REQ-{SiguienteId(_requisitosBaseDto, x => x.Id) + Random.Shared.Next(10, 99)}";
                var nuevoReq = _api.Insertar("RequisitoRS", new RequisitoDto
                {
                    CodigoRequisito = codigoReq,
                    Nombre = nombre,
                    Descripcion = nombre,
                    TipoArchivoRequerido = MapFormatoArchivoToDb(modelo.Formato),
                    TamanioMaximoBytes = 5242880,
                    Activo = true
                });
                if (nuevoReq is not null)
                {
                    reqId = nuevoReq.Id;
                    _requisitosBaseDto.Add(nuevoReq);
                }

                var orden = Requisitos.Count(r => r.ConvocatoriaId == modelo.ConvocatoriaId && r.ModalidadId == modelo.ModalidadId) + 1;
                var nuevoRcm = _api.Insertar("RequisitoConvocatoriaModalidadRS", new RequisitoConvocatoriaModalidadDto
                {
                    ConvocatoriaModalidad = new ConvocatoriaModalidadDto { Id = cmId },
                    Requisito = new RequisitoDto { Id = reqId },
                    Obligatorio = modelo.Obligatorio,
                    OrdenPresentacion = orden
                });
                if (nuevoRcm is not null)
                {
                    modelo.Id = nuevoRcm.Id;
                    modelo.RequisitoId = reqId;
                    modelo.ConvocatoriaModalidadId = cmId;
                    modelo.OrdenPresentacion = orden;
                }
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Requisitos, x => x.Id);
            modelo.Nombre = nombre;
            Requisitos.Add(modelo);
        }
        else
        {
            var r = Requisitos.First(x => x.Id == modelo.Id);
            r.Nombre = nombre;
            r.Formato = modelo.Formato;
            r.Obligatorio = modelo.Obligatorio;
            if (_api is not null)
            {
                if (r.RequisitoId > 0)
                {
                    var reqBase = _requisitosBaseDto.FirstOrDefault(x => x.Id == r.RequisitoId);
                    _api.Modificar("RequisitoRS", new RequisitoDto
                    {
                        Id = r.RequisitoId,
                        CodigoRequisito = reqBase?.CodigoRequisito ?? $"REQ-{r.RequisitoId}",
                        Nombre = r.Nombre,
                        Descripcion = reqBase?.Descripcion ?? r.Nombre,
                        TipoArchivoRequerido = MapFormatoArchivoToDb(r.Formato),
                        TamanioMaximoBytes = reqBase?.TamanioMaximoBytes ?? 5242880,
                        Activo = r.Activo
                    });
                }
                _api.Modificar("RequisitoConvocatoriaModalidadRS", new RequisitoConvocatoriaModalidadDto
                {
                    Id = r.Id,
                    ConvocatoriaModalidad = new ConvocatoriaModalidadDto { Id = r.ConvocatoriaModalidadId > 0 ? r.ConvocatoriaModalidadId : 1 },
                    Requisito = new RequisitoDto { Id = r.RequisitoId > 0 ? r.RequisitoId : 1 },
                    Obligatorio = r.Obligatorio,
                    OrdenPresentacion = r.OrdenPresentacion > 0 ? r.OrdenPresentacion : 1
                });
            }
        }
        Avisar();
    }

    public void CambiarEstadoRequisito(RequisitoAplicable r, bool activo)
    {
        if (_api is null || r.RequisitoId <= 0) throw new InvalidOperationException("Requisito no disponible");
        if (activo) _api.Reactivar("RequisitoRS", r.RequisitoId);
        else if (!_api.Eliminar("RequisitoRS", r.RequisitoId)) throw new InvalidOperationException("No se pudo dar de baja");
        r.Activo = activo;
        Avisar();
    }

    public void EliminarBorradorConvocatoria(Convocatoria c)
    {
        if (c.Estado != EstadoConvocatoria.Borrador) throw new InvalidOperationException("Solo se eliminan borradores");
        if (_api is null || !_api.Eliminar("ConvocatoriaRS", c.Id)) throw new InvalidOperationException("No se pudo eliminar el borrador");
        Convocatorias.Remove(c);
        Avisar();
    }

    private int ObtenerOCrearConvocatoriaModalidadId(int convocatoriaId, int modalidadId)
    {
        var existente = _convModalidades.FirstOrDefault(x => x.Convocatoria?.Id == convocatoriaId && x.Modalidad?.Id == modalidadId);
        if (existente is not null) return existente.Id;
        if (_api is not null)
        {
            var mod = Modalidades.FirstOrDefault(m => m.Id == modalidadId);
            var creado = _api.Insertar("ConvocatoriaModalidadRS", new ConvocatoriaModalidadDto
            {
                Convocatoria = new ConvocatoriaDto { Id = convocatoriaId },
                Modalidad = new ModalidadDto { Id = modalidadId },
                CostoInscripcion = (double)(mod?.DerechoInscripcion ?? 350m),
                Observacion = mod?.Descripcion ?? "Habilitada"
            });
            if (creado is not null)
            {
                _convModalidades.Add(creado);
                return creado.Id;
            }
        }
        return 1;
    }

    // ---- Evaluadores --------------------------------------------------------
    public void GuardarEvaluador(Evaluador modelo)
    {
        var partes = modelo.Nombre.Trim().Split(' ', StringSplitOptions.RemoveEmptyEntries);
        var nombres = partes.Length > 0 ? partes[0] : modelo.Nombre.Trim();
        var paterno = partes.Length > 1 ? partes[1] : "Evaluador";
        var materno = partes.Length > 2 ? string.Join(" ", partes.Skip(2)) : "";

        if (modelo.Id == 0)
        {
            if (string.IsNullOrWhiteSpace(modelo.NumeroDocumento))
                modelo.NumeroDocumento = $"{30000000 + Random.Shared.Next(1000, 9999)}";
            if (_api is not null)
            {
                var creado = _api.Insertar("EvaluadorRS", new EvaluadorDto
                {
                    Nombres = nombres,
                    ApellidoPaterno = paterno,
                    ApellidoMaterno = materno,
                    Correo = modelo.Correo.Trim(),
                    TipoDocumento = string.IsNullOrWhiteSpace(modelo.TipoDocumento) ? "DNI" : modelo.TipoDocumento,
                    NumeroDocumento = modelo.NumeroDocumento.Trim(),
                    Telefono = string.IsNullOrWhiteSpace(modelo.Telefono) ? "955000111" : modelo.Telefono.Trim(),
                    Cargo = modelo.Especialidad.Trim(),
                    Activo = true
                });
                if (creado is not null) modelo.Id = creado.Id;
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Evaluadores, x => x.Id);
            Evaluadores.Add(modelo);
        }
        else
        {
            var e = Evaluadores.First(x => x.Id == modelo.Id);
            e.Nombre = modelo.Nombre.Trim();
            e.Especialidad = modelo.Especialidad.Trim();
            e.Telefono = modelo.Telefono.Trim();
            if (_api is not null)
            {
                _api.Modificar("EvaluadorRS", new EvaluadorDto
                {
                    Id = e.Id,
                    Nombres = nombres,
                    ApellidoPaterno = paterno,
                    ApellidoMaterno = materno,
                    Correo = e.Correo,
                    TipoDocumento = string.IsNullOrWhiteSpace(e.TipoDocumento) ? "DNI" : e.TipoDocumento,
                    NumeroDocumento = string.IsNullOrWhiteSpace(e.NumeroDocumento) ? "30002211" : e.NumeroDocumento,
                    Telefono = e.Telefono,
                    Cargo = e.Especialidad,
                    Activo = e.Activo
                });
            }
        }
        Avisar();
    }

    public void CambiarEstadoEvaluador(Evaluador e, bool activo)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        if (activo) _api.Reactivar("EvaluadorRS", e.Id);
        else if (!_api.Eliminar("EvaluadorRS", e.Id)) throw new InvalidOperationException("No se pudo dar de baja");
        e.Activo = activo;
        Avisar();
    }

    // ---- RF_05: Perfil del Postulante, Antecedentes y Documentos Personales -
    public void GuardarPerfilPostulante(Postulante yo)
    {
        if (_api is not null)
        {
            _api.Modificar("PostulanteRS", new PostulanteDto
            {
                Id = yo.Id,
                Nombres = yo.Nombres,
                ApellidoPaterno = yo.ApellidoPaterno,
                ApellidoMaterno = yo.ApellidoMaterno,
                Correo = yo.Correo,
                TipoDocumento = yo.TipoDocumento,
                NumeroDocumento = yo.NumeroDocumento,
                Telefono = yo.Telefono,
                FechaNacimiento = yo.FechaNacimiento,
                TieneDiscapacidad = yo.TieneDiscapacidad,
                NumeroCarnetConadis = yo.CarnetConadis,
                CorreoValidado = yo.CorreoValidado
            });
        }
        Avisar();
    }

    public void GuardarAntecedente(Antecedente modelo)
    {
        int instId = ObtenerOCrearInstitucionId(modelo.Institucion, modelo.Tipo);
        if (modelo.Id == 0)
        {
            modelo.PostulanteId = PostulanteActualId;
            modelo.InstitucionId = instId;
            modelo.Fecha = Hoy;
            if (_api is not null)
            {
                var creado = _api.Insertar("AntecedenteAcademicoRS", new AntecedenteAcademicoDto
                {
                    Postulante = new PostulanteDto { Id = PostulanteActualId },
                    Institucion = new InstitucionEducativaDto { Id = instId },
                    AnioInicio = modelo.AnioInicio,
                    AnioFin = modelo.AnioFin,
                    Descripcion = modelo.Archivo,
                    Activo = true
                });
                if (creado is not null) modelo.Id = creado.Id;
            }
            if (modelo.Id <= 0) modelo.Id = SiguienteId(Antecedentes, x => x.Id);
            Antecedentes.Add(modelo);
        }
        else
        {
            var a = Antecedentes.First(x => x.Id == modelo.Id);
            a.Tipo = modelo.Tipo;
            a.Institucion = modelo.Institucion.Trim();
            a.InstitucionId = instId;
            a.Archivo = modelo.Archivo;
            a.Fecha = Hoy;
            if (_api is not null)
            {
                _api.Modificar("AntecedenteAcademicoRS", new AntecedenteAcademicoDto
                {
                    Id = a.Id,
                    Postulante = new PostulanteDto { Id = a.PostulanteId },
                    Institucion = new InstitucionEducativaDto { Id = instId },
                    AnioInicio = a.AnioInicio,
                    AnioFin = a.AnioFin,
                    Descripcion = a.Archivo,
                    Activo = true
                });
            }
        }
        Avisar();
    }

    public void EliminarAntecedente(Antecedente a)
    {
        if (_api is not null) _api.Eliminar("AntecedenteAcademicoRS", a.Id);
        Antecedentes.Remove(a);
        Avisar();
    }

    private int ObtenerOCrearInstitucionId(string nombreInstitucion, string tipoCertificado)
    {
        var existente = _institucionesDto.FirstOrDefault(i => i.Nombre.Equals(nombreInstitucion.Trim(), StringComparison.OrdinalIgnoreCase));
        if (existente is not null) return existente.Id;
        if (_api is not null)
        {
            var tipoInst = tipoCertificado.Contains("universit", StringComparison.OrdinalIgnoreCase) ? "UNIVERSIDAD" : "COLEGIO";
            var creado = _api.Insertar("InstitucionEducativaRS", new InstitucionEducativaDto
            {
                Pais = new PaisDto { Id = 1 },
                CodigoExterno = $"IE-{Random.Shared.Next(100, 999)}",
                Nombre = nombreInstitucion.Trim(),
                TipoInstitucion = tipoInst,
                Activo = true
            });
            if (creado is not null)
            {
                _institucionesDto.Add(creado);
                return creado.Id;
            }
        }
        return _institucionesDto.FirstOrDefault()?.Id ?? 1;
    }

    // ---- RF_06: Registro de Postulación y Carga de Documentos ---------------
    public IEnumerable<Modalidad> ModalidadesParaConvocatoria(int convocatoriaId) => Modalidades.Where(m => m.Activo && _convModalidades.Any(cm => cm.Convocatoria?.Id == convocatoriaId && cm.Modalidad?.Id == m.Id));

    public async Task<Postulacion> EnviarNuevaPostulacionAsync(Convocatoria c, int carreraId, int modalidadId, int sedeId, Dictionary<int, ArchivoSeleccionado> archivos)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        if (!InscripcionesAbiertas(c)) throw new InvalidOperationException("La convocatoria no está recibiendo postulaciones");
        var yo = PostulanteActual;
        if (Postulaciones.Any(p => p.PostulanteId == yo.Id && p.ConvocatoriaId == c.Id)) throw new InvalidOperationException("Ya tienes una postulación en esta convocatoria");
        int cmId = _convModalidades.FirstOrDefault(x => x.Convocatoria?.Id == c.Id && x.Modalidad?.Id == modalidadId)?.Id ?? 0;
        int ofertaId = _ofertasDto.FirstOrDefault(x => x.Convocatoria?.Id == c.Id && x.Carrera?.Id == carreraId)?.Id ?? 0;
        if (cmId == 0 || ofertaId == 0 || sedeId <= 0) throw new InvalidOperationException("Selecciona una oferta y modalidad válidas");
        int id = await _api.EnviarPostulacionAsync(new {
            postulacion = new PostulacionDto {
                Postulante = new PostulanteDto { Id = yo.Id }, Convocatoria = new ConvocatoriaDto { Id = c.Id },
                ModalidadElegida = new ConvocatoriaModalidadDto { Id = cmId }, CarreraElegida = new OfertaCarreraDto { Id = ofertaId },
                EstadoActual = new EstadoPostulacionDto { Id = 2 }, CodigoInscripcion = "INS-" + Guid.NewGuid().ToString("N")[..20], SedeId = sedeId
            }, sedeId,
            documentos = archivos.Select(x => new { requisitoAplicableId = x.Key, nombre = x.Value.Nombre, mime = x.Value.Mime, contenidoBase64 = x.Value.ContenidoBase64 }).ToArray()
        });
        await CargarDesdeBackendAsync();
        return Postulaciones.First(p => p.Id == id);
    }

    // ---- RF_13: Seguimiento y Actualización de Estado de Postulación --------
    public void CambiarEstado(Postulacion p, EstadoPostulacion nuevo, string motivo, string responsable)
    {
        if (p.Estado == nuevo) return;
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        int cmId = _convModalidades.FirstOrDefault(x => x.Convocatoria?.Id == p.ConvocatoriaId && x.Modalidad?.Id == p.ModalidadId)?.Id ?? 0;
        int ofertaId = _ofertasDto.FirstOrDefault(o => o.Convocatoria?.Id == p.ConvocatoriaId && o.Carrera?.Id == p.CarreraId)?.Id ?? 0;
        _api.Modificar("PostulacionRS", new PostulacionDto {
            Id = p.Id, Postulante = new PostulanteDto { Id = p.PostulanteId },
            Convocatoria = new ConvocatoriaDto { Id = p.ConvocatoriaId }, ModalidadElegida = new ConvocatoriaModalidadDto { Id = cmId },
            CarreraElegida = new OfertaCarreraDto { Id = ofertaId }, EstadoActual = new EstadoPostulacionDto { Id = MapEstadoPostulacionToDbId(nuevo) },
            CodigoInscripcion = p.Codigo, ObservacionGeneral = motivo
        });
        // El procedimiento registra el historial en la misma transacción.
        p.Estado = nuevo;
        CargarDesdeBackend();
        Avisar();
    }

    // ---- RF_10: Gestión de Observaciones en Documentos de Postulación -------
    public void AprobarDocumento(Documento doc, string responsable)
    {
        doc.Estado = EstadoDocumento.Aprobado;
        doc.Comentario = null;
        if (_api is not null)
        {
            _api.Modificar("DocumentoPostulacionRS", BuildDocumentoDto(doc, "APROBADO", "Documento aprobado"));
        }
        foreach (var o in Observaciones.Where(o => o.DocumentoId == doc.Id && o.Estado == EstadoObservacion.Abierta).ToList())
        {
            SubsanarObservacion(o, "Documento aprobado por el evaluador");
        }
        IniciarRevision(doc.PostulacionId, responsable);
        Avisar();
    }

    public Observacion ObservarDocumento(Documento doc, TipoObservacion tipo, string descripcion, int? evaluadorId, string responsable)
    {
        doc.Estado = EstadoDocumento.Observado;
        doc.Comentario = descripcion;
        int evalId = evaluadorId ?? EvaluadorActualId;
        int obsId = SiguienteId(Observaciones, x => x.Id);

        if (_api is not null)
        {
            _api.Modificar("DocumentoPostulacionRS", BuildDocumentoDto(doc, "OBSERVADO", descripcion));
            var creada = _api.Insertar("DocumentoObservacionRS", new DocumentoObservacionDto
            {
                Documento = new DocumentoPostulacionDto { Id = doc.Id },
                Evaluador = new EvaluadorDto { Id = evalId },
                TipoObservacion = MapTipoObservacionToDb(tipo),
                Descripcion = descripcion,
                FechaObservacion = Hoy,
                EstadoObservacion = "PENDIENTE"
            });
            if (creada is not null) obsId = creada.Id;
        }

        var obs = new Observacion
        {
            Id = obsId,
            DocumentoId = doc.Id,
            EvaluadorId = evalId,
            Tipo = tipo,
            Descripcion = descripcion,
            Fecha = Hoy,
            Estado = EstadoObservacion.Abierta
        };
        Observaciones.Add(obs);
        IniciarRevision(doc.PostulacionId, responsable);
        Avisar();
        return obs;
    }

    public void ModificarObservacion(Observacion obs, TipoObservacion tipo, string descripcion, EstadoObservacion estado, string? comentarioSubsanacion)
    {
        obs.Tipo = tipo;
        obs.Descripcion = descripcion.Trim();
        obs.Estado = estado;
        obs.ComentarioSubsanacion = string.IsNullOrWhiteSpace(comentarioSubsanacion) ? null : comentarioSubsanacion.Trim();
        if (estado == EstadoObservacion.Subsanada && obs.FechaSubsanacion is null)
            obs.FechaSubsanacion = Hoy;

        if (_api is not null)
        {
            _api.Modificar("DocumentoObservacionRS", new DocumentoObservacionDto
            {
                Id = obs.Id,
                Documento = new DocumentoPostulacionDto { Id = obs.DocumentoId },
                Evaluador = new EvaluadorDto { Id = obs.EvaluadorId ?? EvaluadorActualId },
                TipoObservacion = MapTipoObservacionToDb(obs.Tipo),
                Descripcion = obs.Descripcion,
                FechaObservacion = obs.Fecha <= Hoy ? obs.Fecha : Hoy,
                EstadoObservacion = obs.Estado == EstadoObservacion.Subsanada ? "SUBSANADA" : "PENDIENTE",
                FechaSubsanacion = obs.Estado == EstadoObservacion.Subsanada
                    ? (obs.FechaSubsanacion >= obs.Fecha ? obs.FechaSubsanacion : Hoy)
                    : null,
                ComentarioSubsanacion = obs.ComentarioSubsanacion
            });
        }

        if (Documento(obs.DocumentoId) is { } doc)
        {
            if (obs.Estado == EstadoObservacion.Abierta)
            {
                doc.Estado = EstadoDocumento.Observado;
                doc.Comentario = obs.Descripcion;
            }
        }
        Avisar();
    }

    public void SubsanarObservacion(Observacion obs, string comentario = "Subsanado")
    {
        ModificarObservacion(obs, obs.Tipo, obs.Descripcion, EstadoObservacion.Subsanada, comentario);
    }

    public void EliminarObservacion(Observacion obs)
    {
        if (_api is not null)
        {
            _api.Eliminar("DocumentoObservacionRS", obs.Id);
        }
        Observaciones.Remove(obs);
        if (Documento(obs.DocumentoId) is { } doc && !Observaciones.Any(o => o.DocumentoId == doc.Id && o.Estado == EstadoObservacion.Abierta))
        {
            doc.Comentario = null;
            if (doc.Estado == EstadoDocumento.Observado)
            {
                doc.Estado = EstadoDocumento.Pendiente;
                if (_api is not null)
                    _api.Modificar("DocumentoPostulacionRS", BuildDocumentoDto(doc, "PENDIENTE", null));
            }
        }
        Avisar();
    }

    private DocumentoPostulacionDto BuildDocumentoDto(Documento doc, string estadoDb, string? comentario)
    {
        var ext = Path.GetExtension(doc.Archivo).TrimStart('.').ToUpperInvariant();
        var tipoDb = ext is "JPG" or "JPEG" or "PNG" || doc.Tipo is "JPG" or "PNG" or "IMAGEN" ? "IMAGEN" : "PDF";
        return new DocumentoPostulacionDto
        {
            Id = doc.Id,
            Postulacion = new PostulacionDto { Id = doc.PostulacionId },
            RequisitoAplicable = new RequisitoConvocatoriaModalidadDto { Id = doc.RequisitoAplicableId > 0 ? doc.RequisitoAplicableId : 1 },
            NumeroVersion = doc.Version,
            NombreArchivo = doc.Archivo,
            TipoArchivo = tipoDb,
            TamanioArchivo = Math.Max(1024, (long)doc.TamanioKb * 1024),
            RutaArchivo = string.IsNullOrWhiteSpace(doc.RutaArchivo) ? $"/docs/{doc.PostulacionId}/{doc.Archivo}" : doc.RutaArchivo,
            FechaCarga = doc.FechaCarga,
            EstadoDocumento = estadoDb,
            FechaEvaluacion = Hoy,
            ComentarioEvaluacion = comentario
        };
    }

    private void IniciarRevision(int postulacionId, string responsable)
    {
        if (Postulacion(postulacionId) is { Estado: EstadoPostulacion.Enviada } p)
            CambiarEstado(p, EstadoPostulacion.EnRevision, "Inicio de la revisión documental", responsable);
    }

    public void SubirNuevaVersion(Documento doc, string archivo)
    {
        doc.Archivo = archivo;
        doc.Version++;
        doc.FechaCarga = Hoy;
        doc.Estado = EstadoDocumento.Pendiente;
        doc.Comentario = null;

        if (_api is not null)
        {
            _api.Modificar("DocumentoPostulacionRS", BuildDocumentoDto(doc, "PENDIENTE", "Nueva versión cargada por el postulante"));
        }

        foreach (var o in Observaciones.Where(o => o.DocumentoId == doc.Id && o.Estado == EstadoObservacion.Abierta).ToList())
            SubsanarObservacion(o, "Nueva versión cargada por el postulante");

        var p = Postulacion(doc.PostulacionId);
        if (p is { Estado: EstadoPostulacion.Observada } && DocumentosDe(p.Id).All(x => x.Estado != EstadoDocumento.Observado))
            CambiarEstado(p, EstadoPostulacion.EnRevision, "Documentos subsanados por el postulante", PersonaDe(p)?.NombreCompleto ?? Sistema);
        Notificar(RolUsuario.Evaluador, "Documento reemplazado", $"{PersonaDe(p)?.NombreCompleto} cargó una nueva versión de: {doc.Requisito}.", "Documento");
        Avisar();
    }

    public string? Decidir(Postulacion p, EstadoPostulacion decision, string comentario, string responsable)
    {
        if (!p.Estado.EsPendiente()) return "La postulación ya fue resuelta.";
        if (decision == EstadoPostulacion.Apta && MotivoNoApta(p) is { } motivo) return motivo;
        if (decision != EstadoPostulacion.Apta && string.IsNullOrWhiteSpace(comentario))
            return "Escribe un comentario que explique la decisión al postulante.";
        p.Observacion = decision == EstadoPostulacion.Apta ? null : comentario.Trim();
        CambiarEstado(p, decision, decision == EstadoPostulacion.Apta ? "Expediente completo y conforme" : comentario.Trim(), responsable);
        EvaluarEmisionCarne(p);
        return null;
    }

    public void AsignarEvaluador(Postulacion p, int evaluadorId)
    {
        if (_api is null) throw new InvalidOperationException("El servicio no está disponible");
        _api.AsignarEvaluador(p.Id, evaluadorId);
        p.EvaluadorId = evaluadorId;
        Notificar(RolUsuario.Evaluador, "Nueva postulación asignada", $"Se te asignó la postulación {p.Codigo}.", "Postulación");
        Avisar();
    }

    // ---- RF_11 y RF_12: Vouchers de Transferencia y Validación de Pagos -----
    public Voucher RegistrarVoucher(Postulacion p, string codigoOperacion, decimal monto, string archivo)
    {
        int id = SiguienteId(Vouchers, x => x.Id);
        var codigoPago = $"PAG-{p.Id:00}{Random.Shared.Next(100, 999)}";
        if (_api is not null)
        {
            var creado = _api.Insertar("PagoRS", new PagoDto
            {
                Postulacion = new PostulacionDto { Id = p.Id },
                MedioPago = "TRANSFERENCIA",
                Monto = (double)monto,
                FechaGeneracion = Hoy,
                CodigoPago = codigoPago,
                ReferenciaPasarela = codigoOperacion.Trim(),
                EstadoPago = "PENDIENTE",
                RutaVoucher = $"/vouchers/{archivo.Trim()}"
            });
            if (creado is not null)
            {
                id = creado.Id;
                codigoPago = creado.CodigoPago;
            }
        }

        var v = new Voucher
        {
            Id = id,
            PostulacionId = p.Id,
            CodigoOperacion = codigoOperacion.Trim(),
            Monto = monto,
            Archivo = archivo.Trim(),
            FechaRegistro = Hoy,
            Estado = EstadoVoucher.Pendiente
        };
        Vouchers.Add(v);

        if (PagoDe(p.Id) is { } pago && pago.Estado != EstadoPago.Aprobado)
        {
            pago.Id = id;
            pago.Codigo = codigoPago;
            pago.Monto = monto;
            pago.Estado = EstadoPago.Pendiente;
        }
        Notificar(RolUsuario.Administrador, "Voucher por validar", $"Se registró un voucher de {Formato.Soles(monto)} para {p.Codigo}.", "Pago");
        Avisar();
        return v;
    }

    public void ModificarVoucher(Voucher v, string nuevoCodigoOperacion, string nuevoArchivo)
    {
        if (v.Estado != EstadoVoucher.Pendiente) return;
        v.CodigoOperacion = nuevoCodigoOperacion.Trim();
        v.Archivo = nuevoArchivo.Trim();
        if (_api is not null)
        {
            _api.Modificar("PagoRS", new PagoDto
            {
                Id = v.Id,
                Postulacion = new PostulacionDto { Id = v.PostulacionId },
                MedioPago = "TRANSFERENCIA",
                Monto = (double)v.Monto,
                CodigoPago = PagoDe(v.PostulacionId)?.Codigo ?? $"PAG-{v.Id:0000}",
                ReferenciaPasarela = v.CodigoOperacion,
                EstadoPago = "PENDIENTE",
                RutaVoucher = $"/vouchers/{v.Archivo}"
            });
        }
        Avisar();
    }

    public void RetirarVoucher(Voucher v)
    {
        if (_api is not null)
        {
            _api.Eliminar("PagoRS", v.Id);
        }
        Vouchers.Remove(v);
        if (PagoDe(v.PostulacionId) is { Estado: EstadoPago.Pendiente } pago && !VouchersDe(v.PostulacionId).Any(x => x.Estado == EstadoVoucher.Pendiente))
            pago.Estado = EstadoPago.Generado;
        Avisar();
    }

    public void AprobarVoucher(Voucher v, string responsable)
    {
        v.Estado = EstadoVoucher.Aprobado;
        v.FechaRevision = Hoy;
        v.MotivoRechazo = null;
        if (_api is not null)
        {
            _api.Modificar("PagoRS", new PagoDto
            {
                Id = v.Id,
                Postulacion = new PostulacionDto { Id = v.PostulacionId },
                MedioPago = "TRANSFERENCIA",
                Monto = (double)v.Monto,
                CodigoPago = PagoDe(v.PostulacionId)?.Codigo ?? $"PAG-{v.Id:0000}",
                ReferenciaPasarela = v.CodigoOperacion,
                EstadoPago = "APROBADO",
                RutaVoucher = $"/vouchers/{v.Archivo}"
            });
        }
        if (PagoDe(v.PostulacionId) is { } pago)
        {
            pago.Estado = EstadoPago.Aprobado;
            pago.FechaAprobacion = Hoy;
        }
        var p = Postulacion(v.PostulacionId);
        if (p is not null)
        {
            Notificar(RolUsuario.Postulante, "Pago validado", $"Tu pago de {Formato.Soles(v.Monto)} fue validado.", "Pago", p.PostulanteId);
            EvaluarEmisionCarne(p);
        }
        Avisar();
    }

    public void RechazarVoucher(Voucher v, string motivo)
    {
        v.Estado = EstadoVoucher.Rechazado;
        v.FechaRevision = Hoy;
        v.MotivoRechazo = motivo.Trim();
        if (_api is not null)
        {
            _api.Modificar("PagoRS", new PagoDto
            {
                Id = v.Id,
                Postulacion = new PostulacionDto { Id = v.PostulacionId },
                MedioPago = "TRANSFERENCIA",
                Monto = (double)v.Monto,
                CodigoPago = PagoDe(v.PostulacionId)?.Codigo ?? $"PAG-{v.Id:0000}",
                ReferenciaPasarela = v.CodigoOperacion,
                EstadoPago = "RECHAZADO",
                RutaVoucher = $"/vouchers/{v.Archivo}"
            });
        }
        if (PagoDe(v.PostulacionId) is { } pago && pago.Estado != EstadoPago.Aprobado) pago.Estado = EstadoPago.Rechazado;
        if (Postulacion(v.PostulacionId) is { } p)
            Notificar(RolUsuario.Postulante, "Voucher rechazado", $"Motivo: {motivo.Trim()} Adjunta un nuevo voucher.", "Pago", p.PostulanteId);
        Avisar();
    }

    // ---- RF_14: Carné Digital de Postulante ---------------------------------
    public void EvaluarEmisionCarne(Postulacion p)
    {
        if (AptaSinCarne(p)) EmitirCarne(p, automatico: true, motivo: null);
    }

    public Carne EmitirCarne(Postulacion p, bool automatico, string? motivo, int? sedeId = null, string? aula = null)
    {
        int id = SiguienteId(Carnes, x => x.Id);
        var periodo = Convocatoria(p.ConvocatoriaId)?.Periodo ?? Hoy.Year.ToString();
        var codigo = $"CAR-{periodo}-{id:0000}";
        var sedeFinal = sedeId ?? p.SedeId;
        var aulaFinal = aula ?? $"A-{101 + p.Id % 12}";
        if (aulaFinal.Length > 20) aulaFinal = aulaFinal[..20];

        if (_api is not null)
        {
            // Como postulacion_id es UNIQUE en carne_postulante, si ya existía un carné en BD para esta postulación lo actualizamos
            var existenteBd = Carnes.FirstOrDefault(x => x.PostulacionId == p.Id);
            if (existenteBd is not null && existenteBd.Id > 0)
            {
                _api.Modificar("CarnePostulanteRS", new CarnePostulanteDto
                {
                    Id = existenteBd.Id,
                    Postulacion = new PostulacionDto { Id = p.Id },
                    Sede = new SedeDto { Id = sedeFinal },
                    CodigoCarne = codigo,
                    FechaGeneracion = Hoy,
                    FechaInicioVigencia = Hoy,
                    FechaFinVigencia = Hoy.AddMonths(3),
                    AulaExamen = aulaFinal
                });
                id = existenteBd.Id;
            }
            else
            {
                var creado = _api.Insertar("CarnePostulanteRS", new CarnePostulanteDto
                {
                    Postulacion = new PostulacionDto { Id = p.Id },
                    Sede = new SedeDto { Id = sedeFinal },
                    CodigoCarne = codigo,
                    FechaGeneracion = Hoy,
                    FechaInicioVigencia = Hoy,
                    FechaFinVigencia = Hoy.AddMonths(3),
                    AulaExamen = aulaFinal
                });
                if (creado is not null) id = creado.Id;
            }
        }

        var carne = new Carne
        {
            Id = id,
            PostulacionId = p.Id,
            Codigo = codigo,
            SedeId = sedeFinal,
            Aula = aulaFinal,
            FechaEmision = Hoy,
            InicioVigencia = Hoy,
            FinVigencia = Hoy.AddMonths(3),
            Automatico = automatico,
            Motivo = motivo
        };
        Carnes.Add(carne);
        Notificar(RolUsuario.Postulante, "Tu carné de postulante está disponible", $"Carné {carne.Codigo}: preséntalo con tu documento el día de la evaluación.", "Postulación", p.PostulanteId);
        Avisar();
        return carne;
    }

    public void AnularCarne(Carne c, string motivo)
    {
        c.Estado = EstadoCarne.Anulado;
        c.Motivo = motivo.Trim();
        if (_api is not null && c.Id > 0 && !Carnes.Any(x => x.PostulacionId == c.PostulacionId && x.Id != c.Id && x.Estado == EstadoCarne.Vigente))
        {
            _api.Modificar("CarnePostulanteRS", new CarnePostulanteDto
            {
                Id = c.Id,
                Postulacion = new PostulacionDto { Id = c.PostulacionId },
                Sede = new SedeDto { Id = c.SedeId },
                CodigoCarne = c.Codigo,
                FechaGeneracion = c.FechaEmision,
                FechaInicioVigencia = c.InicioVigencia,
                FechaFinVigencia = c.FinVigencia,
                AulaExamen = "ANULADO"
            });
        }
        Avisar();
    }

    public Carne? ReemitirCarne(Carne c, int sedeId, string aula, string motivo)
    {
        var p = Postulacion(c.PostulacionId);
        if (p is null) return null;
        c.Estado = EstadoCarne.Anulado;
        c.Motivo = $"Reemitido: {motivo.Trim()}";
        return EmitirCarne(p, automatico: false, motivo: motivo.Trim(), sedeId: sedeId, aula: aula.Trim());
    }

    // ---- Notificaciones ------------------------------------------------------
    public IEnumerable<Notificacion> NotificacionesDe(RolUsuario rol) => Notificaciones.Where(n => n.Rol == rol).OrderByDescending(n => n.Fecha);
    public int SinLeer(RolUsuario rol) => Notificaciones.Count(n => n.Rol == rol && !n.Leida);

    public void MarcarLeidas(RolUsuario rol)
    {
        foreach (var n in Notificaciones.Where(n => n.Rol == rol)) n.Leida = true;
        Avisar();
    }

    public void Notificar(RolUsuario rol, string titulo, string detalle, string tipo, int? postulanteId = null)
    {
        if (rol == RolUsuario.Postulante && postulanteId is not null && postulanteId != PostulanteActualId) return;
        Notificaciones.Add(new Notificacion
        {
            Id = SiguienteId(Notificaciones, x => x.Id),
            Rol = rol,
            Titulo = titulo,
            Detalle = detalle,
            Tipo = tipo,
            Medio = "Esta sesión",
            Fecha = TimeZoneInfo.ConvertTimeBySystemTimeZoneId(DateTimeOffset.UtcNow, "America/Lima").DateTime
        });
    }

    // ---- Utilidades -------------------------------------------------------
    public static int SiguienteId<T>(IEnumerable<T> elementos, Func<T, int> id) => elementos.Select(id).DefaultIfEmpty().Max() + 1;

    public void Avisar() => Cambio?.Invoke();
}
