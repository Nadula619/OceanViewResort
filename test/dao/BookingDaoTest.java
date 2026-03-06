package dao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Date;
import model.Booking;
import model.Room;

public class BookingDaoTest {
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;

    @Before
    public void setUp() {
        bookingDAO = new BookingDAO();
        roomDAO = new RoomDAO();
    }

    @Test
    public void testGetAllBookings() {
        System.out.println("getAllBookings");
        List<Booking> result = bookingDAO.getAllBookings();
        assertNotNull("The returned list should not be null", result);
        System.out.println("Total bookings found: " + result.size());
    }

    @Test
    public void testAddBookingAndStatusUpdate() throws Exception {
        System.out.println("addBooking and updateBookingStatus");

        // Ensure we have a room for testing
        List<Room> rooms = roomDAO.getAllRooms();
        if (rooms.isEmpty()) {
            Room newRoom = new Room(0, "T99", "Deluxe Single", 100.00, "AVAILABLE", "Test Room", null);
            roomDAO.addRoom(newRoom);
            rooms = roomDAO.getAllRooms();
        }
        Room testRoom = rooms.get(0);

        // Create a test booking
        String testEmail = "booktest_" + System.currentTimeMillis() + "@example.com";
        Date checkIn = new Date();
        Date checkOut = new Date(checkIn.getTime() + (24 * 60 * 60 * 1000)); // +1 day

        Booking booking = new Booking(
                0,
                "Test",
                "Guest",
                testEmail,
                "071-1234567",
                "123 Test Street",
                testRoom.getId(),
                checkIn,
                checkOut,
                testRoom.getPricePerNight(),
                "CONFIRMED");

        // Test addBooking
        try {
            bookingDAO.addBooking(booking);
            System.out.println("Booking added successfully for: " + testEmail);
        } catch (Exception e) {
            fail("addBooking threw an exception: " + e.getMessage());
        }

        // Verify it was added by finding it in the list
        List<Booking> allBookings = bookingDAO.getAllBookings();
        Booking found = null;
        for (Booking b : allBookings) {
            if (testEmail.equals(b.getEmail())) {
                found = b;
                break;
            }
        }

        assertNotNull("Added booking should be found in the list", found);
        assertEquals("Status should be CONFIRMED", "CONFIRMED", found.getStatus());

        // Test updateBookingStatus
        boolean statusUpdated = bookingDAO.updateBookingStatus(found.getId(), "CHECKED_IN");
        assertTrue("Status update should be successful", statusUpdated);

        // Re-verify status
        allBookings = bookingDAO.getAllBookings();
        Booking updated = null;
        for (Booking b : allBookings) {
            if (found.getId() == b.getId()) {
                updated = b;
                break;
            }
        }
        assertNotNull("Updated booking should still exist", updated);
        assertEquals("Status should be updated to CHECKED_IN", "CHECKED_IN", updated.getStatus());
    }
}
