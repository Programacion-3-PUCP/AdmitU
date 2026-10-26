using System.Net.Http.Json;
using AdmitUWA.Models;

namespace AdmitUWA.Services;

public sealed class ApiAdmitUService(IHttpClientFactory fabrica) : IAdmitUService
{
    private HttpClient Cliente => fabrica.CreateClient("AdmitUApi");

    private async Task<List<T>> ListarAsync<T>(string ruta)
    {
        using var cliente = Cliente;
        return await cliente.GetFromJsonAsync<List<T>>(ruta) ?? [];
    }

    private async Task<T?> ObtenerAsync<T>(string ruta, int id)
    {
        using var cliente = Cliente;
        using var respuesta = await cliente.GetAsync($"{ruta}/{id}");
        if (respuesta.StatusCode == System.Net.HttpStatusCode.NotFound)
            return default;
        await ComprobarAsync(respuesta);
        return await respuesta.Content.ReadFromJsonAsync<T>();
    }

    private async Task GuardarAsync<T>(string ruta, int id, T modelo)
    {
        using var cliente = Cliente;
        using var respuesta = id == 0
            ? await cliente.PostAsJsonAsync(ruta, modelo)
            : await cliente.PutAsJsonAsync($"{ruta}/{id}", modelo);
        await ComprobarAsync(respuesta);
    }

    private async Task EliminarAsync(string ruta, int id)
    {
        using var cliente = Cliente;
        using var respuesta = await cliente.DeleteAsync($"{ruta}/{id}");
        await ComprobarAsync(respuesta);
    }

    private static async Task ComprobarAsync(HttpResponseMessage respuesta)
    {
        if (respuesta.IsSuccessStatusCode)
            return;
        var detalle = await respuesta.Content.ReadAsStringAsync();
        if (detalle.Length > 350)
            detalle = detalle[..350];
        throw new HttpRequestException(
            $"El servidor respondió {(int)respuesta.StatusCode}. {detalle}".Trim());
    }

    public Task<List<PostulanteDto>> ListarPostulantesAsync() => ListarAsync<PostulanteDto>("api/postulantes");
    public Task<PostulanteDto?> ObtenerPostulanteAsync(int id) => ObtenerAsync<PostulanteDto>("api/postulantes", id);
    public Task GuardarPostulanteAsync(PostulanteDto m) => GuardarAsync("api/postulantes", m.Id, m);
    public Task EliminarPostulanteAsync(int id) => EliminarAsync("api/postulantes", id);

    public Task<List<FacultadDto>> ListarFacultadesAsync() => ListarAsync<FacultadDto>("api/facultades");
    public Task<FacultadDto?> ObtenerFacultadAsync(int id) => ObtenerAsync<FacultadDto>("api/facultades", id);
    public Task GuardarFacultadAsync(FacultadDto m) => GuardarAsync("api/facultades", m.Id, m);
    public Task DesactivarFacultadAsync(int id) => EliminarAsync("api/facultades", id);

    public Task<List<CarreraDto>> ListarCarrerasAsync() => ListarAsync<CarreraDto>("api/carreras");
    public Task<CarreraDto?> ObtenerCarreraAsync(int id) => ObtenerAsync<CarreraDto>("api/carreras", id);
    public Task GuardarCarreraAsync(CarreraDto m) => GuardarAsync("api/carreras", m.Id, m);
    public Task DesactivarCarreraAsync(int id) => EliminarAsync("api/carreras", id);

    public Task<List<ConvocatoriaDto>> ListarConvocatoriasAsync() => ListarAsync<ConvocatoriaDto>("api/convocatorias");
    public Task<ConvocatoriaDto?> ObtenerConvocatoriaAsync(int id) => ObtenerAsync<ConvocatoriaDto>("api/convocatorias", id);
    public Task GuardarConvocatoriaAsync(ConvocatoriaDto m) => GuardarAsync("api/convocatorias", m.Id, m);
    public Task DesactivarConvocatoriaAsync(int id) => EliminarAsync("api/convocatorias", id);

    public Task<List<ModalidadDto>> ListarModalidadesAsync() => ListarAsync<ModalidadDto>("api/modalidades");
    public Task<ModalidadDto?> ObtenerModalidadAsync(int id) => ObtenerAsync<ModalidadDto>("api/modalidades", id);
    public Task GuardarModalidadAsync(ModalidadDto m) => GuardarAsync("api/modalidades", m.Id, m);
    public Task DesactivarModalidadAsync(int id) => EliminarAsync("api/modalidades", id);

    public Task<List<PostulacionDto>> ListarPostulacionesAsync() => ListarAsync<PostulacionDto>("api/postulaciones");
    public Task<List<PagoDto>> ListarPagosAsync() => ListarAsync<PagoDto>("api/pagos");
    public async Task AprobarPagoAsync(int id)
    {
        using var cliente = Cliente;
        using var respuesta = await cliente.PutAsync($"api/pagos/{id}/aprobar", null);
        await ComprobarAsync(respuesta);
    }
    public Task<List<DocumentoPostulacionDto>> ListarDocumentosAsync() => ListarAsync<DocumentoPostulacionDto>("api/documentos-postulacion");
}
