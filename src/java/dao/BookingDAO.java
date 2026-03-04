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
        String sql = "SELECT b.*, u.first_name, u.last_name, u.email as guest_email, u.phone, u.address, r.room_number "
                +
                "FROM bookings b " +
                "LEFT JOIN users u ON b.guest_id = u.id " +
                "LEFT JOIN rooms r ON b.room_id = r.id";
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

    public void addBooking(Booking booking) throws Exception {
        String sqlCheckUser = "SELECT id FROM users WHERE email = ?";
        String sqlUser = "INSERT INTO users (first_name, last_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        String sqlBooking = "INSERT INTO bookings (guest_id, customer_name, customer_email, room_id, check_in_date, check_out_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlRoom = "UPDATE rooms SET status='OCCUPIED' WHERE id=?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Process Guest in 'users' table (as requested)
                int guestId = -1;
                String email = booking.getEmail() != null ? booking.getEmail().trim() : "";
                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckUser)) {
                    pstmtCheck.setString(1, email);
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            guestId = rs.getInt("id");
                        }
                    }
                }

                if (guestId == -1) {
                    try (PreparedStatement pstmtU = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                        pstmtU.setString(1, booking.getFirstName());
                        pstmtU.setString(2, booking.getLastName());
                        pstmtU.setString(3, email);
                        pstmtU.setString(4, booking.getPhone());
                        pstmtU.setString(5, booking.getAddress());
                        pstmtU.executeUpdate();

                        try (ResultSet rs = pstmtU.getGeneratedKeys()) {
                            if (rs.next()) {
                                guestId = rs.getInt(1);
                            }
                        }
                    }
                } else {
                    // Update existing user details if they've changed (Optional but good UX)
                    String sqlUpdateUser = "UPDATE users SET first_name=?, last_name=?, phone=?, address=? WHERE id=?";
                    try (PreparedStatement pstmtU = conn.prepareStatement(sqlUpdateUser)) {
                        pstmtU.setString(1, booking.getFirstName());
                        pstmtU.setString(2, booking.getLastName());
                        pstmtU.setString(3, booking.getPhone());
                        pstmtU.setString(4, booking.getAddress());
                        pstmtU.setInt(5, guestId);
                        pstmtU.executeUpdate();
                    }
                }

                // 2. Create Booking (with guest_id column)
                int bookingId = -1;
                try (PreparedStatement pstmtB = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtB.setInt(1, guestId);
                    pstmtB.setString(2, booking.getFirstName() + " " + booking.getLastName());
                    pstmtB.setString(3, email);
                    pstmtB.setInt(4, booking.getRoomId());
                    pstmtB.setDate(5, new java.sql.Date(booking.getCheckInDate().getTime()));
                    pstmtB.setDate(6, new java.sql.Date(booking.getCheckOutDate().getTime()));
                    pstmtB.setDouble(7, booking.getTotalPrice());
                    pstmtB.setString(8, booking.getStatus());
                    pstmtB.executeUpdate();

                    try (ResultSet rs = pstmtB.getGeneratedKeys()) {
                        if (rs.next()) {
                            bookingId = rs.getInt(1);
                        }
                    }
                }

                // 3. Create Automation Payment Record
                String sqlPayment = "INSERT INTO payments (booking_id, amount, payment_method, transaction_id) VALUES (?, ?, ?, ?)";
                String txId = "TXN-" + System.currentTimeMillis() + "-" + bookingId;
                try (PreparedStatement pstmtP = conn.prepareStatement(sqlPayment)) {
                    pstmtP.setInt(1, bookingId);
                    pstmtP.setDouble(2, booking.getTotalPrice());
                    pstmtP.setString(3, "CASH"); // Default method, can be updated later
                    pstmtP.setString(4, txId);
                    pstmtP.executeUpdate();
                }

                // 4. Update Room Status
                try (PreparedStatement pstmtR = conn.prepareStatement(sqlRoom)) {
                    pstmtR.setInt(1, booking.getRoomId());
                    pstmtR.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
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
        Booking b = new Booking(
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
        try {
            b.setRoomNumber(rs.getString("room_number"));
        } catch (SQLException e) {
            // Optional column, ignore if not joined
        }
        return b;
    }
}
