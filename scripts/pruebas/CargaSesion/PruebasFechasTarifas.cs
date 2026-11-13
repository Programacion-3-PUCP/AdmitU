using System.Net;
using System.Security.Claims;
using System.Text;
using AdmitUWA.Models;
using AdmitUWA.Services;
using AdmitUWA.Services.REST;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Http;

public static class PruebasFechasTarifas
{
    public static async Task EjecutarAsync(Action<bool, string> verificar)
    {
        var cierre = new DateOnly(2026, 11, 13);
        var convocatoria = new Convocatoria { Inicio = cierre.AddDays(-20), Fin = cierre, Estado = EstadoConvocatoria.Publicada };
        verificar(DatosSesion.InscripcionesAbiertas(convocatoria, cierre.AddDays(-1)), "H02: inscripción antes del cierre");
        verificar(DatosSesion.InscripcionesAbiertas(convocatoria, cierre), "H02: cierre inclusivo el día límite de Lima");
        verificar(!DatosSesion.InscripcionesAbiertas(convocatoria, cierre.AddDays(1)), "H02: inscripción rechazada después del cierre");
        verificar(!DatosSesion.InscripcionesAbiertas(convocatoria, convocatoria.Inicio!.Value.AddDays(-1)), "H02: inscripción rechazada antes de apertura");
        convocatoria.Estado = EstadoConvocatoria.Borrador;
        verificar(!DatosSesion.InscripcionesAbiertas(convocatoria, cierre), "H02: borrador sin inscripciones");
        var instante = new DateTimeOffset(2026, 11, 14, 4, 59, 59, TimeSpan.Zero);
        var diaLima = DateOnly.FromDateTime(TimeZoneInfo.ConvertTimeBySystemTimeZoneId(instante, "America/Lima").DateTime);
        convocatoria.Estado = EstadoConvocatoria.Publicada;
        verificar(DatosSesion.InscripcionesAbiertas(convocatoria, diaLima), "H02: último segundo del día de Lima aunque UTC sea mañana");
        verificar(!DatosSesion.InscripcionesAbiertas(convocatoria, DateOnly.FromDateTime(TimeZoneInfo.ConvertTimeBySystemTimeZoneId(instante.AddSeconds(1), "America/Lima").DateTime)), "H02: medianoche de Lima termina inscripción");

        using var http = new HttpClient(new RespuestasFechas()) { BaseAddress = new Uri("http://prueba.invalid/") };
        var datos = new DatosSesion(new AdmitURestClient(http, new HttpContextAccessor()), new IdentidadFechas());
        await datos.InicializarAsync();
        verificar(datos.ErrorCarga is null, "H03/H09: carga de fixtures con fechas ausentes");
        verificar(datos.TarifaInscripcion(101, 301) == 111.23m && datos.TarifaInscripcion(102, 301) == 222.34m,
            "H03: misma modalidad y dos convocatorias conservan tarifas distintas");
        verificar(datos.TarifaInscripcion(999, 301) is null && datos.TarifaInscripcionTexto(999, 301) == "Tarifa no configurada",
            "H03: falta de tarifa no inventa S/350 ni costo de otra convocatoria");
        verificar(datos.TarifaInscripcionTexto(101, 301) == "S/ 111.23" && datos.TarifaInscripcionTexto(102, 301) == "S/ 222.34",
            "H03: resumen presenta la tarifa del par seleccionado");
        var historica = datos.Convocatoria(102)!;
        verificar(historica.Inicio is null && historica.Fin is null && historica.CierreInscripciones is null && historica.LimiteEvaluacion is null && datos.HitosDe(historica).Count == 0,
            "H09: ausencia de cronograma no produce fechas ni hitos ficticios");
        verificar(datos.Convocatoria(101)!.CierreInscripciones == cierre && datos.Convocatoria(101)!.LimiteEvaluacion is null,
            "H02/H09: cierre procede de fecha_fin sin restar quince días ni inventar evaluación");
        var antecedente = datos.Antecedentes.Single();
        verificar(antecedente.Archivo == "" && antecedente.AnioInicio == 0 && antecedente.AnioFin == 0 && antecedente.Fecha is null,
            "H09: antecedente sin archivo/años/fecha conserva ausencia");
        verificar(datos.Historial.Single(x => x.Id == 501).Fecha is null && datos.Historial.Single(x => x.Id == 502).Fecha?.TimeOfDay == TimeSpan.Zero,
            "H09: fecha histórica ausente se conserva y DATE no se convierte a 10:00");
        verificar(AdmitUWA.Data.Formato.Fecha(datos.Historial.Single(x => x.Id == 502).Fecha).Contains("2026") && !AdmitUWA.Data.Formato.Fecha(datos.Historial.Single(x => x.Id == 502).Fecha).Contains(':'),
            "H09: historial muestra fecha sin hora inventada");
        verificar(datos.Postulantes.Single().FechaNacimiento is null,
            "H09: fecha de nacimiento ausente no se sustituye por un cumpleaños ficticio");
        verificar(datos.Postulaciones.Single().FechaRegistro is null && datos.Postulaciones.Single().FechaEnvio is null,
            "H09: postulación histórica sin fechas conserva NULL");
        verificar(datos.Documentos.Single().FechaCarga is null && datos.Observaciones.Single().Fecha is null,
            "H09: documento y observación sin fechas no aparecen cargados hoy");
        verificar(datos.Pagos.All(p => p.FechaGeneracion is null) && datos.Vouchers.Single().FechaRegistro is null && datos.Vouchers.Single().Archivo == "",
            "H09: pago/voucher sin fecha o archivo conserva ausencia");
        verificar(datos.Pagos.Single(p => p.PostulacionId == 702).Estado == EstadoPago.Vencido,
            "H09: pago vencido no se presenta como generado");
        verificar(datos.Carnes.Single().FechaEmision is null && datos.Carnes.Single().InicioVigencia is null && datos.Carnes.Single().FinVigencia is null && datos.Carnes.Single().Aula == "No registrada",
            "H09: carné sin metadatos no inventa emisión/vigencia/aula");

        // Una denegación REST no debe convertirse en cambios que solo existen en pantalla.
        bool Rechazado(Action accion)
        {
            try { accion(); return false; }
            catch (InvalidOperationException) { return true; }
        }
        var observacion = datos.Observaciones.Single();
        var descripcionAntes = observacion.Descripcion;
        var estadoAntes = observacion.Estado;
        verificar(Rechazado(() => datos.ModificarObservacion(observacion, TipoObservacion.Vencido, "Texto rechazado", EstadoObservacion.Subsanada, "Rechazado"))
            && observacion.Descripcion == descripcionAntes && observacion.Estado == estadoAntes && observacion.FechaSubsanacion is null,
            "H01/H17: rechazo REST de edición conserva la observación en memoria");
        var documento = datos.Documentos.Single();
        var estadoDocumento = documento.Estado;
        verificar(Rechazado(() => datos.AprobarDocumento(documento, "Evaluador")) && documento.Estado == estadoDocumento,
            "H01: rechazo REST de aprobación conserva el documento en memoria");
        verificar(Rechazado(() => datos.EliminarObservacion(observacion)) && datos.Observaciones.Count == 1,
            "H01: eliminación denegada conserva la observación visible");
        var voucher = datos.Vouchers.Single();
        var pago = datos.Pagos.Single(x => x.PostulacionId == voucher.PostulacionId);
        var estadoPago = pago.Estado;
        verificar(Rechazado(() => datos.AprobarVoucher(voucher, "Evaluador")) && voucher.Estado == EstadoVoucher.Pendiente && pago.Estado == estadoPago && voucher.FechaRevision is null,
            "H01: rechazo REST de validación conserva pago y voucher en memoria");
        verificar(Rechazado(() => datos.RechazarVoucher(voucher, "Motivo rechazado")) && voucher.Estado == EstadoVoucher.Pendiente && pago.Estado == estadoPago && voucher.MotivoRechazo is null,
            "H01: rechazo REST no modifica el voucher ni su motivo en memoria");
    }

