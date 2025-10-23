package com.nmsCinemas.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.nmsCinemas.models.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Allow public access to home, login, and user registration
        if (uri.equals("/") || uri.equals("/home") || uri.equals("/login") ||
            (uri.equals("/users") && method.equals("POST"))) {
            return true;
        }

        // Allow logout
        if (uri.equals("/logout")) {
            return true;
        }

        // Check if user is logged in
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect("/?error=Please login first");
            return false;
        }

        User user = (User) session.getAttribute("loggedInUser");
        String role = user.getRole().name();

        // Admin-only paths: Create, Edit, Delete operations
        boolean isAdminPath =
            (uri.startsWith("/movies/") && (uri.contains("/edit") || uri.contains("/delete"))) ||
            (uri.startsWith("/theatres/") && (uri.contains("/edit") || uri.contains("/delete"))) ||
            (uri.startsWith("/shows/") && (uri.contains("/edit") || uri.contains("/delete"))) ||
            (uri.equals("/movies/new") || uri.equals("/theatres/new") || uri.equals("/shows/new")) ||
            (uri.equals("/movies") && method.equals("POST")) ||
            (uri.equals("/theatres") && method.equals("POST")) ||
            (uri.equals("/shows") && method.equals("POST")) ||
            (uri.matches("/movies/\\d+") && method.equals("POST")) ||
            (uri.matches("/theatres/\\d+") && method.equals("POST")) ||
            (uri.matches("/shows/.+") && method.equals("POST")) ||
            (uri.startsWith("/users") && !uri.equals("/users/" + user.getIdusers() + "/edit"));

        if (isAdminPath && !role.equals("ADMIN")) {
            response.sendRedirect("/user/dashboard?error=Admin access required");
            return false;
        }

        return true;
    }
}