package com.upthink.qms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Configuration
public class SecurityConfig {

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(x->x.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/home", "/signup", "/login", "/confirm-signup",
                            "/logout",
                            "/qm/addEssay",
                            "/qm/presignUrl",
                            "/qm/listCredsForClient",
                            "/qm/checkExpiry")
                    .permitAll()
                    .anyRequest().authenticated()
            )
//            .oauth2Login(oauth2 -> oauth2
//                    .userInfoEndpoint(userInfo -> userInfo
//                            .oidcUserService(oidcUserService())
//                    )
//                    .successHandler(this::handleOAuth2Success)
//            )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "*",
                "http://localhost:5173"
//                "https://qms-upthink.vercel.app",
//                "https://main.d2xuveirwkpf26.amplifyapp.com"
        )); // Allow frontend origin http://localhost:5173
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allow necessary headers
//        configuration.setAllowCredentials(true); // Allow credentials (cookies, headers)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS to all endpoints
        return source;
    }



//    @Bean
//    public OidcUserService oidcUserService() {
//        return new OidcUserService() {
//            @Override
//            public OidcUser loadUser(OidcUserRequest userRequest) {
//                // Load the OIDC user from the request
//                OidcUser oidcUser = super.loadUser(userRequest);
//
//                // Extract the Access Token
//                String accessToken = userRequest.getAccessToken().getTokenValue();
//
//                // Optionally, store the access token in the user's attributes
//                Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
//                attributes.put("access_token", accessToken);
//
//                // Return a new OidcUser with the additional attributes
//                return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
//            }
//        };
//    }


//    private void handleOAuth2Success(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        // Get the authenticated OIDC user
//        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//
//        // Extract the JWT token (ID Token) from the OIDC user
//        String jwtToken = oidcUser.getIdToken().getTokenValue();
//
//        // Extract the Access Token using OAuth2AuthorizedClientService
//        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
//        String clientRegistrationId = oauth2Token.getAuthorizedClientRegistrationId();
//
//        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
//                clientRegistrationId,
//                oauth2Token.getName()
//        );
//
//        String accessToken = null;
//        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
//            accessToken = authorizedClient.getAccessToken().getTokenValue();
//        }
//
//        // Extract the roles (groups) from the token
//        List<String> roles = oidcUser.getClaimAsStringList("cognito:groups");
//
//        // Extract the email from the token
//        String email = oidcUser.getClaimAsString("email");
//
//        // Print the JWT token to the console
//        System.out.println("JWT Token (ID Token) from Cognito: " + jwtToken);
//
//        // Print the Access Token to the console
//        System.out.println("Access Token from Cognito: " + accessToken);
//
//        // Print the roles (groups) to the console
//        System.out.println("Roles (Groups) from Cognito: " + roles);
//
//        // Print the email to the console
//        System.out.println("Email from Cognito: " + email);
//
//        // Optionally, send the tokens back in the response for testing
//        response.setContentType("text/plain");
//        response.getWriter().write("Email: " + email + "\n"
//                + "JWT Token (ID Token): " + jwtToken + "\n"
//                + "Access Token: " + accessToken + "\n"
//                + "Roles: " + roles);
//    }
}
