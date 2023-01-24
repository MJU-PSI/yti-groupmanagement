package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

public class UserRequestWithOrganizationTrans {

    public Integer id;
    public String email;
    public UUID organizationId;
    public String role;
    public String firstName;
    public String lastName;
    public String language;
    public String name;
    public boolean sent = false;

    public UserRequestWithOrganizationTrans(final Integer id,
            final String userEmail,
            final UUID organizationId,
            final String roleName,
            final String firstName,
            final String lastName,
            final String language,
            final String name,
            final Boolean sent) {

        this.id = id;
        this.email = userEmail;
        this.organizationId = organizationId;
        this.role = roleName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.language = language;
        this.name = name;
        this.sent = sent;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean getSent() {
        return sent;
    }

}
