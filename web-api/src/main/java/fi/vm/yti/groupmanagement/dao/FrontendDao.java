package fi.vm.yti.groupmanagement.dao;

import fi.vm.yti.groupmanagement.model.*;

import org.dalesbred.Database;
import org.dalesbred.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.util.*;

import static fi.vm.yti.groupmanagement.util.CollectionUtil.mapToList;
import static java.util.stream.Collectors.*;

@Repository
public class FrontendDao {

    private final Database db;

    @Autowired
    public FrontendDao(Database db) {
        this.db = db;
    }

    public List<UserWithRolesInOrganizations> getUsers() {

        List<UserRow> rows = db.findAll(UserRow.class,
                "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, array_agg(uo.role_name) AS roles \n" +
                        "FROM \"user\" u \n" +
                        "  LEFT JOIN user_organization uo ON (uo.user_email = u.email) \n" +
                        "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at \n" +
                        "ORDER BY u.lastName, u.firstName \n" +
                        "");

        Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
                rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            UserRow.UserDetails user = entry.getKey();

            List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                    .filter(org -> org.id != null)
                    .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                    .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.creationDateTime, organizations);
        });
    }

    public @NotNull List<OrganizationListItem> getOrganizationList() {

        List<OrganizationListItemRow> rows =
                db.findAll(OrganizationListItemRow.class, "SELECT id, name_en, name_fi, name_sv FROM organization ORDER BY name_fi");

        return mapToList(rows, row -> new OrganizationListItem(row.id, row.nameFi, row.nameEn, row.nameSv));
    }

    public @NotNull Organization getOrganization(UUID organizationId) {
        return db.findUnique(Organization.class,"SELECT id, name_en, name_fi, name_sv, description_en, description_fi, description_sv, url FROM organization where id = ?", organizationId);
    }

    public @NotNull List<UserWithRoles> getOrganizationUsers(UUID organizationId) {

        List<UserRow> rows = db.findAll(UserRow.class,
                "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, array_agg(uo.role_name) AS roles \n" +
                        "FROM \"user\" u \n" +
                        "  LEFT JOIN user_organization uo ON (uo.user_email = u.email) \n" +
                        "WHERE uo.organization_id = ? \n" +
                        "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at", organizationId);

        return mapToList(rows, row -> {

            User user = new User();
            user.firstName = row.user.firstName;
            user.lastName = row.user.lastName;
            user.email = row.user.email;

            UserWithRoles result = new UserWithRoles();
            result.roles = row.organization.roles;
            result.user = user;

            return result;
        });
    }

    public @NotNull List<String> getAvailableRoles() {
        return db.findAll(String.class, "SELECT name from role");
    }

    public void createOrganization(Organization org) {

        db.update("INSERT INTO organization (id, name_en, name_fi, name_sv, description_en, description_fi, description_sv, url) VALUES (?,?,?,?,?,?,?,?)",
                org.id, org.nameEn, org.nameFi, org.nameSv, org.descriptionEn, org.descriptionFi, org.descriptionSv, org.url);
    }

    public void updateOrganization(Organization org) {

        db.update("UPDATE organization SET name_en=?, name_fi=?, name_sv=?, description_en=?, description_fi=?, description_sv=?, url=? WHERE id = ?",
                org.nameEn, org.nameFi, org.nameSv, org.descriptionEn, org.descriptionFi, org.descriptionSv, org.url, org.id);
    }

    public void addUserToRoleInOrganization(String userEmail, String role, UUID id) {
        db.update("INSERT INTO user_organization (user_email, organization_id, role_name) VALUES (?, ?, ?)", userEmail, id, role);
    }

    public void clearUserRoles(UUID id) {
        db.update("DELETE FROM user_organization uo where uo.organization_id = ?", id);
    }

    public @NotNull List<String> getAllRoles() {
        return db.findAll(String.class,"SELECT name FROM role");
    }

    public void addUserRequest(UserRequestModel userRequest) {
        db.update("INSERT INTO request (user_email, organization_id, role_name, sent) VALUES (?,?,?,?)",
                userRequest.email, userRequest.organizationId, userRequest.role, false);
    }

    public @NotNull List<UserRequestWithOrganization> getAllUserRequestsForOrganizations(@Nullable Set<UUID> organizations) {

        QueryBuilder builder = new QueryBuilder(
                "SELECT r.id, r.user_email, r.organization_id, r.role_name, us.firstName, us.lastName, org.name_fi, org.name_en, org.name_sv, r.sent \n" +
                        "FROM request r\n" +
                        "LEFT JOIN \"user\" us ON (us.email = r.user_email)\n" +
                        "LEFT JOIN organization org ON (org.id = r.organization_id)\n");

        if (organizations != null) {
            builder.append("WHERE r.organization_id in (").appendPlaceholders(organizations).append(")");
        }

        return db.findAll(UserRequestWithOrganization.class, builder.build());
    }

    public void deleteUserRequest(int requestId) {
        db.update("DELETE FROM request WHERE id=?", requestId);
    }

    public @NotNull UserRequest getUserRequest(int requestId) {
        return db.findUnique(UserRequest.class,
                "SELECT r.id, r.user_email, r.organization_id, r.role_name, r.sent FROM request r\n" +
                        "WHERE r.id = ?", requestId);
    }

    public static class OrganizationListItemRow {

        public UUID id;
        public String nameFi;
        public String nameEn;
        public String nameSv;
    }
}
