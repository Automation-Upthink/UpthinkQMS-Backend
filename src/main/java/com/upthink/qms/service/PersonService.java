package com.upthink.qms.service;

import com.upthink.qms.domain.Person;
import com.upthink.qms.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    public Optional<Person> findPersonByEmail(String email) {return personRepository.findByEmail(email);}

    public Optional<Person> findPersonById(String id){return personRepository.findById(id);}

    public List<Person> findPersonsByGroup(String group) {
        return personRepository.findByGroup(group);
    }

    public Person findByCognitoId(String id){
        Optional<Person> personOpt = personRepository.findByCognitoId(id);
        if(!personOpt.isPresent()){
            return null;
        }
        return personOpt.get();
    }


    public List<Person> findAllByCognitoIds(List<String> cognitoIds) {
        return personRepository.findByCognitoIdIn(cognitoIds);
    }

    @Transactional
    public Person savePerson(Person person) {
        System.out.println("Saving in to database initiated ...");
        return personRepository.save(person);
    }



}
