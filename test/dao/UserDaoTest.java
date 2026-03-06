/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import model.User;

public class UserDaoTest {
    private UserDAO userDAO;

    @Before
    public void setUp() {
        userDAO = new UserDAO();
    }

    @Test
    public void testGetAllUsers() {
        System.out.println("getAllUsers");
        List<User> result = userDAO.getAllUsers();
        assertNotNull("The returned list should not be null", result);
        System.out.println("Total users found: " + result.size());
    }

    @Test
    public void testAddUserAndGetByEmail() {
        System.out.println("addUser and getUserByEmail");
        
        // Generate a unique email for testing
        String testEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        User user = new User(0, "Test", "User", testEmail, "1234567890", "123 Test St");
        
        boolean added = userDAO.addUser(user);
        assertTrue("User should be added successfully", added);
        
        User retrieved = userDAO.getUserByEmail(testEmail);
        assertNotNull("Retrieved user should not be null", retrieved);
        assertEquals("Emails should match", testEmail, retrieved.getEmail());
        assertEquals("First name should match", "Test", retrieved.getFirstName());
        assertEquals("Last name should match", "User", retrieved.getLastName());
    }

    @Test
    public void testGetUserByEmailNotFound() {
        System.out.println("getUserByEmail (not found)");
        String nonExistentEmail = "nonexistent_" + System.currentTimeMillis() + "@example.com";
        User result = userDAO.getUserByEmail(nonExistentEmail);
        assertNull("Should return null for non-existent email", result);
    }
}
