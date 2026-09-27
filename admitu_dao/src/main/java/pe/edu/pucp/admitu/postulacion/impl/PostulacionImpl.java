package pe.edu.pucp.admitu.postulacion.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.configuracion.Convocatoria;
import pe.edu.pucp.admitu.configuracion.ConvocatoriaModalidad;
import pe.edu.pucp.admitu.configuracion.OfertaCarrera;
import pe.edu.pucp.admitu.persona.Postulante;
import pe.edu.pucp.admitu.persona.TipoDocumento;
import pe.edu.pucp.admitu.postulacion.EstadoPostulacion;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulacionImpl implements PostulacionDAO {

    @Override
    public int insertar(Postulacion post) {
        String sql = "{call INSERTAR_POSTULACION(?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulante_id", post.getPostulante().getId());
                cs.setInt("_convocatoria_id", post.getConvocatoria().getId());
                cs.setInt("_modalidad_id", post.getModalidadElegida().getId());
                cs.setInt("_oferta_id", post.getCarreraElegida().getId());
                cs.setInt("_estado_id", post.getEstadoActual().getId());
                cs.setString("_codigo", post.getCodigoInscripcion());
                cs.setString("_observacion", post.getObservacionGeneral());
                cs.executeUpdate();
                post.setId(cs.getInt("_id"));
                return post.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Postulacion post) {
        String sql = "{call MODIFICAR_POSTULACION(?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", post.getId());
            cs.setInt("_estado_id", post.getEstadoActual().getId());
            cs.setString("_observacion", post.getObservacionGeneral());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idPostulacion) {
        String sql = "{call ELIMINAR_POSTULACION(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPostulacion);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Postulacion mapear(ResultSet rs) throws Exception {
        Postulacion post = new Postulacion();
        post.setId(rs.getInt("id"));
        Postulante postulante = new Postulante("TMP", "TMP", null, "tmp@tmp.com",
            TipoDocumento.DNI, "00000000", null, null, null, false, null, false, null, null, null);
        postulante.setId(rs.getInt("postulante_id"));
        post.setPostulante(postulante);
        Convocatoria conv = new Convocatoria();
        conv.setId(rs.getInt("convocatoria_id"));
        post.setConvocatoria(conv);
        ConvocatoriaModalidad mod = new ConvocatoriaModalidad();
        mod.setId(rs.getInt("modalidad_elegida_id"));
        post.setModalidadElegida(mod);
        OfertaCarrera oferta = new OfertaCarrera();
        oferta.setId(rs.getInt("carrera_elegida_id"));
        post.setCarreraElegida(oferta);
        EstadoPostulacion est = new EstadoPostulacion("TMP", "TMP", null);
        est.setId(rs.getInt("estado_actual_id"));
        post.setEstadoActual(est);
        if (rs.getDate("fecha_registro") != null) post.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        post.setCodigoInscripcion(rs.getString("codigo_inscripcion"));
        post.setObservacionGeneral(rs.getString("observacion_general"));
        return post;
    }

    @Override
    public Postulacion buscarPorId(int idPostulacion) {
        String sql = "{call LISTAR_POSTULACION_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPostulacion);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
            return null;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR POSTULACION: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Postulacion> listarTodos() {
        List<Postulacion> lista = null;
        String sql = "{call LISTAR_POSTULACIONES_TODAS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (lista == null) lista = new ArrayList<>();
                lista.add(mapear(rs));
            }
            return lista;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR POSTULACIONES: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
