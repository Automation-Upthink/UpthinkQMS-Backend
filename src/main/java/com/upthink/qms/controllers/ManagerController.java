package com.upthink.qms.controllers;

import com.upthink.qms.domain.Person;
import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ManagerController {

    @Autowired
    AuthService authService;

    @Autowired
    PersonService personService; // Assuming you have a PersonService to interact with the database

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;


    @GetMapping("/listUsers")
    public String listCognitoUsers() {
        // Get the current authentication object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user has the "MANAGER" role
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));

        if (!isManager) {
            // If the user is not a manager, return an error message
            return "You do not have permission to access this resource.";
        }

        // If the user is a manager, list the Cognito users
        ListUsersResponse response = authService.listAllCognitoUsers();

        // Compare the Cognito users' subs against the database
        List<String> cognitoSubs = response.users().stream()
                .map(user -> user.attributes().stream()
                        .filter(attr -> attr.name().equals("sub"))
                        .map(attr -> attr.value())
                        .findFirst()
                        .orElse(null))
                .collect(Collectors.toList());

        List<Person> peopleInDatabase = personService.findAllByCognitoIds(cognitoSubs); // Assuming findAllByCognitoSubIn is a method in PersonService

        StringBuilder result = new StringBuilder("Cognito Users:\n");
        System.out.println("People in DB: " + peopleInDatabase);
        for (UserType user : response.users()) {
            String sub = user.attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .map(attr -> attr.value())
                    .findFirst()
                    .orElse("Unknown");

            boolean existsInDatabase = peopleInDatabase.stream()
                    .anyMatch(person -> person.getCognitoId().equals(sub));

            result.append("User Sub: ").append(sub).append(" - Exists in DB: ").append(existsInDatabase).append("\n");
        }

        return result.toString();
    }
}
