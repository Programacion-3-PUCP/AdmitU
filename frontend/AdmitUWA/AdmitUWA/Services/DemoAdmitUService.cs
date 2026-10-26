using AdmitUWA.Models;

namespace AdmitUWA.Services;

// El catálogo de ejemplo es temporal y solo vive durante la sesión interactiva.
public sealed class DemoAdmitUService : IAdmitUService
{
    private readonly List<FacultadDto> facultades =
    [
        new() { Id = 1, Codigo = "ING", Nombre = "Facultad de Ingeniería" },
        new() { Id = 2, Codigo = "CS", Nombre = "Facultad de Ciencias" },
        new() { Id = 3, Codigo = "HUM", Nombre = "Facultad de Humanidades" }
    ];

    private readonly List<CarreraDto> carreras =
    [
        new() { Id = 1, FacultadId = 1, CodigoCarrera = "SIS", Nombre = "Ingeniería de Sistemas" },
        new() { Id = 2, FacultadId = 1, CodigoCarrera = "IND", Nombre = "Ingeniería Industrial" },
        new() { Id = 3, FacultadId = 2, CodigoCarrera = "MAT", Nombre = "Matemáticas" },
        new() { Id = 4, FacultadId = 3, CodigoCarrera = "HIS", Nombre = "Historia" }
    ];

    private readonly List<PostulanteDto> postulantes =
    [
        new() { Id = 1, Nombres = "Andrea", ApellidoPaterno = "Salas", ApellidoMaterno = "Ríos", TipoDocumento = "DNI", NumeroDocumento = "74216850", Correo = "andrea.salas@example.com", Telefono = "987654321", FechaNacimiento = new DateOnly(2007, 8, 14) },
        new() { Id = 2, Nombres = "Marco", ApellidoPaterno = "Paredes", ApellidoMaterno = "León", TipoDocumento = "DNI", NumeroDocumento = "71928463", Correo = "marco.paredes@example.com", Telefono = "945123678", FechaNacimiento = new DateOnly(2006, 3, 25) },
        new() { Id = 3, Nombres = "Valeria", ApellidoPaterno = "Mendoza", ApellidoMaterno = "Chávez", TipoDocumento = "DNI", NumeroDocumento = "76543210", Correo = "valeria.mendoza@example.com", FechaNacimiento = new DateOnly(2007, 11, 9) },
        new() { Id = 4, Nombres = "Luis", ApellidoPaterno = "Vargas", ApellidoMaterno = "Campos", TipoDocumento = "DNI", NumeroDocumento = "70819263", Correo = "luis.vargas@example.com", FechaNacimiento = new DateOnly(2007, 1, 2) }
    ];

    private readonly List<ConvocatoriaDto> convocatorias =
    [
        new() { Id = 1, CodigoConvocatoria = "ADM-2027-1", Nombre = "Admisión 2027-I", Periodo = "2027-I", FechaInicio = new DateOnly(2026, 10, 1), FechaFin = new DateOnly(2026, 12, 15), Estado = "PUBLICADA", Descripcion = "Proceso de admisión ordinario" },
        new() { Id = 2, CodigoConvocatoria = "ADM-2027-2", Nombre = "Admisión 2027-II", Periodo = "2027-II", FechaInicio = new DateOnly(2027, 3, 1), FechaFin = new DateOnly(2027, 5, 15), Estado = "BORRADOR" }
    ];

    private readonly List<ModalidadDto> modalidades =
    [
        new() { Id = 1, CodigoModalidad = "ORD", Nombre = "Evaluación ordinaria", Descripcion = "Modalidad general de admisión", RequiereColegio = true },
        new() { Id = 2, CodigoModalidad = "TRAS", Nombre = "Traslado externo", Descripcion = "Postulantes procedentes de otra universidad", RequiereUniversidad = true },
        new() { Id = 3, CodigoModalidad = "PRIM", Nombre = "Primeros puestos", RequiereColegio = true }
    ];

    private readonly List<PostulacionDto> postulaciones =
    [
        new() { Id = 1, PostulanteId = 1, ConvocatoriaId = 1, ConvocatoriaModalidadId = 1, OfertaCarreraId = 1, EstadoActualId = 2, EstadoActualNombre = "En revisión", CodigoInscripcion = "POST-001", FechaRegistro = new DateOnly(2026, 10, 4) },
        new() { Id = 2, PostulanteId = 2, ConvocatoriaId = 1, ConvocatoriaModalidadId = 1, OfertaCarreraId = 2, EstadoActualId = 1, EstadoActualNombre = "Registrada", CodigoInscripcion = "POST-002", FechaRegistro = new DateOnly(2026, 10, 5) },
        new() { Id = 3, PostulanteId = 3, ConvocatoriaId = 1, ConvocatoriaModalidadId = 3, OfertaCarreraId = 3, EstadoActualId = 2, EstadoActualNombre = "En revisión", CodigoInscripcion = "POST-003", FechaRegistro = new DateOnly(2026, 10, 6) }
    ];

