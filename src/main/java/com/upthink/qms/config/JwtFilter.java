package com.upthink.qms.config;

import com.upthink.qms.service.CustomUserDetailsService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.request.TokenValidation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;


import com.upthink.qms.service.CustomUserDetailsService;
import com.upthink.qms.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

//        // Exclude paths from JWT filtering
//        String requestURI = request.getRequestURI();
//        if (requestURI.startsWith("/signup") || requestURI.startsWith("/login") || requestURI.startsWith("/confirm-signup")) {
//            System.out.println("Skipping JWT filter for URL: " + requestURI);
//            filterChain.doFilter(request, response);
//            return;  // Skip JWT processing for public paths
//        }

        String bearerToken = extractJwtFromRequest(request);

        if (bearerToken != null) {
            System.out.println("Received JWT: " + bearerToken); // Log the token being processed

            String cognitoSub = jwtService.extractCognitoSub(bearerToken);
            System.out.println("Extracted Cognito sub: " + cognitoSub);
            System.out.println("Context: "+SecurityContextHolder.getContext().getAuthentication());
            if (cognitoSub != null) {
                Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Existing auth : " + existingAuth);
                if (existingAuth == null || existingAuth instanceof OAuth2AuthenticationToken) {
                    // Load user details from the JWT
                    UserDetails userDetails = applicationContext
                            .getBean(CustomUserDetailsService.class)
                            .loadUserByUsername(cognitoSub);

                    if (jwtService.validateAuthToken(bearerToken, userDetails)) {
                        List<String> roles = jwtService.extractGroups(bearerToken);
                        List<GrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());

                        System.out.println("Extracted Roles from JWT: " + roles);
                        System.out.println("Extracted Authorities from JWT: " + authorities);

                        // Merge with existing authorities
                        if (existingAuth != null && existingAuth.getAuthorities() != null) {
                            authorities.addAll(existingAuth.getAuthorities());
                        }

                        // Create new authentication token with merged authorities
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set the authentication in the SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                        System.out.println("Updated SecurityContext with authorities: " + authorities);
                    } else {
                        System.out.println("JWT validation failed.");
                    }
                } else {
                    // Log existing authentication if it's not OAuth2 or if it already contains the correct authorities
                    System.out.println("Existing authentication found: " + existingAuth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