    private sealed class IdentidadFechas : AuthenticationStateProvider
    {
        public override Task<AuthenticationState> GetAuthenticationStateAsync() => Task.FromResult(new AuthenticationState(
            new ClaimsPrincipal(new ClaimsIdentity([
                new Claim(ClaimTypes.NameIdentifier, "111"), new Claim(ClaimTypes.Role, "postulante"),
                new Claim("admitu_token", "sintetico-solo-mock")], "prueba"))));
    }
    private sealed class RespuestasFechas : HttpMessageHandler
    {
        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            if (request.Method != HttpMethod.Get)
                return Task.FromResult(new HttpResponseMessage(HttpStatusCode.Forbidden) { Content = new StringContent("Operación no autorizada") });
            var recurso = request.RequestUri!.AbsolutePath.Split('/')[1];
            var json = recurso switch
            {
                "ConvocatoriaRS" => """[{"id":101,"codigoConvocatoria":"C101","nombre":"Tarifa A","periodo":"2026-2","fechaInicio":"2026-10-01","fechaFin":"2026-11-13","estado":"PUBLICADA","activo":true},{"id":102,"codigoConvocatoria":"C102","nombre":"Tarifa B","periodo":"2026-2","estado":"PUBLICADA","activo":true}]""",
                "ConvocatoriaModalidadRS" => """[{"id":401,"convocatoria":{"id":101},"modalidad":{"id":301},"costoInscripcion":111.23},{"id":402,"convocatoria":{"id":102},"modalidad":{"id":301},"costoInscripcion":222.34}]""",
                "ModalidadRS" => """[{"id":301,"codigoModalidad":"ORD","nombre":"Ordinaria","activo":true}]""",
                "PostulanteRS" => """[{"id":111}]""",
                "PostulacionRS" => """[{"id":701,"postulante":{"id":111},"convocatoria":{"id":101},"modalidadElegida":{"id":401},"estadoActual":{"id":2}}]""",
                "DocumentoPostulacionRS" => """[{"id":801,"postulacion":{"id":701}}]""",
                "DocumentoObservacionRS" => """[{"id":901,"documento":{"id":801}}]""",
                "PagoRS" => """[{"id":1001,"postulacion":{"id":701},"estadoPago":"PENDIENTE","referenciaPasarela":"REF"},{"id":1002,"postulacion":{"id":702},"estadoPago":"VENCIDO"}]""",
                "CarnePostulanteRS" => """[{"id":1101,"postulacion":{"id":701}}]""",
                "AntecedenteAcademicoRS" => """[{"id":601,"postulante":{"id":111},"descripcion":"Solo metadatos históricos","anioInicio":0,"anioFin":0}]""",
                "PostulacionHistorialRS" => """[{"id":501,"postulacion":{"id":701},"estadoActual":{"id":2}},{"id":502,"postulacion":{"id":701},"estadoActual":{"id":3},"fechaCambio":"2026-11-13"}]""",
                _ => "[]"
            };
            return Task.FromResult(new HttpResponseMessage(HttpStatusCode.OK) { Content = new StringContent(json, Encoding.UTF8, "application/json") });
        }
    }
}
