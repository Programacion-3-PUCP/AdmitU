using System.Net.Http.Json;

namespace AdmitUWA.Services.REST;

public partial class AdmitURestClient
{
    public async Task<int> EnviarPostulacionAsync(object envio)
    {
        using var response = await _httpClient.PostAsJsonAsync("PostulacionRS/enviarPostulacion", envio);
        if (!response.IsSuccessStatusCode)
        {
            var msg = "No se pudo enviar la postulación. Verifica los documentos y que no exista una postulación previa.";
            try
            {
                var error = await response.Content.ReadFromJsonAsync<Dictionary<string, string>>();
                if (error?.GetValueOrDefault("mensaje") is { Length: > 0 } m)
                    msg = m;
            }
            catch
            {
            }
            throw new InvalidOperationException(msg);
        }
        return await response.Content.ReadFromJsonAsync<int>();
    }

    public void AsignarEvaluador(int id, int evaluadorId)
    {
        using var response = _httpClient.PutAsync($"PostulacionRS/asignarEvaluador/{id}/{evaluadorId}", null).GetAwaiter().GetResult();
        response.EnsureSuccessStatusCode();
    }
}
