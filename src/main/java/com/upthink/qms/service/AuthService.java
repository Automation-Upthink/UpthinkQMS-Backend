package com.upthink.qms.service;

import com.upthink.qms.domain.Person;
import com.upthink.qms.repository.PersonRepository;
import com.upthink.qms.service.request.TokenValidation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final PersonService personService;
    private final JwtService jwtService;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    public AuthService(CognitoIdentityProviderClient cognitoIdentityProviderClient, PersonService personService, JwtService jwtService) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.personService = personService;
        this.jwtService = jwtService;
    }

    /**
     * Checks if a user is registered either in the local database or in Cognito.
     * @param cognitoSub The Cognito sub.
     * @return True if the user is registered, false otherwise.
     */
    public boolean isUserRegistered(String cognitoSub) {
        // Check the local database
        Person person = personService.findByCognitoId(cognitoSub);
        if (person != null) {
            return true; // User found in the local database
        }

        // Check Cognito
        return isUserRegisteredInCognito(cognitoSub);
    }

    /**
     * Checks if a user is registered in Cognito.
     * @param cognitoSub The Cognito sub.
     * @return True if the user is registered in Cognito, false otherwise.
     */
    private boolean isUserRegisteredInCognito(String cognitoSub) {

        AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(cognitoSub)
                .build();
        System.out.println("Comes here");
         AdminGetUserResponse userResponse = cognitoIdentityProviderClient.adminGetUser(getUserRequest); // If user does not exist, an exception will be thrown
        System.out.println(userResponse);
        return true;

    }

    /**
     * Registers a new user locally if they are found in Cognito but not in the local database.
     * @param cognitoSub The Cognito sub.
     * @param email The user's email.
     * @param userGroups The user's groups.
     */
    public void registerNewUser(String cognitoUsername,
                                String cognitoSub, String email,
                                List<String> userGroups) {
        // Ensure the user is registered in Cognito
        System.out.println("Registering new user..............");
        System.out.println("Cognito Sub for new registered user : " + cognitoSub);
//        if (!isUserRegisteredInCognito(cognitoSub)) {
//            throw new IllegalStateException("Cannot register new user; user not found in Cognito.");
//        }
        System.out.println("REGISTER NEW USER");
        // Add the user to a Cognito group and save the user in the local database

        addUserToGroup(cognitoUsername, "USER");
        savePersonToDatabase(cognitoSub, email, "USER");
    }

    private void savePersonToDatabase(String cognitoSub, String email, String userGroup) {
        Person person = new Person(
                cognitoSub,
                cognitoSub,
                email, // Assuming email as name for simplicity
                email,
                Collections.singletonList(userGroup),
                true // Active status
        );

        personService.savePerson(person);
    }

    private void addUserToGroup(String cognitoUsername, String userGroup) {
        System.out.println("Addd user to group...........");
        System.out.println(cognitoUsername);
        try {
            AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoUsername)
                    .groupName(userGroup)
                    .build();

            cognitoIdentityProviderClient.adminAddUserToGroup(addUserToGroupRequest);
        } catch (CognitoIdentityProviderException e) {
            System.err.println("Error adding user to group: " + e.awsErrorDetails().errorMessage());
            System.err.println("Error code: " + e.awsErrorDetails().errorCode());
            System.err.println("Request ID: " + e.requestId());
            throw new RuntimeException("Error adding user to group: " + e.awsErrorDetails().errorMessage(), e);
        }

    }


    /**
     * Handles the user authentication after login.
     * @param authentication The authentication object.
     * @return A JWT token for the authenticated user.
     */
    public String handleAuthenticatedUser(Authentication authentication) {

        String cognitoSub = getCognitoSub(authentication);
        System.out.println("Cognito sub handle authentication " + cognitoSub);
        // Check if the user exists in the local database
        Person person = personService.findByCognitoId(cognitoSub);

        if (person == null) {
            // If the user is not found locally, register them
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String username = (String) oidcUser.getAttributes().get("cognito:username");
            System.out.println("COMES HERE IN HANDLING AUTHENTICATION");
            registerNewUser(username, cognitoSub, oidcUser.getEmail(), List.of("USER"));
            // Fetch user groups from Cognito
            List<String> userGroups = getUserGroupsFromCognito(cognitoSub);
            // Generate a JWT token with username and email
            return jwtService.generateToken(cognitoSub, oidcUser.getEmail(), List.of("USER"));
        }

        // Fetch user groups from Cognito
        List<String> userGroups = getUserGroupsFromCognito(cognitoSub);
        // Generate a JWT token with username and email
        return jwtService.generateToken(cognitoSub, person.getEmail(), userGroups);
    }


//    public TokenValidation handleAuthenticatedUser(Authentication authentication) {
//
//        String cognitoSub = getCognitoSub(authentication);
//        System.out.println("Cognito sub handle authentication " + cognitoSub);
//
//        Person person = personService.findByCognitoId(cognitoSub);
//
//        TokenValidation tokenValidation;
//        if (person == null) {
//            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//            String username = (String) oidcUser.getAttributes().get("cognito:username");
//            System.out.println("COMES HERE IN HANDLING AUTHENTICATION");
//            registerNewUser(username, cognitoSub, oidcUser.getEmail(), List.of("USER"));
//
//            List<String> userGroups = getUserGroupsFromCognito(cognitoSub);
//
//            String jwtToken = jwtService.generateToken(cognitoSub, oidcUser.getEmail(), userGroups);
//
//            tokenValidation = new TokenValidation(
//                    true,
//                    cognitoSub,
//                    userGroups,
//                    oidcUser.getFullName(),
//                    oidcUser.getEmail(),
//                    (String) oidcUser.getAttributes().get("phone_number"),
//                    username,
//                    null, // No validation errors
//                    Map.of(), // Custom attributes can be added here if any
//                    jwtToken // Include the generated JWT token
//            );
//
//        } else {
//            List<String> userGroups = getUserGroupsFromCognito(cognitoSub);
//
//            String jwtToken = jwtService.generateToken(cognitoSub, person.getEmail(), userGroups);
//
//            tokenValidation = new TokenValidation(
//                    true,
//                    cognitoSub,
//                    userGroups,
//                    person.getName(),
//                    person.getEmail(),
//                    null,
//                    null,
//                    null, // No validation errors
//                    Map.of(), // Custom attributes can be added here if any
//                    jwtToken // Include the generated JWT token
//            );
//        }
//
//        return tokenValidation; // Return the TokenValidation object
//    }

    public List<String> getUserGroupsFromCognito(String cognitoSub) {
        AdminListGroupsForUserRequest listGroupsRequest = AdminListGroupsForUserRequest.builder()
                .userPoolId(userPoolId)
                .username(cognitoSub)
                .build();

        AdminListGroupsForUserResponse listGroupsResponse = cognitoIdentityProviderClient.adminListGroupsForUser(listGroupsRequest);

        return listGroupsResponse.groups().stream()
                .map(GroupType::groupName)
                .collect(Collectors.toList());
    }

    private String getCognitoSub(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser.getSubject();
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new IllegalStateException("User not authenticated via OIDC or JWT.");
    }

    public ListUsersResponse listAllCognitoUsers() {
        ListUsersRequest request = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .build();

        return cognitoIdentityProviderClient.listUsers(request);
    }
}
