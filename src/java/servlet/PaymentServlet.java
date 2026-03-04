package servlet;

import com.google.gson.Gson;
import dao.PaymentDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Payment;

@WebServlet(name = "PaymentServlet", urlPatterns = { "/api/payments" })
public class PaymentServlet extends HttpServlet {
    private PaymentDAO paymentDAO = new PaymentDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String query = request.getParameter("search");
        String idStr = request.getParameter("id");

        if (idStr != null) {
            int id = Integer.parseInt(idStr);
            Payment payment = paymentDAO.getPaymentById(id);
            response.getWriter().print(gson.toJson(payment));
        } else if (query != null && !query.trim().isEmpty()) {
            List<Payment> payments = paymentDAO.searchPayments(query);
            response.getWriter().print(gson.toJson(payments));
        } else {
            List<Payment> payments = paymentDAO.getAllPayments();
            response.getWriter().print(gson.toJson(payments));
        }
    }
}
