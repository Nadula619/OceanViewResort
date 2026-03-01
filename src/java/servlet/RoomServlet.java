package servlet;

import com.google.gson.Gson;
import dao.RoomDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;

@WebServlet(name = "RoomServlet", urlPatterns = {"/api/rooms"})
public class RoomServlet extends HttpServlet {
    private RoomDAO roomDAO = new RoomDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        List<Room> rooms = roomDAO.getAllRooms();
        response.getWriter().print(gson.toJson(rooms));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        boolean success = false;

        if ("add".equals(action)) {
            Room room = new Room();
            room.setRoomNumber(request.getParameter("roomNumber"));
            room.setRoomType(request.getParameter("roomType"));
            room.setPricePerNight(Double.parseDouble(request.getParameter("price")));
            room.setStatus(request.getParameter("status"));
            room.setDescription(request.getParameter("description"));
            success = roomDAO.addRoom(room);
        } else if ("update".equals(action)) {
            Room room = new Room();
            room.setId(Integer.parseInt(request.getParameter("id")));
            room.setRoomNumber(request.getParameter("roomNumber"));
            room.setRoomType(request.getParameter("roomType"));
            room.setPricePerNight(Double.parseDouble(request.getParameter("price")));
            room.setStatus(request.getParameter("status"));
            room.setDescription(request.getParameter("description"));
            success = roomDAO.updateRoom(room);
        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            success = roomDAO.deleteRoom(id);
        }

        response.setContentType("application/json");
        response.getWriter().print("{\"success\":" + success + "}");
    }
}
