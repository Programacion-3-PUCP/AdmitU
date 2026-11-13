#!/usr/bin/env python3
"""Compara clientes original/corregido con HTTP simulado; nunca solicita credenciales."""
import json
from pathlib import Path
import subprocess
import tempfile
from xml.sax.saxutils import escape

repo = Path(__file__).resolve().parents[2]
base = "2ad401985177e8b26e16db4840c2e9c262cdb2b5"
frontend = Path("frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj")
(repo / "work").mkdir(exist_ok=True)
with tempfile.TemporaryDirectory(prefix="medicion-carga-", dir=repo / "work") as temporal:
    temporal = Path(temporal)
    archivo = temporal / "base.tar"
    subprocess.run(["git", "archive", "--output", str(archivo), base, str(frontend.parent)], cwd=repo, check=True)
    original = temporal / "base"
    original.mkdir()
    subprocess.run(["tar", "-xf", str(archivo), "-C", str(original)], check=True)
    resultados = {"base_commit": base, "metodo": "HTTP simulado 30ms; cinco muestras tras una vuelta de calentamiento; no acredita RNF04"}
    for etiqueta, referencia in (("antes", original / frontend), ("despues", repo / frontend)):
        proyecto = temporal / etiqueta
        proyecto.mkdir()
        (proyecto / "Program.cs").write_text((repo / "scripts/pruebas/CargaSesion/MedicionCarga.cs.txt").read_text())
        (proyecto / "Medicion.csproj").write_text(
            '<Project Sdk="Microsoft.NET.Sdk"><PropertyGroup><OutputType>Exe</OutputType>'
            '<TargetFramework>net10.0</TargetFramework><ImplicitUsings>enable</ImplicitUsings>'
            '<Nullable>enable</Nullable></PropertyGroup><ItemGroup>'
            '<FrameworkReference Include="Microsoft.AspNetCore.App"/><ProjectReference Include="'
            + escape(str(referencia), {'"': '&quot;'}) + '"/></ItemGroup></Project>')
        ejecucion = subprocess.run(["dotnet", "run", "--project", str(proyecto / "Medicion.csproj"), "--verbosity", "quiet"],
                                  cwd=repo, text=True, capture_output=True)
        if ejecucion.returncode:
            raise RuntimeError(ejecucion.stdout + ejecucion.stderr)
        resultados[etiqueta] = json.loads(ejecucion.stdout[ejecucion.stdout.index("{"):])
    print(json.dumps(resultados, indent=2, ensure_ascii=False))
