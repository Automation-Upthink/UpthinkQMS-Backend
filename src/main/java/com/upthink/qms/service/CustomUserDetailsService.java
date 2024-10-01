package com.upthink.qms.service;

import com.upthink.qms.domain.PersonPrincipal;
import com.upthink.qms.domain.Person;
import com.upthink.qms.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PersonService personService;

    @Override
    public UserDetails loadUserByUsername(String cognitoId) throws UsernameNotFoundException {
        Person user = personService.findByCognitoId(cognitoId);
        return new PersonPrincipal(user);
    }

}
