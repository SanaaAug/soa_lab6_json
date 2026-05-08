package com.example.demo;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String userId = req.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
            return;
        }

        try {
            req.setAttribute("userId",   Long.parseLong(userId));
            req.setAttribute("username", req.getHeader("X-Username") != null ? req.getHeader("X-Username") : "");
        } catch (NumberFormatException e) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"Invalid user id\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
