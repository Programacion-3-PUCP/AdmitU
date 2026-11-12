package pe.edu.pucp.admitu.archivo.impl;
import java.sql.*;
import java.util.*;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.dao.ArchivoDAO;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
public class ArchivoImpl implements ArchivoDAO {
 public ArchivoDigital guardar(ArchivoDigital a) {
  try {
   Connection c=TransactionContext.getConnection();
   try(CallableStatement s=c.prepareCall("{call GUARDAR_ARCHIVO_DIGITAL(?,?,?,?,?,?,?,?,?,?)}")) {
    s.registerOutParameter(1,Types.INTEGER);s.setInt(2,a.getPostulanteId());s.setString(3,a.getTipo());s.setInt(4,a.getReferenciaId());s.setString(5,a.getNombre());s.setString(6,a.getMime());s.setBytes(7,Base64.getDecoder().decode(a.getContenidoBase64()));s.setString(8,a.getSha256());s.setDate(9,java.sql.Date.valueOf(a.getFechaRegistro()));s.setInt(10,a.getRequisitoAplicableId());s.execute();a.setId(s.getInt(1));return a;
   }
  }catch(SQLException e){throw new IllegalStateException("No se pudo guardar el archivo",e);}
 }
 public void guardarCertificado(ArchivoDigital a) {
  try(CallableStatement s=TransactionContext.getConnection().prepareCall("{call GUARDAR_CERTIFICADO_METADATA(?,?,?,?,?,?,?)}")) {
   s.registerOutParameter(1,Types.INTEGER);s.setInt(2,a.getReferenciaId());s.setInt(3,a.getPostulanteId());s.setString(4,a.getInstitucion());s.setInt(5,a.getAnioInicio());s.setInt(6,a.getAnioFin());s.setString(7,a.getNombre());s.execute();a.setReferenciaId(s.getInt(1));
  }catch(SQLException e){throw new IllegalStateException("No se pudo guardar el certificado",e);}
 }
 public void guardarVoucher(ArchivoDigital a) {
  try(CallableStatement s=TransactionContext.getConnection().prepareCall("{call GUARDAR_VOUCHER_METADATA(?,?,?,?)}")) {
   s.setInt(1,a.getReferenciaId());s.setInt(2,a.getPostulanteId());s.setString(3,a.getCodigoOperacion());s.setDouble(4,a.getMonto());s.execute();
  }catch(SQLException e){throw new IllegalStateException("No se pudo guardar el voucher",e);}
 }
 private ArchivoDigital map(ResultSet r,boolean contenido)throws SQLException {
  ArchivoDigital a=new ArchivoDigital();a.setId(r.getInt("id"));a.setPostulanteId(r.getInt("postulante_id"));a.setTipo(r.getString("tipo"));a.setReferenciaId(r.getInt("referencia_id"));a.setNombre(r.getString("nombre"));a.setMime(r.getString("mime"));a.setSha256(r.getString("sha256"));a.setTamanio(r.getLong("tamanio"));a.setFechaRegistro(r.getDate("fecha_registro").toLocalDate());
  a.setInstitucion(r.getString("institucion"));a.setAnioInicio(r.getInt("anio_inicio"));a.setAnioFin(r.getInt("anio_fin"));
  if(contenido)a.setContenidoBase64(Base64.getEncoder().encodeToString(r.getBytes("contenido")));return a;
 }
 public List<ArchivoDigital> listarPorPostulante(int id) {
  try(Connection c=DBManager.getInstance().getConnection();CallableStatement s=c.prepareCall("{call LISTAR_ARCHIVOS_POSTULANTE(?)}")){
   s.setInt(1,id);try(ResultSet r=s.executeQuery()){List<ArchivoDigital> list=new ArrayList<>();while(r.next())list.add(map(r,false));return list;}
  }catch(SQLException e){throw new IllegalStateException("No se pudieron listar los archivos",e);}
 }
 public ArchivoDigital buscarPorId(int id) {
  try(Connection c=DBManager.getInstance().getConnection();CallableStatement s=c.prepareCall("{call BUSCAR_ARCHIVO_DIGITAL(?)}")){
   s.setInt(1,id);try(ResultSet r=s.executeQuery()){return r.next()?map(r,true):null;}
  }catch(SQLException e){throw new IllegalStateException("No se pudo recuperar el archivo",e);}
 }
 public int eliminar(int id) {
  try(CallableStatement s=TransactionContext.getConnection().prepareCall("{call ELIMINAR_ARCHIVO_DIGITAL(?)}")){s.setInt(1,id);s.execute();return 1;}
  catch(SQLException e){throw new IllegalStateException("No se pudo eliminar el archivo",e);}
 }
}