    private readonly List<PagoDto> pagos =
    [
        new() { Id = 1, PostulacionId = 1, MedioPago = "TRANSFERENCIA", Monto = 260m, CodigoPago = "PAGO-001", FechaGeneracion = new DateOnly(2026, 10, 4), FechaPago = new DateOnly(2026, 10, 5), EstadoPago = "APROBADO" },
        new() { Id = 2, PostulacionId = 2, MedioPago = "TARJETA", Monto = 260m, CodigoPago = "PAGO-002", FechaGeneracion = new DateOnly(2026, 10, 5), EstadoPago = "PENDIENTE" },
        new() { Id = 3, PostulacionId = 3, MedioPago = "TRANSFERENCIA", Monto = 260m, CodigoPago = "PAGO-003", FechaGeneracion = new DateOnly(2026, 10, 6), EstadoPago = "GENERADO" }
    ];

    private readonly List<DocumentoPostulacionDto> documentos =
    [
        new() { Id = 1, PostulacionId = 1, RequisitoAplicableId = 1, NumeroVersion = 1, NombreArchivo = "constancia-estudios.pdf", TipoArchivo = "PDF", TamanioArchivo = 102400, FechaCarga = new DateOnly(2026, 10, 4), EstadoDocumento = "PENDIENTE" },
        new() { Id = 2, PostulacionId = 3, RequisitoAplicableId = 2, NumeroVersion = 1, NombreArchivo = "identificacion.pdf", TipoArchivo = "PDF", TamanioArchivo = 82432, FechaCarga = new DateOnly(2026, 10, 6), EstadoDocumento = "PENDIENTE" }
    ];

    private static int SiguienteId<T>(IEnumerable<T> datos, Func<T, int> id) => datos.Any() ? datos.Max(id) + 1 : 1;

    public Task<List<FacultadDto>> ListarFacultadesAsync() => Task.FromResult(facultades.Select(x => x with { }).ToList());
    public Task<FacultadDto?> ObtenerFacultadAsync(int id) => Task.FromResult(facultades.Find(x => x.Id == id) is { } f ? f with { } : null);
    public Task GuardarFacultadAsync(FacultadDto modelo)
    {
        if (facultades.Any(x => x.Codigo.Equals(modelo.Codigo, StringComparison.OrdinalIgnoreCase) && x.Id != modelo.Id))
            throw new InvalidOperationException("Ya existe una facultad con ese código.");
        if (modelo.Id == 0)
            facultades.Add(modelo with { Id = SiguienteId(facultades, x => x.Id) });
        else
        {
            var i = facultades.FindIndex(x => x.Id == modelo.Id);
            if (i < 0) throw new InvalidOperationException("La facultad no existe.");
            facultades[i] = modelo with { };
        }
        return Task.CompletedTask;
    }
    public Task DesactivarFacultadAsync(int id)
    {
        var dato = facultades.Find(x => x.Id == id) ?? throw new InvalidOperationException("La facultad no existe.");
        facultades[facultades.IndexOf(dato)] = dato with { Activo = false };
        return Task.CompletedTask;
    }

    public Task<List<CarreraDto>> ListarCarrerasAsync() => Task.FromResult(carreras.Select(x => x with { }).ToList());
    public Task<CarreraDto?> ObtenerCarreraAsync(int id) => Task.FromResult(carreras.Find(x => x.Id == id) is { } c ? c with { } : null);
    public Task GuardarCarreraAsync(CarreraDto modelo)
    {
        if (!facultades.Any(x => x.Id == modelo.FacultadId && x.Activo))
            throw new InvalidOperationException("La facultad seleccionada no está activa.");
        if (carreras.Any(x => x.CodigoCarrera.Equals(modelo.CodigoCarrera, StringComparison.OrdinalIgnoreCase) && x.Id != modelo.Id))
            throw new InvalidOperationException("Ya existe una carrera con ese código.");
        if (modelo.Id == 0)
            carreras.Add(modelo with { Id = SiguienteId(carreras, x => x.Id) });
        else
        {
            var i = carreras.FindIndex(x => x.Id == modelo.Id);
            if (i < 0) throw new InvalidOperationException("La carrera no existe.");
            carreras[i] = modelo with { };
        }
        return Task.CompletedTask;
    }
    public Task DesactivarCarreraAsync(int id)
    {
        var dato = carreras.Find(x => x.Id == id) ?? throw new InvalidOperationException("La carrera no existe.");
        carreras[carreras.IndexOf(dato)] = dato with { Activo = false };
        return Task.CompletedTask;
    }

