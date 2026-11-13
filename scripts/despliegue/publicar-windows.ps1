# Ejecutar dentro de la EC2 Windows Server de laboratorio ya existente.
# Dry-run por defecto. No crea AWS ni modifica reglas de red.
[CmdletBinding(SupportsShouldProcess)]
param(
 [Parameter(Mandatory=$true)][string]$GlassFishHome,
 [Parameter(Mandatory=$true)][string]$War,
 [Parameter(Mandatory=$true)][string]$FrontendPublish,
 [Parameter(Mandatory=$true)][string]$IisPath,
 [Parameter(Mandatory=$true)][string]$AppPool,
 [Parameter(Mandatory=$true)][uri]$ApiBaseUrl,
 [Parameter(Mandatory=$true)][string]$AllowedHost,
 [Parameter(Mandatory=$true)][string]$DataProtectionKeysPath,
 [string]$DataProtectionCertificateThumbprint='',
 [string]$HealthUrl='',
 [switch]$Apply
)
$ErrorActionPreference='Stop'
if (-not $ApiBaseUrl.AbsoluteUri.EndsWith('/admitu_services/webresources/')) { throw 'ApiBaseUrl incorrecta.' }
if ($ApiBaseUrl.Scheme -ne 'https' -and -not $ApiBaseUrl.IsLoopback) { throw 'API remoto requiere HTTPS; en la misma instancia use loopback privado.' }
if ($AllowedHost -notmatch '^[a-zA-Z0-9.-]+$' -or $AllowedHost.Contains('*')) { throw 'Indique el nombre DNS concreto de IIS, sin esquema, puerto ni comodines.' }
if (-not [System.IO.Path]::IsPathRooted($DataProtectionKeysPath)) { throw 'DataProtectionKeysPath debe ser una ruta absoluta privada.' }
$KeyRoot=[System.IO.Path]::GetFullPath($DataProtectionKeysPath).TrimEnd('\')
foreach($SiteRoot in @($IisPath,$FrontendPublish)){
 $RootPath=[System.IO.Path]::GetFullPath($SiteRoot).TrimEnd('\')
 if($KeyRoot.Equals($RootPath,[System.StringComparison]::OrdinalIgnoreCase) -or $KeyRoot.StartsWith($RootPath+'\',[System.StringComparison]::OrdinalIgnoreCase)){throw 'Las claves deben persistir fuera de los directorios publicados.'}
}
$Asadmin=Join-Path $GlassFishHome 'bin\asadmin.bat'
foreach($RequiredPath in @($Asadmin,$War,$FrontendPublish)){if(-not(Test-Path -LiteralPath $RequiredPath)){throw 'Falta GlassFish o un artefacto.'}}
Write-Host 'Plan: desplegar WAR existente, respaldar y publicar Blazor en IIS existente; comprobar API y UI.'
if(-not $Apply){Write-Host 'DRY-RUN. Añadir -Apply para ejecutar.';return}
if(-not $PSCmdlet.ShouldProcess('GlassFish/IIS existentes','Publicar AdmitU')){return}
Import-Module WebAdministration
if(-not(Test-Path "IIS:\AppPools\$AppPool")){throw 'AppPool existente no encontrado.'}
if(-not(Test-Path -LiteralPath $KeyRoot -PathType Container)){throw 'Prepare primero el directorio privado de claves y sus ACL para la identidad del AppPool.'}
& $Asadmin list-applications
if($LASTEXITCODE -ne 0){throw 'GlassFish no responde; configurar el dominio antes.'}
$BackupPath="$IisPath.backup-$(Get-Date -Format yyyyMMddTHHmmss)-$([guid]::NewGuid().ToString('N').Substring(0,8))"
if(Test-Path -LiteralPath $IisPath){Copy-Item -LiteralPath $IisPath -Destination $BackupPath -Recurse}
& $Asadmin deploy --force=true --name admitu_services --contextroot admitu_services $War
if($LASTEXITCODE -ne 0){throw 'Falló deploy WAR. Revisar server.log por canal privado.'}
Stop-WebAppPool -Name $AppPool
try{
 New-Item -ItemType Directory -Force -Path $IisPath|Out-Null
 Copy-Item -Path (Join-Path $FrontendPublish '*') -Destination $IisPath -Recurse -Force
 $Settings=@{ApiSettings=@{BaseUrl=$ApiBaseUrl.AbsoluteUri};AllowedHosts=$AllowedHost;DataProtection=@{KeysPath=$KeyRoot}}
 if($DataProtectionCertificateThumbprint){$Settings.DataProtection.CertificateThumbprint=$DataProtectionCertificateThumbprint}
 $Settings|ConvertTo-Json -Depth 5|Set-Content -Path (Join-Path $IisPath 'appsettings.Production.json') -Encoding utf8
 Set-ItemProperty "IIS:\AppPools\$AppPool" -Name managedRuntimeVersion -Value ''
}finally{Start-WebAppPool -Name $AppPool}
$ApiHealth=[uri]::new($ApiBaseUrl,'CarreraRS/listarTodasCarreras')
if((Invoke-WebRequest -Uri $ApiHealth -UseBasicParsing -TimeoutSec 30).StatusCode -ne 200){throw 'Smoke REST falló.'}
if($HealthUrl -and (Invoke-WebRequest -Uri $HealthUrl -UseBasicParsing -TimeoutSec 30).StatusCode -ne 200){throw 'Smoke UI falló.'}
Write-Host 'Publicación y smoke HTTP completados. Falta validar login, roles, archivos, flujo transaccional y reportes con cuentas de prueba.'
