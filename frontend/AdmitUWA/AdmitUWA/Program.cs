using AdmitUWA.Components;
using AdmitUWA.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

builder.Services.AddHttpClient("AdmitUApi", client =>
{
    var direccion = builder.Configuration["AdmitU:ApiBaseUrl"]
        ?? "http://localhost:8080/";
    client.BaseAddress = new Uri(direccion);
    client.Timeout = TimeSpan.FromSeconds(15);
});

if (builder.Configuration.GetValue<bool>("AdmitU:ModoDemostracion"))
    builder.Services.AddScoped<IAdmitUService, DemoAdmitUService>();
else
    builder.Services.AddScoped<IAdmitUService, ApiAdmitUService>();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseAntiforgery();

app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();
