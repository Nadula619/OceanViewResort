package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Room;
import util.DBConnection;

public class RoomDAO {

    public boolean isRoomNumberExists(String roomNumber) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isRoomNumberExists(String roomNumber, int excludeId) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ? AND id != ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNumber);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DBConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, status, description, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setDouble(3, room.getPricePerNight());
            pstmt.setString(4, room.getStatus());
            pstmt.setString(5, room.getDescription());
            pstmt.setString(6, room.getImageUrl());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, price_per_night=?, status=?, description=?, image_url=? WHERE id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setDouble(3, room.getPricePerNight());
            pstmt.setString(4, room.getStatus());
            pstmt.setString(5, room.getDescription());
            pstmt.setString(6, room.getImageUrl());
            pstmt.setInt(7, room.getId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoom(int id) {
        String sql = "DELETE FROM rooms WHERE id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("room_number"),
                rs.getString("room_type"),
                rs.getDouble("price_per_night"),
                rs.getString("status"),
                rs.getString("description"),
                rs.getString("image_url"));
    }
}
