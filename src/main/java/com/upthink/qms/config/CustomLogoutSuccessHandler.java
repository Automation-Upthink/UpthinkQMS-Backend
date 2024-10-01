package com.upthink.qms.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // Invalidate session and clear security context
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear JWT cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Ensure this matches your application's protocol
        cookie.setPath("/");
        cookie.setMaxAge(0); // Set the cookie's max age to 0 to delete it
        response.addCookie(cookie);
        // URL encode the redirect URI
        String encodedRedirectUri = URLEncoder.encode("http://localhost:8080/logout-success", StandardCharsets.UTF_8.toString());
        System.out.println("Redirect url : " + encodedRedirectUri);
        String logoutUrl = "https://queue-manager.auth.ap-south-1.amazoncognito.com/logout?client_id=7hssrapn8l03o8tf5gcts6lr1m&logout_uri=" + encodedRedirectUri;

        // Redirect to a neutral page after logout
        response.sendRedirect(logoutUrl);
    }
}

