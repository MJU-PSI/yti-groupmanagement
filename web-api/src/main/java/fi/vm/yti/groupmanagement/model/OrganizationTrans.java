package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

public class OrganizationTrans {

    public UUID organizationId;
    public String language;
    public String name;
    public String description;

    public OrganizationTrans(final UUID organizationId, final String language, final String name,
            final String description) {
        this.organizationId = organizationId;
        this.language = language;
        this.name = name;
        this.description = description;
    }

    public OrganizationTrans() {
    }

}
