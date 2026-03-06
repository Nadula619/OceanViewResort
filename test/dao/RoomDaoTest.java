package dao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import model.Room;

public class RoomDaoTest {
    private RoomDAO roomDAO;

    @Before
    public void setUp() {
        roomDAO = new RoomDAO();
    }

    @Test
    public void testGetAllRooms() {
        System.out.println("getAllRooms");
        List<Room> result = roomDAO.getAllRooms();
        assertNotNull("The returned list should not be null", result);
        System.out.println("Total rooms found: " + result.size());
    }

    @Test
    public void testRoomLifecycle() {
        System.out.println("room lifecycle: add, exist-check, update, delete");

        String roomNum = "T-" + (System.currentTimeMillis() % 10000);
        Room room = new Room(0, roomNum, "Ocean View Suite", 450.00, "AVAILABLE", "Luxury test suite", "test.jpg");

        // Test addRoom
        boolean added = roomDAO.addRoom(room);
        assertTrue("Room should be added successfully", added);

        // Test isRoomNumberExists
        assertTrue("Room number should exist", roomDAO.isRoomNumberExists(roomNum));

        // Find the room to get its ID
        List<Room> rooms = roomDAO.getAllRooms();
        Room found = null;
        for (Room r : rooms) {
            if (roomNum.equals(r.getRoomNumber())) {
                found = r;
                break;
            }
        }
        assertNotNull("Added room should be in the list", found);

        // Test updateRoom
        found.setPricePerNight(500.00);
        found.setStatus("MAINTENANCE");
        boolean updated = roomDAO.updateRoom(found);
        assertTrue("Room update should be successful", updated);

        // Verify update
        rooms = roomDAO.getAllRooms();
        Room verified = null;
        for (Room r : rooms) {
            if (found.getId() == r.getId()) {
                verified = r;
                break;
            }
        }
        assertEquals("Price should be updated", 500.00, verified.getPricePerNight(), 0.01);
        assertEquals("Status should be updated", "MAINTENANCE", verified.getStatus());

        // Test deleteRoom
        boolean deleted = roomDAO.deleteRoom(verified.getId());
        assertTrue("Room deletion should be successful", deleted);

        assertFalse("Room number should no longer exist", roomDAO.isRoomNumberExists(roomNum));
    }
}
