package pe.edu.pucp.admitu.pago.impl;
import pe.edu.pucp.admitu.config.DBManager;
import pe.edu.pucp.admitu.pago.*;
import pe.edu.pucp.admitu.pago.dao.PagoDAO;
import pe.edu.pucp.admitu.postulacion.Postulacion;
import pe.edu.pucp.admitu.postulacion.dao.PostulacionDAO;
import pe.edu.pucp.admitu.postulacion.impl.PostulacionDAOImpl;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class PagoDAOImpl implements PagoDAO {
    private final PostulacionDAO postulacionDAO = new PostulacionDAOImpl();

    @Override
    public boolean insertar(Pago obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pago_insertar(?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setInt(1, obj.getPostulacion().getId());
            cs.setString(2, obj.getMedioPago().name());
            cs.setDouble(3, obj.getMonto());
            cs.setDate(4, obj.getFechaGeneracion() != null ? Date.valueOf(obj.getFechaGeneracion()) : null);
            cs.setDate(5, obj.getFechaPago() != null ? Date.valueOf(obj.getFechaPago()) : null);
            cs.setString(6, obj.getCodigoPago());
            cs.setString(7, obj.getReferenciaPasarela());
            cs.setString(8, obj.getEstadoPago().name());
            cs.setString(9, obj.getRutaVoucher());
            cs.registerOutParameter(10, Types.INTEGER);
            cs.execute(); obj.setId(cs.getInt(10)); return true;
        } catch (SQLException ex) { System.out.println("ERROR insertar Pago: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean actualizar(Pago obj) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pago_actualizar(?,?,?,?)}")) {
            cs.setInt(1, obj.getId());
            cs.setDate(2, obj.getFechaPago() != null ? Date.valueOf(obj.getFechaPago()) : null);
            cs.setString(3, obj.getEstadoPago().name());
            cs.setString(4, obj.getRutaVoucher());
            cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR actualizar Pago: " + ex.getMessage()); }
        return false;
    }
    @Override
    public boolean eliminar(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pago_eliminar(?)}")) {
            cs.setInt(1, id); cs.execute(); return true;
        } catch (SQLException ex) { System.out.println("ERROR eliminar Pago: " + ex.getMessage()); }
        return false;
    }
    @Override
    public Pago buscarPorId(int id) {
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pago_buscar_por_id(?)}")) {
            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) { if (rs.next()) return mapear(rs); }
        } catch (SQLException ex) { System.out.println("ERROR buscar Pago: " + ex.getMessage()); }
        return null;
    }
    @Override
    public List<Pago> listar() { throw new UnsupportedOperationException("Usar listarPorPostulacion(id)"); }
    @Override
    public List<Pago> listarPorPostulacion(int idPostulacion) {
        List<Pago> lista = new ArrayList<>();
        try (Connection con = DBManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_pago_listar_por_postulacion(?)}")) {
            cs.setInt(1, idPostulacion);
            try (ResultSet rs = cs.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        } catch (SQLException ex) { System.out.println("ERROR listar Pago: " + ex.getMessage()); }
        return lista;
    }
    private Pago mapear(ResultSet rs) throws SQLException {
        Postulacion postulacion = postulacionDAO.buscarPorId(rs.getInt("id_postulacion"));
        Date fg = rs.getDate("fecha_generacion"), fp = rs.getDate("fecha_pago");
        Pago p = new Pago(postulacion, MedioPago.valueOf(rs.getString("medio_pago")), rs.getDouble("monto"),
                fg != null ? fg.toLocalDate() : null, fp != null ? fp.toLocalDate() : null,
                rs.getString("codigo_pago"), rs.getString("referencia_pasarela"),
                EstadoPago.valueOf(rs.getString("estado_pago")), rs.getString("ruta_voucher"));
        p.setId(rs.getInt("id"));
        return p;
    }
}
