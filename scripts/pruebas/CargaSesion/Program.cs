using System.Diagnostics;
using System.Net;
using System.Security.Claims;
using System.Text;
using System.Text.Json;
using AdmitUWA.Services;
using AdmitUWA.Services.REST;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Http;

var resultados = new List<object>();
void Verificar(bool condicion, string caso)
{
    if (!condicion) throw new InvalidOperationException(caso);
    resultados.Add(new { caso, resultado = "PASS" });
}
DatosSesion Sesion(int id, Respuestas handler)
{
    var usuario = new ClaimsPrincipal(new ClaimsIdentity([
        new Claim(ClaimTypes.NameIdentifier, id.ToString()),
        new Claim(ClaimTypes.Role, "postulante"),
        new Claim("admitu_token", "sintetico-solo-mock")], "prueba"));
    var http = new HttpClient(handler) { BaseAddress = new Uri("http://prueba.invalid/") };
    return new DatosSesion(new AdmitURestClient(http, new HttpContextAccessor()), new Autenticacion(usuario));
}
var handler = new Respuestas(111);
var reloj = Stopwatch.StartNew();
var sesion = Sesion(111, handler);
var constructorMs = reloj.Elapsed.TotalMilliseconds;
Verificar(handler.Llamadas == 0, "Constructor sin peticiones HTTP");
var carga = sesion.InicializarAsync();
Verificar(!carga.IsCompleted, "La carga devuelve el control mientras espera E/S");
Verificar(ReferenceEquals(carga, sesion.InicializarAsync()), "Una única carga compartida en el scope");
await carga;
Verificar(sesion.ErrorCarga is null && sesion.CargaCompletada && sesion.PostulanteActualId == 111,
    "Carga asíncrona completa y vinculada a la identidad autenticada");
Verificar(handler.Maximo is > 1 and <= 6 && handler.Llamadas == 18,
    "Concurrencia acotada y evaluadores no consultados por el postulante");
var otra = Sesion(222, new Respuestas(222));
await otra.InicializarAsync();
Verificar(otra.Postulantes.Single().Id == 222 && sesion.Postulantes.Single().Id == 111,
    "Scopes con identidades y colecciones separadas");
sesion.Notificar(AdmitUWA.Models.RolUsuario.Postulante, "Aviso", "Solo memoria", "Pago", 111);
Verificar(otra.Notificaciones.Count == 0, "Avisos temporales no se comparten entre scopes");
var fallo = Sesion(333, new Respuestas(333, fallar: true));
await fallo.InicializarAsync();
Verificar(fallo.ErrorCarga is not null && fallo.CargaCompletada && fallo.Postulantes.Count == 0,
    "Error de red visible y sin datos demostrativos");
await PruebasFechasTarifas.EjecutarAsync(Verificar);
Console.WriteLine(JsonSerializer.Serialize(new {
    entorno = "Cloud; HttpMessageHandler simulado, sin SQL ni GlassFish; no acredita RNF04",
    constructor_ms = Math.Round(constructorMs, 3),
    carga_ms = sesion.UltimaCargaMilisegundos,
    peticiones = handler.Llamadas,
    maximo_en_vuelo = handler.Maximo,
    resultados
}, new JsonSerializerOptions { WriteIndented = true }));

sealed class Autenticacion(ClaimsPrincipal usuario) : AuthenticationStateProvider
{
    public override Task<AuthenticationState> GetAuthenticationStateAsync() => Task.FromResult(new AuthenticationState(usuario));
}
sealed class Respuestas(int id, bool fallar = false) : HttpMessageHandler
{
    private int llamadas, activas, maximo;
    public int Llamadas => llamadas;
    public int Maximo => maximo;
    protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        Interlocked.Increment(ref llamadas);
        var actual = Interlocked.Increment(ref activas);
        int anterior;
        do { anterior = maximo; } while (actual > anterior && Interlocked.CompareExchange(ref maximo, actual, anterior) != anterior);
        try
        {
            await Task.Delay(30, cancellationToken);
            if (fallar) return new HttpResponseMessage(HttpStatusCode.ServiceUnavailable);
            var json = request.RequestUri!.AbsolutePath.StartsWith("/PostulanteRS/") ? $"[{{\"id\":{id}}}]" : "[]";
            return new HttpResponseMessage(HttpStatusCode.OK) { Content = new StringContent(json, Encoding.UTF8, "application/json") };
        }
        finally { Interlocked.Decrement(ref activas); }
    }
}
