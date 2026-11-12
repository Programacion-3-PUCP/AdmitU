package pe.edu.pucp.admitu.archivo.bo;
import java.time.*;
import java.util.*;
import java.security.MessageDigest;
import java.util.function.Supplier;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.boi.IArchivoBO;
import pe.edu.pucp.admitu.archivo.dao.ArchivoDAO;
import pe.edu.pucp.admitu.archivo.impl.ArchivoImpl;
import pe.edu.pucp.admitu.config.TransactionContext;
public class ArchivoBOImpl implements IArchivoBO {
 public static final int MAX_BYTES=5*1024*1024;
 private final ArchivoDAO dao=new ArchivoImpl();
 private static LocalDate hoy(){return LocalDate.now(ZoneId.of("America/Lima"));}
 private void validar(ArchivoDigital a){
  if(a==null||a.getPostulanteId()<=0)throw new IllegalArgumentException("El archivo requiere un postulante válido");
  if(a.getTipo()==null||!Set.of("DNI","FOTOGRAFIA","CERTIFICADO","VOUCHER","DOCUMENTO").contains(a.getTipo()))throw new IllegalArgumentException("Tipo de archivo inválido");
  if(a.getNombre()==null||a.getNombre().isBlank()||a.getNombre().length()>150||a.getNombre().contains("/")||a.getNombre().contains("\\")||a.getNombre().chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Nombre de archivo inválido");
  if(a.getContenidoBase64()==null||a.getContenidoBase64().length()>((MAX_BYTES+2)/3)*4)throw new IllegalArgumentException("Adjunta bytes reales, máximo 5 MB");
  byte[] b;try{b=Base64.getDecoder().decode(a.getContenidoBase64());}catch(IllegalArgumentException e){throw new IllegalArgumentException("Contenido base64 inválido");}
  if(b.length==0||b.length>MAX_BYTES)throw new IllegalArgumentException("Tamaño de archivo inválido, máximo 5 MB");
  String n=a.getNombre().toLowerCase(Locale.ROOT);boolean png=b.length>=8&&b[0]==(byte)137&&b[1]==80&&b[2]==78&&b[3]==71&&b[4]==13&&b[5]==10&&b[6]==26&&b[7]==10;boolean jpg=b.length>=4&&b[0]==(byte)255&&b[1]==(byte)216&&b[2]==(byte)255;boolean pdf=b.length>=5&&b[0]=='%'&&b[1]=='P'&&b[2]=='D'&&b[3]=='F'&&b[4]=='-';
  boolean formato=(png&&n.endsWith(".png")&&"image/png".equals(a.getMime()))||(jpg&&(n.endsWith(".jpg")||n.endsWith(".jpeg"))&&"image/jpeg".equals(a.getMime()))||(pdf&&n.endsWith(".pdf")&&"application/pdf".equals(a.getMime()));
  if(!formato)throw new IllegalArgumentException("Extensión, MIME y firma deben coincidir: JPG, PNG o PDF");
  if(Set.of("DNI","FOTOGRAFIA").contains(a.getTipo())&&!(png||jpg))throw new IllegalArgumentException("DNI y fotografía solo admiten JPG/PNG");
  if("CERTIFICADO".equals(a.getTipo())&&!pdf)throw new IllegalArgumentException("El certificado debe ser PDF");
  if(a.getFechaRegistro()==null)a.setFechaRegistro(hoy());if(a.getFechaRegistro().isAfter(hoy())||a.getFechaRegistro().isBefore(LocalDate.of(1900,1,1)))throw new IllegalArgumentException("La fecha de registro no puede ser futura");
  a.setTamanio(b.length);try{a.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b)));}catch(Exception e){throw new IllegalStateException(e);}
 }
 private <T>T tx(Supplier<T>s){try{T v=s.get();TransactionContext.commit();return v;}catch(Exception e){TransactionContext.rollback();throw e instanceof RuntimeException?(RuntimeException)e:new IllegalStateException(e);}finally{TransactionContext.close();}}
 public ArchivoDigital guardarEnTransaccion(ArchivoDigital a){validar(a);return dao.guardar(a);}
 public ArchivoDigital guardar(ArchivoDigital a){if(a==null||!Set.of("DNI","FOTOGRAFIA","DOCUMENTO").contains(a.getTipo()))throw new IllegalArgumentException("Utiliza la operación correspondiente de certificado o voucher");return tx(()->guardarEnTransaccion(a));}
 public ArchivoDigital guardarCertificado(ArchivoDigital a){if(a==null||!"CERTIFICADO".equals(a.getTipo()))throw new IllegalArgumentException("Certificado inválido");validar(a);if(a.getInstitucion()==null||a.getInstitucion().isBlank()||a.getInstitucion().length()>150||a.getAnioInicio()<1900||a.getAnioFin()<a.getAnioInicio()||a.getAnioFin()>hoy().getYear())throw new IllegalArgumentException("Institución o años de estudio inválidos");return tx(()->{dao.guardarCertificado(a);return dao.guardar(a);});}
 public ArchivoDigital guardarVoucher(ArchivoDigital a){if(a==null||!"VOUCHER".equals(a.getTipo()))throw new IllegalArgumentException("Voucher inválido");validar(a);if(a.getCodigoOperacion()==null||!a.getCodigoOperacion().matches("[A-Za-z0-9-]{4,30}")||!Double.isFinite(a.getMonto())||a.getMonto()<=0)throw new IllegalArgumentException("Número de operación o monto inválido");return tx(()->{dao.guardarVoucher(a);return dao.guardar(a);});}
 public List<ArchivoDigital> listarPorPostulante(int id){if(id<=0)throw new IllegalArgumentException("Postulante inválido");return dao.listarPorPostulante(id);}
 public ArchivoDigital buscarPorId(int id){if(id<=0)throw new IllegalArgumentException("Archivo inválido");return dao.buscarPorId(id);}
 public int eliminar(int id){if(id<=0)throw new IllegalArgumentException("Archivo inválido");return tx(()->dao.eliminar(id));}
}
