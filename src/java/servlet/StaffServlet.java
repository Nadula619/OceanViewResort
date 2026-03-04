package servlet;

import com.google.gson.Gson;
import dao.StaffDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Staff;

@WebServlet(name = "StaffServlet", urlPatterns = { "/api/staff" })
public class StaffServlet extends HttpServlet {
    private StaffDAO staffDAO = new StaffDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        List<Staff> staffList = staffDAO.getAllStaff();
        response.getWriter().print(gson.toJson(staffList));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        boolean success = false;

        if ("add".equals(action)) {
            Staff staff = new Staff();
            staff.setFirstName(request.getParameter("firstName"));
            staff.setLastName(request.getParameter("lastName"));
            staff.setEmail(request.getParameter("email"));
            staff.setPhone(request.getParameter("phone"));
            staff.setRole(request.getParameter("role"));
            staff.setUsername(request.getParameter("username"));
            staff.setPassword(request.getParameter("password"));
            success = staffDAO.addStaff(staff);
        } else if ("update".equals(action)) {
            Staff staff = new Staff();
            staff.setStaffId(Integer.parseInt(request.getParameter("id")));
            staff.setFirstName(request.getParameter("firstName"));
            staff.setLastName(request.getParameter("lastName"));
            staff.setEmail(request.getParameter("email"));
            staff.setPhone(request.getParameter("phone"));
            staff.setRole(request.getParameter("role"));
            staff.setUsername(request.getParameter("username"));
            staff.setPassword(request.getParameter("password"));
            success = staffDAO.updateStaff(staff);
        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            success = staffDAO.deleteStaff(id);
        }

        response.setContentType("application/json");
        response.getWriter().print("{\"success\":" + success + "}");
    }
}
