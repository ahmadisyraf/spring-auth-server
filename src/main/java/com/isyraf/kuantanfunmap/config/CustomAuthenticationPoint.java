package com.isyraf.kuantanfunmap.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class CustomAuthenticationPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String json = """
                {
                  "title": "Unauthorized",
                  "status": 401,
                  "description": "%s",
                  "path": "%s",
                  "timestamp": "%s"
                }
                """.formatted(
                authException.getMessage(),
                request.getRequestURI(),
                Instant.now().toString()
        );

        response.getWriter().write(json);
    }
}
