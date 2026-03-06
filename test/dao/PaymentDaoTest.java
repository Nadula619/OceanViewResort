package dao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Date;
import model.Payment;
import model.Booking;
import model.Room;

public class PaymentDaoTest {
    private PaymentDAO paymentDAO;
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;

    @Before
    public void setUp() {
        paymentDAO = new PaymentDAO();
        bookingDAO = new BookingDAO();
        roomDAO = new RoomDAO();
    }

    @Test
    public void testGetAllPayments() {
        System.out.println("getAllPayments");
        List<Payment> result = paymentDAO.getAllPayments();
        assertNotNull("The returned list should not be null", result);
        System.out.println("Total payments found: " + result.size());
    }

    @Test
    public void testPaymentLookupAndSearch() throws Exception {
        System.out.println("payment lookup and search");

        // 1. Create a booking to generate a payment (Payments are auto-generated in
        // BookingDAO.addBooking)
        List<Room> rooms = roomDAO.getAllRooms();
        if (rooms.isEmpty()) {
            roomDAO.addRoom(new Room(0, "P1", "Deluxe Single", 100.00, "AVAILABLE", "Payment test", null));
            rooms = roomDAO.getAllRooms();
        }
        Room r = rooms.get(0);

        String uniqueEmail = "paytest_" + System.currentTimeMillis() + "@example.com";
        Booking b = new Booking(0, "Pay", "Tester", uniqueEmail, "000", "Addr", r.getId(), new Date(), new Date(),
                100.0, "CONFIRMED");

        bookingDAO.addBooking(b);

        // 2. Find the generated payment
        List<Payment> allPayments = paymentDAO.getAllPayments();
        Payment found = null;
        for (Payment p : allPayments) {
            if ("Pay Tester".equals(p.getGuestName())) {
                found = p;
                break;
            }
        }

        assertNotNull("A payment should have been created for the new booking", found);

        // 3. Test getPaymentById
        Payment byId = paymentDAO.getPaymentById(found.getId());
        assertNotNull("Payment should be retrievable by ID", byId);
        assertEquals("Transaction IDs should match", found.getTransactionId(), byId.getTransactionId());

        // 4. Test searchPayments
        List<Payment> searchResult = paymentDAO.searchPayments(found.getTransactionId());
        assertFalse("Search result should not be empty", searchResult.isEmpty());
        assertEquals("Search should find the correct payment", found.getId(), searchResult.get(0).getId());

        searchResult = paymentDAO.searchPayments("Pay Tester");
        assertFalse("Search by guest name should not be empty", searchResult.isEmpty());
    }
}
