namespace AdmitUWA.Services;

public sealed record Aviso(Guid Id, string Mensaje, string Tono);

/// <summary>Avisos temporales que confirman el resultado de una acción. Los errores no se cierran solos.</summary>
public sealed class ToastService
{
    private readonly object candado = new();
    private readonly List<Aviso> avisos = [];

    public event Action? Cambio;

    public IReadOnlyList<Aviso> Avisos
    {
        get { lock (candado) return avisos.ToList(); }
    }

    public void Exito(string mensaje) => Mostrar(mensaje, "success", cerrarSolo: true);
    public void Info(string mensaje) => Mostrar(mensaje, "info", cerrarSolo: true);
    public void Error(string mensaje) => Mostrar(mensaje, "danger", cerrarSolo: false);

    public void Cerrar(Guid id)
    {
        bool quitado;
        lock (candado) quitado = avisos.RemoveAll(a => a.Id == id) > 0;
        if (quitado) Cambio?.Invoke();
    }

    private void Mostrar(string mensaje, string tono, bool cerrarSolo)
    {
        var aviso = new Aviso(Guid.NewGuid(), mensaje, tono);
        lock (candado) avisos.Add(aviso);
        Cambio?.Invoke();
        if (cerrarSolo) _ = CerrarDespuesAsync(aviso.Id);
    }

    private async Task CerrarDespuesAsync(Guid id)
    {
        await Task.Delay(4500);
        Cerrar(id);
    }
}
