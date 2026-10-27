namespace AdmitU.Web.Services;

public sealed record Aviso(Guid Id, string Mensaje, string Tono);

// Avisos temporales que confirman el resultado de una acción.
public sealed class ToastService
{
    private readonly List<Aviso> avisos = [];

    public event Action? Cambio;
    public IReadOnlyList<Aviso> Avisos => avisos;

    public void Mostrar(string mensaje, string tono = "info")
    {
        var aviso = new Aviso(Guid.NewGuid(), mensaje, tono);
        avisos.Add(aviso);
        Cambio?.Invoke();
        _ = Task.Delay(4500).ContinueWith(_ => Cerrar(aviso.Id));
    }

    public void Exito(string mensaje) => Mostrar(mensaje, "success");

    public void Cerrar(Guid id)
    {
        if (avisos.RemoveAll(a => a.Id == id) > 0)
            Cambio?.Invoke();
    }
}
