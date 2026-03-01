package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Booking;
import util.DBConnection;

public class BookingDAO {
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, u.first_name, u.last_name, u.email as guest_email, u.phone, u.address " +
                "FROM bookings b LEFT JOIN users u ON b.guest_id = u.id";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean addBooking(Booking booking) {
        String sqlCheckUser = "SELECT id FROM users WHERE email = ?";
        String sqlUser = "INSERT INTO users (first_name, last_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        String sqlBooking = "INSERT INTO bookings (guest_id, customer_name, customer_email, room_id, check_in_date, check_out_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlRoom = "UPDATE rooms SET status='OCCUPIED' WHERE id=?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int guestId = -1;

                // 1. Check if guest exists
                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckUser)) {
                    pstmtCheck.setString(1, booking.getEmail());
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            guestId = rs.getInt("id");
                        }
                    }
                }

                // 2. Register Guest if not exists
                if (guestId == -1) {
                    try (PreparedStatement pstmtU = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                        pstmtU.setString(1, booking.getFirstName());
                        pstmtU.setString(2, booking.getLastName());
                        pstmtU.setString(3, booking.getEmail());
                        pstmtU.setString(4, booking.getPhone());
                        pstmtU.setString(5, booking.getAddress());
                        pstmtU.executeUpdate();

                        try (ResultSet rs = pstmtU.getGeneratedKeys()) {
                            if (rs.next()) {
                                guestId = rs.getInt(1);
                            }
                        }
                    }
                }

                // 3. Create Booking
                try (PreparedStatement pstmtB = conn.prepareStatement(sqlBooking)) {
                    pstmtB.setInt(1, guestId);
                    pstmtB.setString(2, booking.getFirstName() + " " + booking.getLastName());
                    pstmtB.setString(3, booking.getEmail());
                    pstmtB.setInt(4, booking.getRoomId());
                    pstmtB.setDate(5, new java.sql.Date(booking.getCheckInDate().getTime()));
                    pstmtB.setDate(6, new java.sql.Date(booking.getCheckOutDate().getTime()));
                    pstmtB.setDouble(7, booking.getTotalPrice());
                    pstmtB.setString(8, booking.getStatus());
                    pstmtB.executeUpdate();
                }

                // 4. Update Room Status
                try (PreparedStatement pstmtR = conn.prepareStatement(sqlRoom)) {
                    pstmtR.setInt(1, booking.getRoomId());
                    pstmtR.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBookingStatus(int id, String status) {
        String sql = "UPDATE bookings SET status=? WHERE id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("guest_email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getInt("room_id"),
                rs.getDate("check_in_date"),
                rs.getDate("check_out_date"),
                rs.getDouble("total_price"),
                rs.getString("status"));
    }
}
