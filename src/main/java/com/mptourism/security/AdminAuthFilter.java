package com.mptourism.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminAuthFilter extends OncePerRequestFilter {
    
    @Value("${admin.secret.key}")
    private String adminKey;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

            if (request.getRequestURI().startsWith("/api/admin")) {

                String requestKey = request.getHeader("X-ADMIN-KEY");

                if (requestKey == null || !requestKey.equals(adminKey)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized Admin Access");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }
}
