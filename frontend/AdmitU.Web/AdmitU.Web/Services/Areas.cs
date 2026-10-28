using AdmitU.Web.Data;
using AdmitU.Web.Models;

namespace AdmitU.Web.Services;

public sealed record NavItem(string Texto, string Href, string Icono, bool Exacto = false, int Conteo = 0, bool Alerta = false);

public sealed record NavGrupo(string Titulo, IReadOnlyList<NavItem> Items);

// Datos de cada área (rol) de la aplicación: identidad, menú y buscador.
public sealed record AreaInfo(
    string Clave,
    string Nombre,
    string Inicio,
    string Usuario,
    string Cargo,
    string TarjetaTitulo,
    string TarjetaTexto,
    string? RutaBusqueda,
    string? MarcadorBusqueda,
    IReadOnlyList<NavGrupo> Menu);

public static class Areas
{
    public static AreaInfo Administrador
    {
        get
        {
            int revision = DatosDemo.Postulaciones.Count(p => p.Estado == EstadoPostulacion.EnRevision);
            int sinLeer = DatosDemo.Notificaciones.Count(n => n.Rol == "Administrador" && !n.Leida);
            return new("admin", "Administrador", "/admin", "Admin Admisión", "Oficina de admisión",
                "Admisión 2027-I", "Inscripciones abiertas hasta el 30 de noviembre.",
                "/admin/postulantes", "Buscar postulantes por nombre, correo o documento",
            [
                new("Gestión",
                [
                    new("Resumen", "/admin", "dashboard", Exacto: true),
                    new("Postulantes", "/admin/postulantes", "users"),
                    new("Postulaciones", "/admin/postulaciones", "file-text", Conteo: revision),
                    new("Pagos", "/admin/pagos", "credit-card"),
                    new("Documentos", "/admin/documentos", "folder"),
                    new("Carnés", "/admin/carnes", "id-card")
                ]),
                new("Proceso de admisión",
                [
                    new("Convocatorias", "/admin/convocatorias", "calendar"),
                    new("Modalidades", "/admin/modalidades", "list-checks"),
                    new("Requisitos", "/admin/requisitos", "paperclip"),
                    new("Evaluadores", "/admin/evaluadores", "user-check")
                ]),
                new("Catálogos",
                [
                    new("Facultades", "/admin/facultades", "landmark"),
                    new("Carreras", "/admin/carreras", "book-open"),
                    new("Sedes", "/admin/sedes", "map-pin")
                ]),
                new("Reportes",
                [
                    new("Ingresos por vouchers", "/admin/reportes/ingresos", "wallet"),
                    new("Expedientes observados", "/admin/reportes/observados", "alert"),
                    new("Vacantes", "/admin/reportes/vacantes", "trending-up")
                ]),
                new("Comunicación",
                [
                    new("Notificaciones", "/admin/notificaciones", "bell", Conteo: sinLeer, Alerta: true)
                ])
            ]);
        }
    }

    public static AreaInfo Postulante
    {
        get
        {
            var yo = DatosDemo.Postulante(DatosDemo.PostulanteActualId)!;
            var mia = DatosDemo.Postulaciones.FirstOrDefault(p => p.PostulanteId == yo.Id);
            int sinLeer = DatosDemo.Notificaciones.Count(n => n.Rol == "Postulante" && !n.Leida);
            return new("postulante", "Postulante", "/postulante", yo.NombreCompleto, "Postulante · Admisión 2027-I",
                "Tu postulación", mia is null ? "Aún no has iniciado tu postulación." : $"Estado actual: {mia.Estado.Etiqueta()}.",
                null, null,
            [
                new("Mi proceso",
                [
                    new("Inicio", "/postulante", "dashboard", Exacto: true),
                    new("Mi postulación", "/postulante/mi-postulacion", "file-text"),
                    new("Mis documentos", "/postulante/documentos", "folder"),
                    new("Mi pago", "/postulante/pago", "credit-card"),
                    new("Mi carné", "/postulante/carne", "id-card"),
                    new("Convocatorias", "/postulante/convocatorias", "calendar")
                ]),
                new("Mi cuenta",
                [
                    new("Mi perfil", "/postulante/perfil", "users"),
                    new("Antecedentes académicos", "/postulante/antecedentes", "graduation-cap"),
                    new("Notificaciones", "/postulante/notificaciones", "bell", Conteo: sinLeer, Alerta: true)
                ])
            ]);
        }
    }

    public static AreaInfo Evaluador
    {
        get
        {
            var yo = DatosDemo.Evaluadores.First(e => e.Id == DatosDemo.EvaluadorActualId);
            int pendientes = DatosDemo.Postulaciones.Count(p => p.EvaluadorId == yo.Id && p.Estado is EstadoPostulacion.Enviada or EstadoPostulacion.EnRevision);
            int sinLeer = DatosDemo.Notificaciones.Count(n => n.Rol == "Evaluador" && !n.Leida);
            var mias = DatosDemo.Postulaciones.Where(p => p.EvaluadorId == yo.Id).Select(p => p.Id).ToHashSet();
            int vouchers = DatosDemo.Pagos.Count(x => x.Voucher is not null && x.Estado == EstadoPago.Pendiente && mias.Contains(x.PostulacionId));
            return new("evaluador", "Evaluador", "/evaluador", yo.Nombre, $"Evaluador · {yo.Especialidad}",
                "Pendientes de revisión", $"Tienes {pendientes} postulaciones por evaluar.",
                "/evaluador/postulaciones", "Buscar por código, postulante o carrera",
            [
                new("Evaluación",
                [
                    new("Resumen", "/evaluador", "dashboard", Exacto: true),
                    new("Mis postulaciones", "/evaluador/postulaciones", "file-text", Conteo: pendientes),
                    new("Vouchers de pago", "/evaluador/vouchers", "credit-card", Conteo: vouchers),
                    new("Historial", "/evaluador/historial", "check-circle")
                ]),
                new("Comunicación",
                [
                    new("Notificaciones", "/evaluador/notificaciones", "bell", Conteo: sinLeer, Alerta: true)
                ])
            ]);
        }
    }
}
