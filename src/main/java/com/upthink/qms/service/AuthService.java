package com.upthink.qms.service;

import com.upthink.qms.domain.Person;
import com.upthink.qms.service.request.TokenValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AuthService {

    private final PersonService personService;
    private final JwtService jwtService;

    @Autowired
    public AuthService(PersonService personService, JwtService jwtService) {
        this.personService = personService;
        this.jwtService = jwtService;
    }

    /**
     * Validates the JWT token and returns a TokenValidation object.
     *
     * @param token The JWT token.
     * @return A TokenValidation object containing the user's details and roles.
     * @throws RuntimeException If the token is invalid.
     */
    public TokenValidation validateTokenAndGetTokenValidation(String token) throws Exception {
        // Validate the token
        String cognitoSub = jwtService.extractCognitoSub(token);

        if (cognitoSub != null && jwtService.isTokenValid(token)) {
            // Retrieve user details from the local database
            Person person = personService.findByCognitoId(cognitoSub);
            if (person != null) {
                // Extract roles and create TokenValidation object
                List<String> roles = jwtService.extractCognitoGroups(token);
                TokenValidation tokenValidation = new TokenValidation(
                        true,
                        cognitoSub,
                        roles,
                        person.getName(),
                        person.getEmail(),
                        null,
                        person.getId(),
                        null, // No validation errors
                        null, // Custom attributes can be added here if any
                        token // Include the JWT token
                );
                return tokenValidation;
            } else {
                throw new RuntimeException("User not found in the local database.");
            }
        } else {
            throw new RuntimeException("Invalid JWT token.");
        }
    }


    public TokenValidation validateTokenAndGetTokenValidationFromId(String token) throws Exception {
        // Validate the token
        String cognitoSub = jwtService.extractCognitoSub(token);
        if (cognitoSub != null && jwtService.isTokenValid(token)) {
            List<String> roles = jwtService.extractCognitoGroups(token);
            String email = jwtService.extractCognitoEmail(token);
            // Extract roles and create TokenValidation object
            TokenValidation tokenValidation = new TokenValidation(
                    true,
                    cognitoSub,
                    Collections.singletonList("USER"),
                    email,
                    email,
                    null,
                    cognitoSub,
                    null, // No validation errors
                    null, // Custom attributes can be added here if any
                    token // Include the JWT token
            );
            return tokenValidation;
        } else {
            throw new RuntimeException("Invalid JWT token.");
        }
    }

    /**
     * Registers a new user locally if they are found in Cognito but not in the local database.
     *
     * @param cognitoUsername The Cognito username.
     * @param cognitoSub      The Cognito sub.
     * @param email           The user's email.
     * @param userGroups      The user's groups.
     */
    public void registerNewUser(String cognitoUsername,
                                String cognitoSub, String email,
                                List<String> userGroups) {
        // Save the user in the local database
        savePersonToDatabase(cognitoSub, email, userGroups);
    }

    private void savePersonToDatabase(String cognitoSub, String email, List<String> userGroups) {
        Person person = new Person(
                cognitoSub,
                cognitoSub,
                email, // Assuming email as name for simplicity
                email,
                userGroups,
                true // Active status
        );

        personService.savePerson(person);
    }

    /**
     * Retrieves the user's roles from the JWT token.
     *
     * @param token The JWT token.
     * @return A list of user roles.
     */
    public List<String> getUserRolesFromToken(String token) throws Exception {
        return jwtService.extractCognitoGroups(token);
    }

    /**
     * Checks if the user is active based on the provided JWT token.
     *
     * @param token The JWT token.
     * @return True if the user is active, false otherwise.
     */
    public boolean isUserActive(String token) throws Exception {
        String cognitoSub = jwtService.extractCognitoSub(token);

        // Check if the user exists in the local database
        Person person = personService.findByCognitoId(cognitoSub);
        return person != null && person.isActive();
    }
}
