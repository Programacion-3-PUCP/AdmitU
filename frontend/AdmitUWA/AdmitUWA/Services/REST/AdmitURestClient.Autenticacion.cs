using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Security.Claims;
namespace AdmitUWA.Services.REST;
public class SesionDto { public int PersonaId {get;set;} public string Correo {get;set;}="";public string Rol {get;set;}=""; public string Token {get;set;}=""; }
public partial class AdmitURestClient
{
 private static readonly HashSet<string> RecursosPublicos=["FacultadRS","CarreraRS","SedeRS","ConvocatoriaRS","OfertaCarreraRS","ConvocatoriaModalidadRS","ModalidadRS","RequisitoRS","RequisitoConvocatoriaModalidadRS","PaisRS","InstitucionEducativaRS","EstadoPostulacionRS"];
 public string Rol {get;private set;}=""; public int PersonaId {get;private set;} public bool Autenticado {get;private set;}
 public void ActualizarSesion(ClaimsPrincipal? user)
 {
  var token=user?.FindFirst("admitu_token")?.Value;
  Autenticado=user?.Identity?.IsAuthenticated==true&&!string.IsNullOrWhiteSpace(token);
  Rol=Autenticado?user!.FindFirst(ClaimTypes.Role)?.Value??"":"";
  PersonaId=Autenticado&&int.TryParse(user!.FindFirst(ClaimTypes.NameIdentifier)?.Value,out var id)?id:0;
  _httpClient.DefaultRequestHeaders.Authorization=Autenticado?new AuthenticationHeaderValue("Bearer",token):null;
 }
 public async Task<SesionDto?> IngresarAsync(string correo,string clave)
 {
  using var response=await _httpClient.PostAsJsonAsync("AutenticacionRS/ingresar",new {correo,clave});
  if(response.StatusCode==System.Net.HttpStatusCode.Unauthorized)return null;
  response.EnsureSuccessStatusCode();return await response.Content.ReadFromJsonAsync<SesionDto>();
 }
 public async Task<int> RegistrarCuentaAsync(PostulanteDto postulante,string clave)
 {
  using var response=await _httpClient.PostAsJsonAsync("AutenticacionRS/registrar",new {postulante,clave});
  if(!response.IsSuccessStatusCode)throw new InvalidOperationException("No se pudo crear la cuenta. Revisa el documento, correo y contraseña; pueden estar registrados.");
  return await response.Content.ReadFromJsonAsync<int>();
 }
 public async Task SalirAsync(){using var r=await _httpClient.PostAsync("AutenticacionRS/salir",null);r.EnsureSuccessStatusCode();}
}
