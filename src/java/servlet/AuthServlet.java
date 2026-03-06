package servlet;

import com.google.gson.Gson;
import dao.StaffDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Staff;

@WebServlet(name = "AuthServlet", urlPatterns = { "/api/auth" })
public class AuthServlet extends HttpServlet {
    private StaffDAO staffDAO = new StaffDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        String action = request.getParameter("action");

        if ("login".equals(action)) {
            String email = request.getParameter("email");
            String pass = request.getParameter("password");

            Staff authenticatedStaff = staffDAO.login(email, pass);
            if (authenticatedStaff != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", authenticatedStaff);
                result.put("success", true);
                result.put("role", authenticatedStaff.getRole());
                result.put("name", authenticatedStaff.getFullName());
                result.put("staffId", authenticatedStaff.getStaffId());
            } else {
                result.put("success", false);
                result.put("message", "Invalid username or password");
            }
        } else if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            result.put("success", true);
        }

        out.print(gson.toJson(result));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        Staff staff = (session != null) ? (Staff) session.getAttribute("user") : null;

        Map<String, Object> result = new HashMap<>();
        if (staff != null) {
            result.put("loggedIn", true);
            result.put("role", staff.getRole());
            result.put("name", staff.getFullName());
            result.put("staffId", staff.getStaffId());
        } else {
            result.put("loggedIn", false);
        }
        out.print(gson.toJson(result));
    }
}
