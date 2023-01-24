package fi.vm.yti.groupmanagement.model;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserRequestWithOrganization {

    public final HashMap<String, String> organizationName = new HashMap<>(3);
    public Integer id;
    public String email;
    public UUID organizationId;
    public String role;
    public String firstName;
    public String lastName;
    public boolean sent = false;

    public UserRequestWithOrganization(final Object obj,
            final List<UserRequestWithOrganizationTrans> translations) {

        UserRequestWithOrganizationTrans data = translations.get(0);

        translations.forEach(translation -> {
            this.organizationName.put(translation.language, translation.name);
        });

        this.id = data.id;
        this.email = data.email;
        this.organizationId = data.organizationId;
        this.role = data.role;
        this.firstName = data.firstName;
        this.lastName = data.lastName;
        this.sent = data.sent;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
