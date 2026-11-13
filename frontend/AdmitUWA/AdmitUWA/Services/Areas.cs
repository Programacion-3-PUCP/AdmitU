using AdmitUWA.Models;

namespace AdmitUWA.Services;

public sealed record NavItem(string Texto, string Href, string Icono, bool Exacto = false, int Conteo = 0, bool Alerta = false);

public sealed record NavGrupo(string Titulo, IReadOnlyList<NavItem> Items);

// Identidad, menú y buscador de cada área (rol). Los contadores salen de la sesión.
public sealed record AreaInfo(
    string Clave,
    RolUsuario Rol,
    string Nombre,
    string Inicio,
    string Usuario,
    string Cargo,
    string? RutaBusqueda,
    string? MarcadorBusqueda,
    int SinLeer,
    IReadOnlyList<NavGrupo> Menu);

public static class Areas
{
    public static AreaInfo Crear(string clave, DatosSesion datos)
    {
        if (datos.ErrorCarga is not null)
            return new(clave, clave == "evaluador" ? RolUsuario.Evaluador : clave == "postulante" ? RolUsuario.Postulante : RolUsuario.Administrador,
                "AdmitU", "/", "Sesión iniciada", "Datos no disponibles", null, null, 0, []);
        return clave switch { "evaluador" => Evaluador(datos), "postulante" => Postulante(datos), _ => Administrador(datos) };
    }

    private static AreaInfo Administrador(DatosSesion datos)
    {
        int conv = datos.ConvocatoriaPorDefecto;
        var postulaciones = datos.PostulacionesDeConvocatoria(conv).ToList();
        int porRevisar = postulaciones.Count(p => p.Estado.EsPendiente());
        int aptosSinCarne = postulaciones.Count(datos.AptaSinCarne);
        int sinLeer = datos.SinLeer(RolUsuario.Administrador);
        return new("admin", RolUsuario.Administrador, "Administrador", "/admin", datos.NombreAdministrador, "Oficina de admisión",
            "/admin/postulantes", "Buscar postulantes por nombre, correo o documento", sinLeer,
        [
            new("Seguimiento",
            [
                new("Resumen", "/admin", "dashboard", Exacto: true),
                new("Postulaciones", "/admin/postulaciones", "file-text", Conteo: porRevisar),
                new("Documentos", "/admin/documentos", "folder"),
                new("Pagos", "/admin/pagos", "credit-card"),
                new("Carnés", "/admin/carnes", "id-card", Conteo: aptosSinCarne, Alerta: aptosSinCarne > 0),
                new("Historial de estados", "/admin/historial-estados", "clock")
            ]),
            new("Proceso de admisión",
            [
                new("Convocatorias", "/admin/convocatorias", "calendar"),
                new("Requisitos documentales", "/admin/requisitos", "clipboard"),
                new("Postulantes", "/admin/postulantes", "users"),
                new("Evaluadores", "/admin/evaluadores", "user-check")
            ]),
            new("Catálogos",
            [
                new("Facultades", "/admin/facultades", "landmark"),
                new("Carreras", "/admin/carreras", "book-open"),
                new("Modalidades", "/admin/modalidades", "list-checks"),
                new("Sedes", "/admin/sedes", "map-pin")
            ]),
            new("Reportes",
            [
                new("Ingresos por vouchers", "/admin/reportes/ingresos", "wallet"),
                new("Vacantes y postulaciones", "/admin/reportes/vacantes", "bar-chart")
            ]),
            new("Comunicación",
            [
                new("Notificaciones", "/admin/notificaciones", "bell", Conteo: sinLeer, Alerta: true)
            ])
        ]);
    }

    private static AreaInfo Postulante(DatosSesion datos)
    {
        var yo = datos.PostulanteActual;
        int sinLeer = datos.SinLeer(RolUsuario.Postulante);
        var conv = datos.ConvocatoriaVigente;
        return new("postulante", RolUsuario.Postulante, "Postulante", "/postulante", yo.NombreCompleto,
            conv is null ? "Postulante" : $"Postulante · {conv.Nombre}", null, null, sinLeer,
        [
            new("Mi proceso",
            [
                new("Inicio", "/postulante", "dashboard", Exacto: true),
                new("Mi postulación", "/postulante/mi-postulacion", "file-text"),
                new("Mis vouchers", "/postulante/vouchers", "credit-card"),
                new("Documentos personales", "/postulante/documentos-personales", "id-card"),
                new("Antecedentes académicos", "/postulante/antecedentes", "graduation-cap"),
                new("Convocatorias", "/postulante/convocatorias", "calendar")
            ]),
            new("Mi cuenta",
            [
                new("Mi perfil", "/postulante/perfil", "user"),
                new("Notificaciones", "/postulante/notificaciones", "bell", Conteo: sinLeer, Alerta: true)
            ])
        ]);
    }

    private static AreaInfo Evaluador(DatosSesion datos)
    {
        var yo = datos.EvaluadorActual;
        int pendientes = datos.Postulaciones.Count(p => p.EvaluadorId == yo.Id && p.Estado.EsPendiente());
        int vouchers = datos.Vouchers.Count(v => v.Estado == EstadoVoucher.Pendiente);
        int sinLeer = datos.SinLeer(RolUsuario.Evaluador);
        return new("evaluador", RolUsuario.Evaluador, "Evaluador", "/evaluador", yo.Nombre, $"Evaluador · {yo.Especialidad}",
            "/evaluador/postulaciones", "Buscar por código, postulante o carrera", sinLeer,
        [
            new("Evaluación",
            [
                new("Resumen", "/evaluador", "dashboard", Exacto: true),
                new("Mi bandeja", "/evaluador/postulaciones", "inbox", Conteo: pendientes),
                new("Validar vouchers", "/evaluador/vouchers", "credit-card", Conteo: vouchers),
                new("Observaciones", "/evaluador/observaciones", "alert"),
                new("Historial", "/evaluador/historial", "check-circle"),
                new("Reporte de observados", "/evaluador/reportes/observados", "bar-chart")
            ]),
            new("Comunicación",
            [
                new("Notificaciones", "/evaluador/notificaciones", "bell", Conteo: sinLeer, Alerta: true)
            ])
        ]);
    }
}
