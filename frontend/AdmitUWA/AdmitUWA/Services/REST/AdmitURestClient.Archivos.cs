using System.Net.Http.Json;
namespace AdmitUWA.Services.REST;
public sealed class ArchivoDigitalDto {
 public int Id {get;set;} public int PostulanteId {get;set;} public int ReferenciaId {get;set;} public int RequisitoAplicableId {get;set;}
 public string Tipo {get;set;}=""; public string Nombre {get;set;}=""; public string Mime {get;set;}=""; public string? ContenidoBase64 {get;set;}
 public string Sha256 {get;set;}="";public long Tamanio {get;set;}public DateOnly? FechaRegistro {get;set;}
 public string Institucion {get;set;}="";public int AnioInicio {get;set;}public int AnioFin {get;set;}public string CodigoOperacion {get;set;}="";public decimal Monto {get;set;}
}
public partial class AdmitURestClient {
 public async Task<List<ArchivoDigitalDto>> ListarArchivosAsync(int postulanteId){using var r=await _httpClient.GetAsync($"ArchivoDigitalRS/listarArchivosPorPostulante/{postulanteId}");r.EnsureSuccessStatusCode();return await r.Content.ReadFromJsonAsync<List<ArchivoDigitalDto>>(_jsonOptions)??[];}
 public async Task<ArchivoDigitalDto> GuardarArchivoAsync(ArchivoDigitalDto archivo,string accion="guardarArchivo"){
  if(accion is not("guardarArchivo" or "guardarCertificado" or "guardarVoucher"))throw new ArgumentException("Acción de archivo inválida");
  using var response=await _httpClient.PostAsJsonAsync("ArchivoDigitalRS/"+accion,archivo,_jsonOptions);
  if(!response.IsSuccessStatusCode){var msg="No fue posible guardar el archivo. Revisa los datos, el estado y el tamaño.";try{var error=await response.Content.ReadFromJsonAsync<Dictionary<string,string>>();if(error?.GetValueOrDefault("mensaje") is {Length:>0} m)msg=m;}catch{}throw new InvalidOperationException(msg);}
  return (await response.Content.ReadFromJsonAsync<ArchivoDigitalDto>(_jsonOptions))!;
 }
 public async Task<byte[]> DescargarArchivoAsync(int id){using var r=await _httpClient.GetAsync($"ArchivoDigitalRS/descargarArchivo/{id}");r.EnsureSuccessStatusCode();return await r.Content.ReadAsByteArrayAsync();}
 public async Task EliminarArchivoAsync(int id){using var r=await _httpClient.DeleteAsync($"ArchivoDigitalRS/eliminarArchivo/{id}");if(!r.IsSuccessStatusCode)throw new InvalidOperationException("No se puede eliminar el archivo en su estado actual.");}
}
