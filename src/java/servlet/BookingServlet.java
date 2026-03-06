package servlet;

import com.google.gson.Gson;
import dao.BookingDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Booking;

@WebServlet(name = "BookingServlet", urlPatterns = { "/api/bookings" })
public class BookingServlet extends HttpServlet {
    private BookingDAO bookingDAO = new BookingDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        List<Booking> bookings = bookingDAO.getAllBookings();
        response.getWriter().print(gson.toJson(bookings));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        boolean success = false;
        String message = "";

        try {
            if ("add".equals(action)) {
                Booking booking = new Booking();
                booking.setFirstName(request.getParameter("first_name"));
                booking.setLastName(request.getParameter("last_name"));
                booking.setEmail(request.getParameter("email"));
                booking.setPhone(request.getParameter("phone"));
                booking.setAddress(request.getParameter("address"));
                booking.setRoomId(Integer.parseInt(request.getParameter("roomId")));

                try {
                    String ci = request.getParameter("checkInDate");
                    String co = request.getParameter("checkOutDate");
                    if (ci == null || ci.isEmpty() || co == null || co.isEmpty()) {
                        throw new Exception("Check-in and Check-out dates are required.");
                    }
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date checkInDate = sdf.parse(ci);
                    java.util.Date checkOutDate = sdf.parse(co);

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    java.util.Date today = cal.getTime();

                    if (checkInDate.before(today)) {
                        throw new Exception("Check-in date cannot be in the past.");
                    }
                    if (checkOutDate.before(checkInDate) || checkOutDate.equals(checkInDate)) {
                        throw new Exception("Check-out date must be after check-in date.");
                    }

                    booking.setCheckInDate(checkInDate);
                    booking.setCheckOutDate(checkOutDate);
                } catch (java.text.ParseException e) {
                    throw new Exception("Invalid date format. Please use YYYY-MM-DD.");
                }

                String priceStr = request.getParameter("totalPrice");
                if (priceStr == null || priceStr.isEmpty()) {
                    throw new Exception("Total price is missing.");
                }
                booking.setTotalPrice(Double.parseDouble(priceStr));
                booking.setStatus("CONFIRMED");

                bookingDAO.addBooking(booking);
                success = true;
                message = "Booking confirmed successfully.";
            } else if ("updateStatus".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                String status = request.getParameter("status");
                success = bookingDAO.updateBookingStatus(id, status);
                message = success ? "Status updated." : "Failed to update status.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            message = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.";
        }

        response.setContentType("application/json");
        response.getWriter()
                .print("{\"success\":" + success + ", \"message\": \"" + message.replace("\"", "\\\"") + "\"}");
    }
}
