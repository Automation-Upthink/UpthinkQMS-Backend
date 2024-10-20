package com.upthink.qms.config;

import com.upthink.qms.domain.PersonPrincipal;
import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.CustomUserDetailsService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.request.TokenValidation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthService authService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get the request URI
        String requestURI = request.getRequestURI();
        // Define URIs that do not require JWT authentication
        List<String> bypassURIs = Arrays.asList(
                "/qm/listCredsForClient",
                "/qm/addEssay",
                "/qm/presignUrl",
                "/qm/checkExpiry"
        );

        if (bypassURIs.contains(requestURI)) {
            System.out.println("Bypassing JWT authentication for URI: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token from the request
        String jwtToken = extractJwtFromRequest(request);

        if (jwtToken != null) {
            System.out.println("JWT token found, proceeding with authentication");
            try {
                // Determine which token validation method to use
                TokenValidation tokenValidation;
                if (requestURI.equals("/addPersonToDatabase")) {
                    tokenValidation = authService.validateTokenAndGetTokenValidationFromId(jwtToken);
                } else {
                    tokenValidation = authService.validateTokenAndGetTokenValidation(jwtToken);
                }

                System.out.println("Token validation result: " + tokenValidation.isValidated());

                if (tokenValidation.isValidated()) {
                    UserDetails userDetails;
                    if (requestURI.equals("/addPersonToDatabase")) {
                        userDetails = createTemporaryUserDetails(tokenValidation);
                    } else {
                        try {
                            userDetails = customUserDetailsService.loadUserByUsername(tokenValidation.getUserName());
                        } catch (Exception exception) {
                            System.out.println("User not found in database: " + exception.getMessage());
                            return;
                        }
                    }
                    // Set up Spring Security's authentication context with the user details
                    setAuthenticationInContext(userDetails, tokenValidation, request);
                    System.out.println("Authentication set in SecurityContext for URI: " + requestURI);

                    // Continue the filter chain after successful authentication
                    filterChain.doFilter(request, response);
                }
            } catch (Exception e) {
                System.out.println("JWT validation failed: " + e.getMessage());
            }
        }
    }


//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        System.out.println("Request (jwt filer) " + request);
//        // Get the request URIs
//        String requestURI = request.getRequestURI();
//        System.out.println("Request uri (jwt) " + requestURI);
//
//        if (requestURI.contains("/qm/listCredsForClient")
//                || requestURI.equals("/qm/addEssay")
//                || requestURI.equals("/qm/presignUrl")
//                || requestURI.equals("/qm/checkExpiry")
//        ) {
//            System.out.println("Executed??????????????????");
//            filterChain.doFilter(request, response);
//            return;
//        }
//        System.out.println("Request JwtFilter " + request);
//
//        // Extract the JWT token from the request
//        String jwtToken = extractJwtFromRequest(request);
//        System.out.println("*******************************************************************");
//        System.out.println("request uri " + requestURI);
//        if (jwtToken != null) {
//
//            System.out.println("Am i in jwt token auth");
//            try {
//                // Validate the token and get TokenValidation object
//                TokenValidation tokenValidation = authService.validateTokenAndGetTokenValidation(jwtToken);
//                System.out.println("Valid Sreenjay " + tokenValidation.isValidated());
//
//                if (tokenValidation.isValidated()) {
//                    if(requestURI.equals("/addPersonToDatabase")){
//                        tokenValidation = authService.validateTokenAndGetTokenValidationFromId(jwtToken);
//                        UserDetails tempUserDetails = createTemporaryUserDetails(tokenValidation);
//
//                        // Set up Spring Security's authentication context with the temporary user details
//                        setAuthenticationInContext(tempUserDetails, tokenValidation, request);
//                        System.out.println("Not registered " +
//                                SecurityContextHolder.getContext().getAuthentication().getCredentials());
//                        System.out.println("Request (Jwt filter) " + request);
//                        // Continue the filter chain for /addPersonToDatabase
//                        filterChain.doFilter(request, response);
//                        return;
//                    } else {
//                        try {
//                            // Load user details by the username from the token
//                            UserDetails userDetails = customUserDetailsService.loadUserByUsername(tokenValidation.getUserName());
//                            // Set up Spring Security's authentication context with the user details
//                            setAuthenticationInContext(userDetails, tokenValidation, request);
//                        } catch (Exception exception) {
//                            System.out.println("User not found in database: " + exception.getMessage());
//                        }
//                        // Print the security context to verify that the token validation is set correctly
//                        System.out.println("SecurityContext after setting TokenValidation: " + SecurityContextHolder.getContext());
//                        System.out.println("Authentication in SecurityContext: " + SecurityContextHolder.getContext().getAuthentication());
//                        System.out.println("Credentials (TokenValidation) in SecurityContext: " +
//                                SecurityContextHolder.getContext().getAuthentication().getCredentials());
//                    }
//
//                }
//            } catch (Exception e) {
//                System.out.println("JWT validation failed: " + e.getMessage());
//            }
//        }
//
//        // Continue the filter chain
//        filterChain.doFilter(request, response);
//    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void setAuthenticationInContext(UserDetails userDetails, TokenValidation tokenValidation, HttpServletRequest request) {
        // Create an authentication token using the user details and roles
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, tokenValidation, userDetails.getAuthorities());
        // Set the authentication details (WebAuthenticationDetailsSource)
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//        SecurityContextHolder.getContext().setAuthentication(tokenValidation);
    }

    // Manually create a UserDetails object for /addPersonToDatabase request
    private UserDetails createTemporaryUserDetails(TokenValidation tokenValidation) {
        // Use the information from TokenValidation to create a temporary UserDetails

        return new org.springframework.security.core.userdetails.User(
                tokenValidation.getUserName(),
                "", // No password needed in this case
                true, // isAccountNonExpired
                true, // isAccountNonLocked
                true, // isCredentialsNonExpired
                true, // isEnabled
                Collections.emptyList() // No roles or authorities yet
        );
    }
}
