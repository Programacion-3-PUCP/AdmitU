using System.Globalization;
using System.Security.Claims;
using AdmitUWA.Components;
using AdmitUWA.Data;
using AdmitUWA.Services;
using AdmitUWA.Services.REST;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Antiforgery;
CultureInfo.DefaultThreadCurrentCulture=Formato.Peru;
CultureInfo.DefaultThreadCurrentUICulture=Formato.Peru;
var builder=WebApplication.CreateBuilder(args);
builder.Services.AddRazorComponents().AddInteractiveServerComponents();
builder.Services.AddHttpContextAccessor();
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme).AddCookie(o=>{
 o.LoginPath="/";o.AccessDeniedPath="/acceso-denegado";o.Cookie.Name="AdmitU.Sesion";o.Cookie.HttpOnly=true;
 o.Cookie.SameSite=SameSiteMode.Strict;o.Cookie.SecurePolicy=CookieSecurePolicy.SameAsRequest;
 o.ExpireTimeSpan=TimeSpan.FromHours(8);o.SlidingExpiration=false;
});
builder.Services.AddAuthorization();builder.Services.AddCascadingAuthenticationState();
var apiBaseUrl=builder.Configuration["ApiSettings:BaseUrl"]??"http://localhost:8080/admitu_services/webresources/";
builder.Services.AddScoped(_=>new HttpClient{BaseAddress=new Uri(apiBaseUrl),Timeout=TimeSpan.FromSeconds(30)});
builder.Services.AddScoped<AdmitURestClient>();builder.Services.AddScoped<ToastService>();builder.Services.AddScoped<DatosSesion>();
var app=builder.Build();
if(!app.Environment.IsDevelopment()){app.UseExceptionHandler("/error",createScopeForErrors:true);app.UseHsts();}
app.UseStaticFiles();app.UseAuthentication();app.UseAuthorization();app.UseAntiforgery();
app.MapPost("/cuenta/ingresar",async (HttpContext ctx,IAntiforgery anti,AdmitURestClient api)=>{
 try{await anti.ValidateRequestAsync(ctx);}catch(AntiforgeryValidationException){return Results.BadRequest("Formulario expirado. Recarga la página.");}
 var f=await ctx.Request.ReadFormAsync();SesionDto? u;
 try{u=await api.IngresarAsync(f["correo"].ToString(),f["clave"].ToString());}catch(HttpRequestException){return Results.LocalRedirect("/?error=servicio");}
 if(u is null||string.IsNullOrWhiteSpace(u.Token))return Results.LocalRedirect("/?error=credenciales");
 var claims=new[]{new Claim(ClaimTypes.NameIdentifier,u.PersonaId.ToString()),new Claim(ClaimTypes.Name,u.Correo),new Claim(ClaimTypes.Role,u.Rol),new Claim("admitu_token",u.Token)};
 await ctx.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme,new ClaimsPrincipal(new ClaimsIdentity(claims,CookieAuthenticationDefaults.AuthenticationScheme)),new AuthenticationProperties{IsPersistent=false,ExpiresUtc=DateTimeOffset.UtcNow.AddHours(8)});
 return Results.LocalRedirect(u.Rol switch{"admin"=>"/admin","evaluador"=>"/evaluador",_=>"/postulante"});
}).AllowAnonymous();
app.MapPost("/cuenta/salir",async(HttpContext ctx,IAntiforgery anti,AdmitURestClient api)=>{
 try{await anti.ValidateRequestAsync(ctx);}catch(AntiforgeryValidationException){return Results.BadRequest("Formulario expirado.");}
 try{await api.SalirAsync();}catch(HttpRequestException){}
 await ctx.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);return Results.LocalRedirect("/");
}).RequireAuthorization();
app.MapRazorComponents<App>().AddInteractiveServerRenderMode();app.Run();
