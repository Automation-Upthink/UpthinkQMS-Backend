package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.util.ArrayList;
import java.util.List;

public class PersonResponse extends GsonDTO {

    private final List<PersonDTO> personList;
    private final boolean success;
    private final String error;

    public static class PersonDTO extends GsonDTO {

        private final String name;
        private final String email;
        private final List<String> groups;
        private final boolean active;

        public PersonDTO(String name, String email, List<String> groups, boolean active){
            this.name = name;
            this.email = email;
            this.groups = groups; //new ArrayList<>(groups)
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }


        public List<String> getGroups() {
            return groups;
        }

        public boolean isActive() {
            return active;
        }
    }

    public PersonResponse(List<PersonDTO> personList, boolean success, String error) {
        this.personList = personList;
        this.success = success;
        this.error = error;
    }

    public List<PersonDTO> getPersonList() {
        return personList;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

}
