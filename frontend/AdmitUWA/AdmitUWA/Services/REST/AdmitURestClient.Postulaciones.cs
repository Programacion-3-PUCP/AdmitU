using System.Net.Http.Json;
namespace AdmitUWA.Services.REST;
public partial class AdmitURestClient
{
    public async Task<int> EnviarPostulacionAsync(object envio)
    {
        using var response = await _httpClient.PostAsJsonAsync("PostulacionRS/enviarPostulacion", envio);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<int>();
    }
    public void AsignarEvaluador(int id, int evaluadorId)
    {
        using var response = _httpClient.PutAsync($"PostulacionRS/asignarEvaluador/{id}/{evaluadorId}", null).GetAwaiter().GetResult();
        response.EnsureSuccessStatusCode();
    }
}
