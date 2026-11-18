package pe.edu.pucp.admitu.archivo.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import pe.edu.pucp.admitu.archivo.ArchivoDigital;
import pe.edu.pucp.admitu.archivo.dao.ArchivoDAO;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;

public class ArchivoImpl implements ArchivoDAO {

    @Override
    public ArchivoDigital guardar(ArchivoDigital archivo) {
        String sql = "{call GUARDAR_ARCHIVO_DIGITAL(?,?,?,?,?,?,?,?,?,?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulante", archivo.getPostulanteId());
                cs.setString("_tipo", archivo.getTipo());
                cs.setInt("_referencia", archivo.getReferenciaId());
                cs.setString("_nombre", archivo.getNombre());
                cs.setString("_mime", archivo.getMime());
                cs.setBytes("_contenido", Base64.getDecoder().decode(archivo.getContenidoBase64()));
                cs.setString("_sha", archivo.getSha256());
                cs.setDate("_fecha", Date.valueOf(archivo.getFechaRegistro()));
                cs.setInt("_requisito", archivo.getRequisitoAplicableId());
                cs.executeUpdate();
                archivo.setId(cs.getInt("_id"));
                return archivo;
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL GUARDAR ARCHIVO DIGITAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void guardarCertificado(ArchivoDigital archivo) {
        String sql = "{call GUARDAR_CERTIFICADO_METADATA_TIPADA(?,?,?,?,?,?,?,?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_anterior", archivo.getReferenciaId());
                cs.setInt("_postulante", archivo.getPostulanteId());
                cs.setString("_institucion", archivo.getInstitucion());
                cs.setInt("_inicio", archivo.getAnioInicio());
                cs.setInt("_fin", archivo.getAnioFin());
                cs.setString("_nombre", archivo.getNombre());
                cs.setString("_tipo_institucion", archivo.getTipoInstitucion());
                cs.executeUpdate();
                archivo.setReferenciaId(cs.getInt("_id"));
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL GUARDAR CERTIFICADO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void guardarVoucher(ArchivoDigital archivo) {
        String sql = "{call GUARDAR_VOUCHER_METADATA(?,?,?,?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.setInt("_pago", archivo.getReferenciaId());
                cs.setInt("_postulante", archivo.getPostulanteId());
                cs.setString("_operacion", archivo.getCodigoOperacion());
                cs.setDouble("_monto", archivo.getMonto());
                cs.executeUpdate();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL GUARDAR VOUCHER: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private ArchivoDigital mapear(ResultSet rs, boolean incluirContenido) throws SQLException {
        ArchivoDigital archivo = new ArchivoDigital();
        archivo.setId(rs.getInt("id"));
        archivo.setPostulanteId(rs.getInt("postulante_id"));
        archivo.setTipo(rs.getString("tipo"));
        archivo.setReferenciaId(rs.getInt("referencia_id"));
        archivo.setNombre(rs.getString("nombre"));
        archivo.setMime(rs.getString("mime"));
        archivo.setSha256(rs.getString("sha256"));
        archivo.setTamanio(rs.getLong("tamanio"));
        archivo.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        archivo.setInstitucion(rs.getString("institucion"));
        archivo.setTipoInstitucion(rs.getString("tipo_institucion"));
        archivo.setAnioInicio(rs.getInt("anio_inicio"));
        archivo.setAnioFin(rs.getInt("anio_fin"));
        if (incluirContenido) {
            archivo.setContenidoBase64(Base64.getEncoder().encodeToString(rs.getBytes("contenido")));
        }
        return archivo;
    }

    @Override
    public List<ArchivoDigital> listarPorPostulante(int id) {
        List<ArchivoDigital> lista = new ArrayList<>();
        String sql = "{call LISTAR_ARCHIVOS_POSTULANTE(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_postulante", id);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs, false));
                }
            }
            return lista;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR ARCHIVOS DIGITALES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ArchivoDigital buscarPorId(int id) {
        ArchivoDigital archivo = null;
        String sql = "{call BUSCAR_ARCHIVO_DIGITAL(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    archivo = mapear(rs, true);
                }
            }
            return archivo;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR ARCHIVO DIGITAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int id) {
        String sql = "{call ELIMINAR_ARCHIVO_DIGITAL(?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.setInt("_id", id);
                cs.executeUpdate();
                return 1;
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR ARCHIVO DIGITAL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
