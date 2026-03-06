package dao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import model.Staff;

public class StaffDaoTest {
    private StaffDAO staffDAO;

    @Before
    public void setUp() {
        staffDAO = new StaffDAO();
    }

    @Test
    public void testGetAllStaff() {
        System.out.println("getAllStaff");
        List<Staff> result = staffDAO.getAllStaff();
        assertNotNull("The returned list should not be null", result);
        System.out.println("Total staff found: " + result.size());
    }

    @Test
    public void testAddUpdateAndDeleteStaff() {
        System.out.println("add, update, and delete staff");

        String testUser = "teststaff_" + System.currentTimeMillis();
        String testEmail = testUser + "@oceanview.com";

        Staff staff = new Staff(0, "Test", "Staff", testEmail, "077-1234567", "RECEPTIONIST", testUser, "staff123");

        // Test addStaff
        boolean added = staffDAO.addStaff(staff);
        assertTrue("Staff should be added successfully", added);

        // Test login (dual mechanism check)
        Staff loggedInByUsername = staffDAO.login(testUser, "staff123");
        assertNotNull("Login by username should work", loggedInByUsername);
        assertEquals("Emails should match", testEmail, loggedInByUsername.getEmail());

        Staff loggedInByEmail = staffDAO.login(testEmail, "staff123");
        assertNotNull("Login by email should work", loggedInByEmail);
        assertEquals("Usernames should match", testUser, loggedInByEmail.getUsername());

        // Test updateStaff
        Staff toUpdate = loggedInByUsername;
        toUpdate.setFirstName("Updated");
        toUpdate.setPassword("newpass123");
        boolean updated = staffDAO.updateStaff(toUpdate);
        assertTrue("Staff update should be successful", updated);

        Staff verifiedUpdate = staffDAO.login(testUser, "newpass123");
        assertNotNull("Login with new password should work", verifiedUpdate);
        assertEquals("First name should be updated", "Updated", verifiedUpdate.getFirstName());

        // Test deleteStaff
        boolean deleted = staffDAO.deleteStaff(verifiedUpdate.getStaffId());
        assertTrue("Staff deletion should be successful", deleted);

        Staff verifiedDelete = staffDAO.login(testUser, "newpass123");
        assertNull("Deleted staff should not be able to login", verifiedDelete);
    }
}
