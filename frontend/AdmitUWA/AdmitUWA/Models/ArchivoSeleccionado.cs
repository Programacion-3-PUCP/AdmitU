namespace AdmitUWA.Models;
public sealed class ArchivoSeleccionado {
 public string Nombre {get;set;}="";
 public string Mime {get;set;}="";
 public byte[] Bytes {get;set;}=[];
 public string ContenidoBase64=>Convert.ToBase64String(Bytes);
 public long Tamanio=>Bytes.LongLength;
}
