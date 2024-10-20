package com.upthink.qms.controllers;

import com.upthink.qms.domain.Person;
import com.upthink.qms.service.AuthService;
import com.upthink.qms.service.PersonService;
import com.upthink.qms.service.request.AuthenticatedRequest;
import com.upthink.qms.service.request.ChangeGroupRequest;
import com.upthink.qms.service.request.ChangeStatusRequest;
import com.upthink.qms.service.response.BaseResponse;
import com.upthink.qms.service.response.PersonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/qm")
public class ManagerController {

    @Autowired
    AuthService authService;

    @Autowired
    PersonService personService; // Assuming you have a PersonService to interact with the database

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;


    @PostMapping("/listUsers")
    @PreAuthorize("hasRole('MANAGER')")
    public PersonResponse listCognitoUsers(AuthenticatedRequest request) {

        List<Person> peopleInDatabase = personService.getAllUsersForManager(); // Assuming findAllByCognitoSubIn is a method in PersonService

        // Map the peopleInDatabase to PersonDTOs
        List<PersonResponse.PersonDTO> personDTOs = peopleInDatabase.stream()
                .map(person -> new PersonResponse.PersonDTO(
                        person.getName(),
                        person.getEmail(),
                        person.getGroups(),
                        person.isActive()))
                .collect(Collectors.toList());

        // Return the response
        return new PersonResponse(personDTOs, true, null);
    }

    @PostMapping("/editGroups")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Person> addGroup (ChangeGroupRequest request) {
        // Add new role to cognito
        Person updatedPerson = personService.addGroup(request.getEmail(), request.getNewGroup());
        return ResponseEntity.ok(updatedPerson);
    }

    @PostMapping("/toggleActive")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Person> toggleActiveStatus(ChangeStatusRequest request) {
        // Delete all cognito roles in cognito
        Person updatedPerson = personService.toggleActiveStatus(request.getEmail());
        return ResponseEntity.ok(updatedPerson);
    }


}
