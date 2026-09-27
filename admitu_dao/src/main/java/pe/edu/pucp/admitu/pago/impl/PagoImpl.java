package pe.edu.pucp.admitu.pago.impl;

import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.config.TransactionContext;
import pe.edu.pucp.admitu.pago.dao.PagoDAO;
import pe.edu.pucp.admitu.pago.EstadoPago;
import pe.edu.pucp.admitu.pago.MedioPago;
import pe.edu.pucp.admitu.pago.Pago;
import pe.edu.pucp.admitu.postulacion.Postulacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoImpl implements PagoDAO {

    @Override
    public int insertar(Pago pago) {
        String sql = "{call INSERTAR_PAGO(?, ?, ?, ?, ?, ?, ?, ?)}";
        try {
            Connection con = TransactionContext.getConnection();
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter("_id", Types.INTEGER);
                cs.setInt("_postulacion_id", pago.getPostulacion().getId());
                cs.setString("_medio", pago.getMedioPago().name());
                cs.setDouble("_monto", pago.getMonto());
                cs.setString("_codigo", pago.getCodigoPago());
                cs.setString("_referencia", pago.getReferenciaPasarela());
                cs.setString("_estado", pago.getEstadoPago().name());
                cs.setString("_voucher", pago.getRutaVoucher());
                cs.executeUpdate();
                pago.setId(cs.getInt("_id"));
                return pago.getId();
            }
        } catch (Exception ex) {
            System.out.println("ERROR AL INSERTAR PAGO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int modificar(Pago pago) {
        String sql = "{call MODIFICAR_PAGO(?, ?, ?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", pago.getId());
            cs.setString("_estado", pago.getEstadoPago().name());
            cs.setString("_referencia", pago.getReferenciaPasarela());
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL MODIFICAR PAGO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int eliminar(int idPago) {
        String sql = "{call ELIMINAR_PAGO(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPago);
            return cs.executeUpdate();
        } catch (Exception ex) {
            System.out.println("ERROR AL ELIMINAR PAGO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Pago mapear(ResultSet rs) throws Exception {
        Postulacion post = new Postulacion();
        post.setId(rs.getInt("postulacion_id"));
        Pago pago = new Pago(post, MedioPago.valueOf(rs.getString("medio_pago")),
            rs.getDouble("monto"), null, null,
            rs.getString("codigo_pago"), rs.getString("referencia_pasarela"),
            EstadoPago.valueOf(rs.getString("estado_pago")), rs.getString("ruta_voucher"));
        pago.setId(rs.getInt("id"));
        if (rs.getDate("fecha_generacion") != null) pago.setFechaGeneracion(rs.getDate("fecha_generacion").toLocalDate());
        if (rs.getDate("fecha_pago") != null) pago.setFechaPago(rs.getDate("fecha_pago").toLocalDate());
        return pago;
    }

    @Override
    public Pago buscarPorId(int idPago) {
        String sql = "{call LISTAR_PAGO_X_ID(?)}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt("_id", idPago);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
            return null;
        } catch (Exception ex) {
            System.out.println("ERROR AL BUSCAR PAGO: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Pago> listarTodos() {
        List<Pago> lista = null;
        String sql = "{call LISTAR_PAGOS_TODOS()}";
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                if (lista == null) lista = new ArrayList<>();
                lista.add(mapear(rs));
            }
            return lista;
        } catch (Exception ex) {
            System.out.println("ERROR AL LISTAR PAGOS: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
