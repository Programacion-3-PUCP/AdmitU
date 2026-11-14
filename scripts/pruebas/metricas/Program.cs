using AdmitUWA.Models;
using System.Text.Json;

var resultados = new List<object>();
void Comprobar(string caso, bool condicion)
{
    if (!condicion) throw new InvalidOperationException(caso);
    resultados.Add(new { caso, resultado = "PASS" });
}
var p = new Postulacion { Id = 1, ConvocatoriaId = 10, ModalidadId = 20, Estado = EstadoPostulacion.Apta };
var requisitos = new List<RequisitoAplicable> {
    new() { Id = 1, ConvocatoriaId = 10, ModalidadId = 20, Obligatorio = true },
    new() { Id = 2, ConvocatoriaId = 10, ModalidadId = 20, Obligatorio = true },
    new() { Id = 3, ConvocatoriaId = 10, ModalidadId = 20, Obligatorio = false },
    new() { Id = 4, ConvocatoriaId = 11, ModalidadId = 20, Obligatorio = true }
};
var docs = new List<Documento> {
    new() { Id = 1, PostulacionId = 1, RequisitoAplicableId = 1, Estado = EstadoDocumento.Aprobado, ArchivoActivo = true }
};
bool Completo() => IndicadoresExpediente.DocumentosCompletos(p, requisitos, docs);
Comprobar("Un documento aprobado no satisface un segundo requisito obligatorio", !Completo());
docs.Add(new() { Id = 2, PostulacionId = 1, RequisitoAplicableId = 2, Estado = EstadoDocumento.Aprobado, Archivo = "historico.pdf", RutaArchivo = "/docs/historico.pdf" });
Comprobar("El nombre y la ruta históricos no prueban bytes activos", !Completo());
docs[1].ArchivoActivo = true;
Comprobar("Todos los requisitos obligatorios con bytes activos aprobados", Completo());
docs.Add(new() { Id = 3, PostulacionId = 1, RequisitoAplicableId = 3, Estado = EstadoDocumento.Pendiente });
Comprobar("Un requisito opcional pendiente no bloquea la regla SQL", Completo());
docs[1].PostulacionId = 2;
Comprobar("Un archivo de otra postulación no completa el expediente", !Completo());
docs[1].PostulacionId = 1;
docs[1].Estado = EstadoDocumento.Observado;
Comprobar("Los bytes de un documento observado no equivalen a aprobación", !Completo());
docs[1].Estado = EstadoDocumento.Aprobado;
requisitos[1].Activo = false;
docs[1].ArchivoActivo = false;
Comprobar("Desactivar el catálogo no anula una asociación obligatoria vigente en SQL", !Completo());
docs[1].ArchivoActivo = true;
Comprobar("El requisito de otra convocatoria no cambia el resultado", Completo());
Comprobar("Una modalidad sin requisitos obligatorios satisface la guarda SQL", IndicadoresExpediente.DocumentosCompletos(p, [], []));
Comprobar("APTA, FINALIZADA y ADMITIDA son estados favorables", new[] { EstadoPostulacion.Apta, EstadoPostulacion.Finalizada, EstadoPostulacion.Admitida }.All(IndicadoresExpediente.EstadoApto));
Comprobar("NO_ADMITIDA, NO_APTA y ANULADA quedan excluidas de aptos", new[] { EstadoPostulacion.NoAdmitida, EstadoPostulacion.NoApta, EstadoPostulacion.Anulada }.All(e => !IndicadoresExpediente.EstadoApto(e)));
Comprobar("Un estado sin fecha de envío no acredita una postulación enviada", !IndicadoresExpediente.Enviada(p));
p.FechaEnvio = new DateOnly(2026, 11, 12);
Comprobar("La fecha persistida acredita el envío", IndicadoresExpediente.Enviada(p));
p.Estado = EstadoPostulacion.Borrador;
Comprobar("Un borrador nunca cuenta como enviado", !IndicadoresExpediente.Enviada(p));
Console.WriteLine(JsonSerializer.Serialize(new { entorno = "Cloud; modelos reales C# .NET 10, sin WSL ni AWS", total = resultados.Count, resultados }, new JsonSerializerOptions { WriteIndented = true }));
