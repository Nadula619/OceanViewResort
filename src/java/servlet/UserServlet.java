package servlet;

import com.google.gson.Gson;
import dao.UserDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

@WebServlet(name = "UserServlet", urlPatterns = { "/api/users" })
public class UserServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String email = request.getParameter("email");

        if (email != null && !email.isEmpty()) {
            User user = userDAO.getUserByEmail(email);
            if (user != null) {
                response.getWriter().print(gson.toJson(user));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"message\": \"User not found\"}");
            }
        } else {
            response.getWriter().print(gson.toJson(userDAO.getAllUsers()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        try {
            User user = gson.fromJson(request.getReader(), User.class);
            if (userDAO.updateUser(user)) {
                response.getWriter().print("{\"status\": \"success\", \"message\": \"Guest updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"status\": \"error\", \"message\": \"Failed to update guest\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
