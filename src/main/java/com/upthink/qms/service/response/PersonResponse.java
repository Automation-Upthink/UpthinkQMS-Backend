package com.upthink.qms.service.response;

import gson.GsonDTO;

import java.util.List;

public class PersonResponse extends GsonDTO {

    private final List<PersonDTO> personList;
    private final boolean success;
    private final String error;

    public class PersonDTO extends GsonDTO {

        private final String id;
        private final String name;
        private final String email;
        private final String cognitoId;
        private final List<String> groups;
        private final boolean active;

        public PersonDTO(String id, String name, String email, String cognitoId, List<String> groups, boolean active) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.cognitoId = cognitoId;
            this.groups = groups;
            this.active = active;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getCognitoId() {
            return cognitoId;
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
