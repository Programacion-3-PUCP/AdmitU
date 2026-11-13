package pe.edu.pucp.admitu.archivo;
import java.time.LocalDate;
public class ArchivoDigital {
 private String tipoInstitucion="COLEGIO";
 public String getTipoInstitucion(){return tipoInstitucion;}
 public void setTipoInstitucion(String tipo){tipoInstitucion=tipo;}

 private int id;
 public int getId() {return id;}
 public void setId(int value) {id=value;}
 private int postulanteId;
 public int getPostulanteId() {return postulanteId;}
 public void setPostulanteId(int value) {postulanteId=value;}
 private int referenciaId;
 public int getReferenciaId() {return referenciaId;}
 public void setReferenciaId(int value) {referenciaId=value;}
 private String tipo;
 public String getTipo() {return tipo;}
 public void setTipo(String value) {tipo=value;}
 private String nombre;
 public String getNombre() {return nombre;}
 public void setNombre(String value) {nombre=value;}
 private String mime;
 public String getMime() {return mime;}
 public void setMime(String value) {mime=value;}
 private String contenidoBase64;
 public String getContenidoBase64() {return contenidoBase64;}
 public void setContenidoBase64(String value) {contenidoBase64=value;}
 private String sha256;
 public String getSha256() {return sha256;}
 public void setSha256(String value) {sha256=value;}
 private long tamanio;
 public long getTamanio() {return tamanio;}
 public void setTamanio(long value) {tamanio=value;}
 private LocalDate fechaRegistro;
 public LocalDate getFechaRegistro() {return fechaRegistro;}
 public void setFechaRegistro(LocalDate value) {fechaRegistro=value;}
 private String institucion;
 public String getInstitucion() {return institucion;}
 public void setInstitucion(String value) {institucion=value;}
 private int anioInicio;
 public int getAnioInicio() {return anioInicio;}
 public void setAnioInicio(int value) {anioInicio=value;}
 private int anioFin;
 public int getAnioFin() {return anioFin;}
 public void setAnioFin(int value) {anioFin=value;}
 private String codigoOperacion;
 public String getCodigoOperacion() {return codigoOperacion;}
 public void setCodigoOperacion(String value) {codigoOperacion=value;}
 private double monto;
 public double getMonto() {return monto;}
 public void setMonto(double value) {monto=value;}
 private int requisitoAplicableId;
 public int getRequisitoAplicableId() {return requisitoAplicableId;}
 public void setRequisitoAplicableId(int value) {requisitoAplicableId=value;}
}
