namespace AdmitUWA.Services.REST;
public partial class AdmitURestClient
{
 public async Task<byte[]> GenerarReporteAsync(string tipo,int convocatoriaId,int modalidadId,DateOnly? desde,DateOnly? hasta)
 {
  if(tipo is not ("Ingresos" or "Observados" or "Vacantes"))throw new ArgumentException("Reporte no válido");
  string path=$"ReporteRS/generar{tipo}?convocatoriaId={convocatoriaId}&modalidadId={modalidadId}";
  if(desde is {} d)path+=$"&desde={d:yyyy-MM-dd}";
  if(hasta is {} h)path+=$"&hasta={h:yyyy-MM-dd}";
  using var response=await _httpClient.GetAsync(path);
  response.EnsureSuccessStatusCode();
  if(response.Content.Headers.ContentType?.MediaType!="application/pdf")throw new InvalidOperationException("El servidor no devolvió PDF");
  return await response.Content.ReadAsByteArrayAsync();
 }
}
