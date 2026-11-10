using System.Globalization;
using AdmitUWA.Components;
using AdmitUWA.Data;
using AdmitUWA.Services;
using AdmitUWA.Services.REST;

// Fechas y números en español del Perú, sin depender de la configuración del servidor.
CultureInfo.DefaultThreadCurrentCulture = Formato.Peru;
CultureInfo.DefaultThreadCurrentUICulture = Formato.Peru;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

var apiBaseUrl = builder.Configuration["ApiSettings:BaseUrl"] ?? "http://localhost:8080/admitu_services/webresources/";
builder.Services.AddSingleton(_ => new HttpClient
{
    BaseAddress = new Uri(apiBaseUrl),
    Timeout = TimeSpan.FromSeconds(30)
});
builder.Services.AddSingleton<AdmitURestClient>();
builder.Services.AddScoped<ToastService>();
builder.Services.AddSingleton<DatosSesion>();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/error", createScopeForErrors: true);
    app.UseHsts();
}

app.UseStaticFiles();
app.UseAntiforgery();

app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();
