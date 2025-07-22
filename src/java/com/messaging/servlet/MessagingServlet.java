package com.messaging.servlet;

import com.messaging.util.DatabaseUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/MessagingServlet")
public class MessagingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String userId = (String) request.getSession().getAttribute("userId");
        String error = "";
        String success = "";

        try {
            DatabaseUtil.initializeDatabase();

            if ("createGroup".equals(action)) {
                String groupName = request.getParameter("groupName");
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO groups (name, creator_id) VALUES (?, ?)")) {
                    pstmt.setString(1, groupName);
                    pstmt.setInt(2, Integer.parseInt(userId));
                    pstmt.executeUpdate();
                    success = "Group created successfully";
                }
            } else if ("deleteGroup".equals(action)) {
                String groupId = request.getParameter("groupId");
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM groups WHERE id = ? AND creator_id = ?")) {
                    pstmt.setInt(1, Integer.parseInt(groupId));
                    pstmt.setInt(2, Integer.parseInt(userId));
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        success = "Group deleted successfully";
                    } else {
                        error = "You can only delete groups you created";
                    }
                }
            } else if ("addMember".equals(action)) {
                String groupId = request.getParameter("groupId");
                String memberUsername = request.getParameter("memberUsername");
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt1 = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                     PreparedStatement pstmt2 = conn.prepareStatement("INSERT INTO group_members (group_id, user_id) VALUES (?, ?)")) {
                    pstmt1.setString(1, memberUsername);
                    ResultSet rs = pstmt1.executeQuery();
                    if (rs.next()) {
                        pstmt2.setInt(1, Integer.parseInt(groupId));
                        pstmt2.setInt(2, rs.getInt("id"));
                        pstmt2.executeUpdate();
                        success = "Member added successfully";
                    } else {
                        error = "User not found";
                    }
                }
            } else if ("removeMember".equals(action)) {
                String groupId = request.getParameter("groupId");
                String memberId = request.getParameter("memberId");
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM group_members WHERE group_id = ? AND user_id = ?")) {
                    pstmt.setInt(1, Integer.parseInt(groupId));
                    pstmt.setInt(2, Integer.parseInt(memberId));
                    pstmt.executeUpdate();
                    success = "Member removed successfully";
                }
            } else if ("sendGroupMessage".equals(action)) {
                String groupId = request.getParameter("groupId");
                String message = request.getParameter("message");
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO group_messages (group_id, sender_id, message) VALUES (?, ?, ?)")) {
                    pstmt.setInt(1, Integer.parseInt(groupId));
                    pstmt.setInt(2, Integer.parseInt(userId));
                    pstmt.setString(3, message);
                    pstmt.executeUpdate();
                    success = "Message sent to group";
                }
            } else if ("sendPrivateMessage".equals(action)) {
                String receiverUsername = request.getParameter("receiverUsername");
                String message = request.getParameter("message");
                try (Connection conn = DatabaseUtil.getConnection();
                     PreparedStatement pstmt1 = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                     PreparedStatement pstmt2 = conn.prepareStatement("INSERT INTO private_messages (sender_id, receiver_id, message) VALUES (?, ?, ?)")) {
                    pstmt1.setString(1, receiverUsername);
                    ResultSet rs = pstmt1.executeQuery();
                    if (rs.next()) {
                        pstmt2.setInt(1, Integer.parseInt(userId));
                        pstmt2.setInt(2, rs.getInt("id"));
                        pstmt2.setString(3, message);
                        pstmt2.executeUpdate();
                        success = "Private message sent";
                    } else {
                        error = "Receiver not found";
                    }
                }
            }
        } catch (Exception e) {
            error = "Error: " + e.getMessage();
        }

        request.setAttribute("error", error);
        request.setAttribute("success", success);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }
}