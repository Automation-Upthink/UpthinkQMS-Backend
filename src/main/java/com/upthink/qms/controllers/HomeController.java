package com.upthink.qms.controllers;

//import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.CustomUserDetailsService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.request.TokenValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    AuthService authService;

    @Autowired
    JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    private Authentication authentication;


//    @PostMapping("/signup")
//    public ResponseEntity<String> signUp(@RequestParam String email, @RequestParam String password) {
//        try {
//            System.out.println("Signup !!!");
//            // Call AuthService to handle signup
//            authService.registerNewUser(email, password, (List<String>) Collections.singleton("USER"));
//            return ResponseEntity.ok("Sign up successful, please check your email for the confirmation code.");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error during signup: " + e.getMessage());
//        }
//    }

    @PostMapping("/confirm-signup")
    public ResponseEntity<String> confirmSignUp(@RequestParam String email, @RequestParam String confirmationCode) {
        try {
            ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode)
                    .build();

            cognitoClient.confirmSignUp(confirmSignUpRequest); // Send the confirm request to Cognito

            return ResponseEntity.ok("Account confirmed successfully. You can now log in.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error confirming account: " + e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {
        System.out.println("Login!!!");
        String token = authService.handleAuthenticatedUser(authentication);
        // Send token back in the response cookie
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // Set to true if using HTTPS
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body("Login Successful");
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/signup";
    }

//
//    @GetMapping("/home")
//    public String home(Authentication authentication) {
//        return "home";
//    }


    @GetMapping("/home")
    public Map<String, Object> getUserDetails(Authentication authentication) {
        // Get the authenticated user's name
        String username = authentication.getName();
        System.out.println("USER NAME : " + username);
        // Get the user's roles
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        System.out.println("ROLES : " + roles);
        // Create a response map with user details
        return Map.of(
                "email", username,
                "roles", roles
        );
    }


}
