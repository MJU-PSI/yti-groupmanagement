package fi.vm.yti.groupmanagement.model;

import java.util.List;
import java.util.UUID;

public class UnsentRequestsForOrganization {

    public UUID id;
    public String name;
    public List<String> adminEmails;
    public List<UUID> adminUsers;
    public UUID userId;
    public int requestCount;
}