package com.messaging.servlet;

import com.messaging.util.DatabaseUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AuthServlet")
public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            request.getSession().invalidate();
            response.sendRedirect("login.jsp");
            return;
        }
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        String error = "";
        String success = "";

        try {
            DatabaseUtil.initializeDatabase();

            if (null != action) switch (action) {
                case "login":{
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    try (Connection conn = DatabaseUtil.getConnection();
                            PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?")) {
                        pstmt.setString(1, username);
                        pstmt.setString(2, password);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            session.setAttribute("userId", rs.getString("id"));
                            session.setAttribute("username", username);
                            response.sendRedirect("dashboard.jsp");
                            return;
                        } else {
                            error = "Invalid credentials";
                        }
                    }       break;
                    }
                case "register":
                {
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    try (Connection conn = DatabaseUtil.getConnection();
                            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
                        pstmt.setString(1, username);
                        pstmt.setString(2, password);
                        pstmt.executeUpdate();
                        success = "Registration successful. Please login.";
                        request.setAttribute("success", success);
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                        return;
                    } catch (SQLException e) {
                        error = "Registration error: Username may already exist";
                }
            }
            }
        } catch (Exception e) {
            error = "Error: " + e.getMessage();
        }

        request.setAttribute("error", error);
        request.getRequestDispatcher(action.equals("register") ? "register.jsp" : "login.jsp").forward(request, response);
    }
}