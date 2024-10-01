package com.upthink.qms.config;

import com.upthink.qms.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/signup", "/login", "/confirm-signup", "/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                // Load the OIDC user from the request
                return super.loadUser(userRequest);
            }
        };
    }

//    private void handleOAuth2Success(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws java.io.IOException {
//        // Get the authenticated OIDC user
//        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//
//        // Extract the JWT token from the OIDC user
//        String jwtToken = oidcUser.getIdToken().getTokenValue();
//
//        // Extract the roles (groups) from the token
//        List<String> roles = oidcUser.getClaimAsStringList("cognito:groups");
//
//        // Extract the email from the token
//        String email = oidcUser.getClaimAsString("email");
//
//        // Print the JWT token to the console
//        System.out.println("JWT Token from Cognito: " + jwtToken);
//
//        // Print the roles (groups) to the console
//        System.out.println("Roles (Groups) from Cognito: " + roles);
//
//        // Print the email to the console
//        System.out.println("Email from Cognito: " + email);
//
//        // Optionally, send the email back in the response for testing
//        response.getWriter().write("Email: " + email + "\nJWT Token: " + jwtToken + "\nRoles: " + roles);
//    }


}