    public Task<List<PostulanteDto>> ListarPostulantesAsync() => Task.FromResult(postulantes.Select(x => x with { }).ToList());
    public Task<PostulanteDto?> ObtenerPostulanteAsync(int id) => Task.FromResult(postulantes.Find(x => x.Id == id) is { } p ? p with { } : null);
    public Task GuardarPostulanteAsync(PostulanteDto modelo)
    {
        if (postulantes.Any(x => x.NumeroDocumento == modelo.NumeroDocumento && x.TipoDocumento == modelo.TipoDocumento && x.Id != modelo.Id))
            throw new InvalidOperationException("Ya existe un postulante con ese documento.");
        if (modelo.Id == 0)
            postulantes.Add(modelo with { Id = SiguienteId(postulantes, x => x.Id) });
        else
        {
            var i = postulantes.FindIndex(x => x.Id == modelo.Id);
            if (i < 0) throw new InvalidOperationException("El postulante no existe.");
            postulantes[i] = modelo with { };
        }
        return Task.CompletedTask;
    }
    public Task EliminarPostulanteAsync(int id)
    {
        if (postulaciones.Any(x => x.PostulanteId == id))
            throw new InvalidOperationException("No se puede eliminar un postulante con postulaciones asociadas.");
        postulantes.RemoveAll(x => x.Id == id);
        return Task.CompletedTask;
    }

    public Task<List<ConvocatoriaDto>> ListarConvocatoriasAsync() => Task.FromResult(convocatorias.Select(x => x with { }).ToList());
    public Task<ConvocatoriaDto?> ObtenerConvocatoriaAsync(int id) => Task.FromResult(convocatorias.Find(x => x.Id == id) is { } c ? c with { } : null);
    public Task GuardarConvocatoriaAsync(ConvocatoriaDto modelo)
    {
        if (convocatorias.Any(x => x.CodigoConvocatoria.Equals(modelo.CodigoConvocatoria, StringComparison.OrdinalIgnoreCase) && x.Id != modelo.Id))
            throw new InvalidOperationException("Ya existe una convocatoria con ese código.");
        if (modelo.Id == 0)
            convocatorias.Add(modelo with { Id = SiguienteId(convocatorias, x => x.Id) });
        else
        {
            var i = convocatorias.FindIndex(x => x.Id == modelo.Id);
            if (i < 0) throw new InvalidOperationException("La convocatoria no existe.");
            convocatorias[i] = modelo with { };
        }
        return Task.CompletedTask;
    }
    public Task DesactivarConvocatoriaAsync(int id)
    {
        var dato = convocatorias.Find(x => x.Id == id) ?? throw new InvalidOperationException("La convocatoria no existe.");
        convocatorias[convocatorias.IndexOf(dato)] = dato with { Activo = false };
        return Task.CompletedTask;
    }

    public Task<List<ModalidadDto>> ListarModalidadesAsync() => Task.FromResult(modalidades.Select(x => x with { }).ToList());
    public Task<ModalidadDto?> ObtenerModalidadAsync(int id) => Task.FromResult(modalidades.Find(x => x.Id == id) is { } m ? m with { } : null);
    public Task GuardarModalidadAsync(ModalidadDto modelo)
    {
        if (modalidades.Any(x => x.CodigoModalidad.Equals(modelo.CodigoModalidad, StringComparison.OrdinalIgnoreCase) && x.Id != modelo.Id))
            throw new InvalidOperationException("Ya existe una modalidad con ese código.");
        if (modelo.Id == 0)
            modalidades.Add(modelo with { Id = SiguienteId(modalidades, x => x.Id) });
        else
        {
            var i = modalidades.FindIndex(x => x.Id == modelo.Id);
            if (i < 0) throw new InvalidOperationException("La modalidad no existe.");
            modalidades[i] = modelo with { };
        }
        return Task.CompletedTask;
    }
    public Task DesactivarModalidadAsync(int id)
    {
        var dato = modalidades.Find(x => x.Id == id) ?? throw new InvalidOperationException("La modalidad no existe.");
        modalidades[modalidades.IndexOf(dato)] = dato with { Activo = false };
        return Task.CompletedTask;
    }

    public Task<List<PostulacionDto>> ListarPostulacionesAsync() => Task.FromResult(postulaciones.Select(x => x with { }).ToList());
    public Task<List<PagoDto>> ListarPagosAsync() => Task.FromResult(pagos.Select(x => x with { }).ToList());
    public Task AprobarPagoAsync(int id)
    {
        var pago = pagos.Find(x => x.Id == id) ?? throw new InvalidOperationException("El pago no existe.");
        if (pago.EstadoPago != "PENDIENTE" && pago.EstadoPago != "GENERADO")
            throw new InvalidOperationException("Solo se puede aprobar un pago generado o pendiente.");
        pagos[pagos.IndexOf(pago)] = pago with
        {
            EstadoPago = "APROBADO",
            FechaPago = DateOnly.FromDateTime(DateTime.Today)
        };
        return Task.CompletedTask;
    }
    public Task<List<DocumentoPostulacionDto>> ListarDocumentosAsync() => Task.FromResult(documentos.Select(x => x with { }).ToList());
}
