package fi.vm.yti.groupmanagement.model;

import java.util.List;
import java.util.UUID;

public class CreateOrganization {

    public String url;
    public UUID parentId;

    public List<OrganizationTrans> translations;
    public List<String> adminUserEmails;
}
