package fi.vm.yti.groupmanagement.dao;

import java.util.List;
import java.util.UUID;

import org.dalesbred.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.vm.yti.groupmanagement.config.GroupManagmentProperties;
import fi.vm.yti.groupmanagement.model.TempUser;
import fi.vm.yti.groupmanagement.model.UnsentRequestsForOrganization;

@Repository
public class EmailSenderDao {

    final String getTempUsersWithContainerQuery = "SELECT email, firstName, lastName, id, token_role, container_uri FROM tempuser WHERE token_created_at IS NULL AND email IS NOT NULL AND container_uri = ?";

    private final Database database;
    private final GroupManagmentProperties groupManagmentProperties;

    @Autowired
    public EmailSenderDao(final Database database, final GroupManagmentProperties groupManagmentProperties) {
        this.database = database;
        this.groupManagmentProperties = groupManagmentProperties;
    }

    public List<UnsentRequestsForOrganization> getUnsentRequests() {
        final String getUnsentQuery = "WITH unsent_requests_in_organizations AS (\n" +
                "    SELECT\n" +
                "      org.id,\n" +
                "      orgt.name,\n" +
                "      r.user_id,\n" +
                "      count(r.id) AS request_count\n" +
                "    FROM request r\n" +
                "      LEFT JOIN organization org ON (org.id = r.organization_id)\n" +
                "      LEFT JOIN organization_trans orgt ON (org.id = orgt.organization_id)\n" +
                "    WHERE r.sent = FALSE AND orgt.language = ?\n" +
                "    GROUP BY org.id, orgt.name, r.user_id\n" +
                ")\n" +
                "SELECT uro.id, uro.name, " +
                "       array_agg(u.email) as admin_emails, array_agg(u.id) as admin_users, uro.user_id, uro.request_count\n"
                +
                "FROM unsent_requests_in_organizations uro\n" +
                "  LEFT JOIN user_organization uo ON (uo.organization_id = uro.id)\n" +
                "  LEFT JOIN \"user\" u ON (u.id = UO.user_id)\n" +
                "WHERE uo.role_name = 'ADMIN'\n" +
                "GROUP BY uro.id, uro.name, uro.user_id, uro.request_count";

        return database.findAll(UnsentRequestsForOrganization.class, getUnsentQuery,
                groupManagmentProperties.getDefaultLanguage());
    }

    public List<TempUser> getTempUsersWithoutTokensAndContainerUri(final String containerUri) {
        return database.findAll(TempUser.class, getTempUsersWithContainerQuery, containerUri);
    }

    public void markRequestAsSentForOrganization(final UUID organizationId) {
        database.update("UPDATE request SET sent='true' WHERE organization_id = ?", organizationId);
    }
}
