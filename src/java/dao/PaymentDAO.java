package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Payment;
import util.DBConnection;

public class PaymentDAO {

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.id, p.booking_id, p.amount, p.payment_method, p.transaction_id, p.payment_date, b.customer_name as guest_name, r.room_number "
                +
                "FROM payments p " +
                "LEFT JOIN bookings b ON p.booking_id = b.id " +
                "LEFT JOIN rooms r ON b.room_id = r.id " +
                "ORDER BY p.payment_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payments;
    }

    public List<Payment> searchPayments(String query) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.id, p.booking_id, p.amount, p.payment_method, p.transaction_id, p.payment_date, b.customer_name as guest_name, r.room_number "
                +
                "FROM payments p " +
                "LEFT JOIN bookings b ON p.booking_id = b.id " +
                "LEFT JOIN rooms r ON b.room_id = r.id " +
                "WHERE p.transaction_id LIKE ? OR b.customer_name LIKE ? " +
                "ORDER BY p.payment_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payments;
    }

    public Payment getPaymentById(int id) {
        String sql = "SELECT p.id, p.booking_id, p.amount, p.payment_method, p.transaction_id, p.payment_date, b.customer_name as guest_name, r.room_number "
                +
                "FROM payments p " +
                "LEFT JOIN bookings b ON p.booking_id = b.id " +
                "LEFT JOIN rooms r ON b.room_id = r.id " +
                "WHERE p.id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setBookingId(rs.getInt("booking_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setPaymentDate(rs.getTimestamp("payment_date"));
        p.setGuestName(rs.getString("guest_name"));
        p.setRoomNumber(rs.getString("room_number"));
        return p;
    }
}
