package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Staff;
import util.DBConnection;

public class StaffDAO {
    public Staff login(String email, String password) {
        String sql = "SELECT * FROM staff WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Staff(
                            rs.getInt("staff_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("role"),
                            rs.getString("username"),
                            null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<Staff> getAllStaff() {

        java.util.List<Staff> staffList = new java.util.ArrayList<>();
        String sql = "SELECT * FROM staff";
        try (Connection conn = DBConnection.getInstance().getConnection();
                java.sql.Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("username"),
                        null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return staffList;
    }

    public boolean addStaff(Staff staff) {
        String sql = "INSERT INTO staff (first_name, last_name, email, phone, role, username, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getEmail());
            pstmt.setString(4, staff.getPhone());
            pstmt.setString(5, staff.getRole());
            pstmt.setString(6, staff.getUsername());
            pstmt.setString(7, staff.getPassword());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStaff(Staff staff) {
        StringBuilder sql = new StringBuilder(
                "UPDATE staff SET first_name=?, last_name=?, email=?, phone=?, role=?, username=?");
        boolean updatePassword = staff.getPassword() != null && !staff.getPassword().isEmpty();
        if (updatePassword)
            sql.append(", password=?");
        sql.append(" WHERE staff_id=?");

        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getEmail());
            pstmt.setString(4, staff.getPhone());
            pstmt.setString(5, staff.getRole());
            pstmt.setString(6, staff.getUsername());
            int paramIndex = 7;
            if (updatePassword) {
                pstmt.setString(paramIndex++, staff.getPassword());
            }
            pstmt.setInt(paramIndex, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStaff(int id) {
        String sql = "DELETE FROM staff WHERE staff_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
