#!/usr/bin/env bash
set -euo pipefail
repo="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo"
command -v mvn >/dev/null
command -v dotnet >/dev/null
java -version
mvn -B -f backend/AdmitU/pom.xml clean package
dotnet build frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj
# Framework-dependent Windows publish; instalar .NET10 Hosting Bundle en IIS.
dotnet publish frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj -c Release -r win-x64 --self-contained false -o work/publish/windows
printf '%s\n' 'Artefactos: WAR en backend/AdmitU/admitu_services/target y cliente en work/publish/windows.'
