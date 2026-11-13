namespace AdmitUWA.Services.REST;
public partial class AdmitURestClient
{
    public void Reactivar(string recurso, int id)
    {
        using var response = _httpClient.PutAsync($"CatalogoRS/reactivar/{recurso.Replace("RS", "")}/{id}", null).GetAwaiter().GetResult();
        response.EnsureSuccessStatusCode();
    }
}
