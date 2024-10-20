package com.upthink.qms.controllers;

//import com.upthink.qms.service.AuthService;
import com.upthink.qms.domain.Person;
import com.upthink.qms.domain.PersonPrincipal;
import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.CustomUserDetailsService;
import com.upthink.qms.service.JwtService;
import com.upthink.qms.service.PersonService;
import com.upthink.qms.service.request.AuthenticatedRequest;
import com.upthink.qms.service.request.TokenValidation;
import com.upthink.qms.service.response.BaseResponse;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class HomeController {

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    AuthService authService;

    @Autowired
    JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    PersonService personService;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    private Authentication authentication;


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

    

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/signup";
    }


    @PostMapping("/addPersonToDatabase")
    public BaseResponse addUserToPersonDb(AuthenticatedRequest request) {
        // Extracting the necessary data from the request, assuming it contains the user details
        String email = request.getId().getEmail();
        String cognitoId = request.getId().getSub();
        String name = request.getId().getName();
        List<String> groups = request.getId().getGroups(); // Groups user is associated with

        // Check if the user already exists by email or cognitoId
        Optional<Person> existingPersonByCognitoId = Optional.ofNullable(personService.findByCognitoId(cognitoId));

        if (existingPersonByCognitoId.isPresent()) {
            // Return response if the person already exists
            return new BaseResponse(false, "Person already exists");
        }

        // Create a new Person object and set its properties
        Person newPerson = new Person(
                cognitoId,  // Generate a unique ID
                cognitoId,
                name,
                email,
                groups,
                true // Assuming the person is active by default
        );

        // Save the new person to the database
        personService.savePerson(newPerson);

        // Return a success response
        return new BaseResponse(true,"Person added successfully");
    }



    @PostMapping("/checkActiveStatus")
    public BaseResponse checkUserStatus(AuthenticatedRequest request){
        try{
            personService.findByCognitoId(request.getId().getSub());
            return new BaseResponse(true, "Welcome valuable user");
        } catch (Exception e) {
            return new BaseResponse(false, "You have been made inactive by the manager");
        }
    }




}
