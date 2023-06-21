package fi.vm.yti.groupmanagement.dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dalesbred.Database;
import org.dalesbred.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fi.vm.yti.groupmanagement.config.GroupManagmentProperties;
import fi.vm.yti.groupmanagement.model.Organization;
import fi.vm.yti.groupmanagement.model.OrganizationListItem;
import fi.vm.yti.groupmanagement.model.OrganizationTrans;
import fi.vm.yti.groupmanagement.model.User;
import fi.vm.yti.groupmanagement.model.UserRequest;
import fi.vm.yti.groupmanagement.model.UserRequestModel;
import fi.vm.yti.groupmanagement.model.UserRequestWithOrganization;
import fi.vm.yti.groupmanagement.model.UserRequestWithOrganizationTrans;
import fi.vm.yti.groupmanagement.model.UserWithRoles;
import fi.vm.yti.groupmanagement.model.UserWithRolesInOrganizations;
import fi.vm.yti.groupmanagement.service.impl.TokenServiceImpl;
import static fi.vm.yti.groupmanagement.util.CollectionUtil.mapToList;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.*;
import static org.dalesbred.query.SqlQuery.namedQuery;

@Repository
public class FrontendDao {

    private static final String TYPE_TOKEN_TEMPUSER = "tempuser";
    private static final String KEY_TYPE = "type";

    private final Database db;
    private final TokenServiceImpl tokenService;
    private final GroupManagmentProperties groupManagmentProperties;

    @Autowired
    public FrontendDao(final Database db,
            final TokenServiceImpl tokenService,
            final GroupManagmentProperties groupManagmentProperties) {
        this.db = db;
        this.tokenService = tokenService;
        this.groupManagmentProperties = groupManagmentProperties;
    }

    public List<UserWithRolesInOrganizations> getUsersForAdminOrganizations(final String email) {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                "WHERE uo.organization_id IN (SELECT organization_id FROM user_organization WHERE u.email = ? and role_name='ADMIN') \n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName \n" +
                "", email);

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            final UserRow.UserDetails user = entry.getKey();

            final List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public List<UserWithRolesInOrganizations> getUsers() {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) WHERE u.removed_at IS NULL \n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName \n" +
                "");

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            final UserRow.UserDetails user = entry.getKey();

            final List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public List<UserWithRolesInOrganizations> getPublicUsers() {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "LEFT JOIN user_organization uo ON (uo.user_id = u.id) WHERE u.removed_at IS NULL AND u.email like '%localhost'\n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName \n" +
                "");

        final Map<UserRow.UserDetails, List<UserRow.OrganizationDetails>> grouped =
            rows.stream().collect(groupingBy(row -> row.user, LinkedHashMap::new, mapping(row -> row.organization, toList())));

        return mapToList(grouped.entrySet(), entry -> {

            final UserRow.UserDetails user = entry.getKey();

            final List<UserWithRolesInOrganizations.OrganizationRoles> organizations = entry.getValue().stream()
                .filter(org -> org.id != null)
                .map(org -> new UserWithRolesInOrganizations.OrganizationRoles(org.id, org.roles))
                .collect(toList());

            return new UserWithRolesInOrganizations(user.email, user.firstName, user.lastName, user.superuser, user.id, user.creationDateTime, user.removalDateTime, organizations);
        });
    }

    public boolean removeUser(String email) {
        db.update("DELETE FROM user_organization uo USING \"user\" u WHERE uo.user_id = u.id AND u.email = ?", email);
        final int modifiedRows = db.update("UPDATE \"user\" SET email=?, firstname=?, lastname=?, removed_at=? WHERE email = ?",
            null, null, null, LocalDateTime.now(), email);
        if (modifiedRows > 0) {
            return true;
        } else {
            return false;
        }
    }

    public @NotNull List<OrganizationListItem> getMainOrganizationListOpt(Boolean showRemoved) {
        final List<OrganizationTransListItemRow> rows = db.findAll(OrganizationTransListItemRow.class,
                "SELECT o.id, t.language, t.name, t.description FROM organization o JOIN organization_trans t ON o.id = t.organization_id WHERE o.removed = ? AND o.parent_id IS NULL ORDER BY o.id, t.language",
                showRemoved);

        return organizationTransToList(rows);
    }

    public @NotNull List<OrganizationListItem> getOrganizationList() {
        return getOrganizationList(false);
    }

    public @NotNull List<OrganizationListItem> getOrganizationList(boolean includeChildOrganizations) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, t.language, t.name, t.description FROM organization o JOIN organization_trans t ON o.id = t.organization_id");

        if (!includeChildOrganizations) {
            sql.append(" WHERE o.parent_id IS NULL");
        }

        sql.append(" ORDER BY o.id, t.language");

        final List<OrganizationTransListItemRow> rows = db.findAll(OrganizationTransListItemRow.class, sql.toString());
        return organizationTransToList(rows);
    }

    public @NotNull Organization getOrganization(UUID organizationId) {
        return db.findUnique(Organization.class, "SELECT id, url, removed, parent_id FROM organization where id = ?",
                organizationId);
    }

    public @NotNull List<OrganizationListItem> getChildOrganizations(UUID parentOrganizationId) {
        final List<OrganizationTransListItemRow> rows = db.findAll(OrganizationTransListItemRow.class,
                "SELECT o.id, t.language, t.name, t.description FROM organization o JOIN organization_trans t ON o.id = t.organization_id WHERE parent_id = ? ORDER BY o.id, t.language ",
                parentOrganizationId);
        return organizationTransToList(rows);
    }

    public @NotNull List<OrganizationTrans> getTranslations(UUID organizationId) {
        final List<OrganizationTrans> rows = db.findAll(OrganizationTrans.class,
                "SELECT organization_id, language, name, description FROM organization_trans where organization_id = ? ORDER BY language ",
                organizationId);
        return mapToList(rows,
                row -> new OrganizationTrans(row.organizationId, row.language, row.name, row.description));
    }

    public @NotNull OrganizationTrans getOrganizationTrans(UUID organizationId, String language) {
        Map<String, Object> values = new HashMap<>();
        values.put("organizationId", organizationId);
        values.put("language", language);
        final List<OrganizationTrans> translations = db.findAll(OrganizationTrans.class,
                namedQuery(
                        "SELECT organization_id, language, name, description FROM organization_trans where organization_id = :organizationId AND language = :language",
                        values));

        if (translations.size() == 1) {
            return translations.get(0);
        }
        return null;
    }

    public @NotNull List<UserWithRoles> getOrganizationUsers(UUID organizationId) {

        final List<UserRow> rows = db.findAll(UserRow.class,
            "SELECT u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id, u.removed_at, array_agg(uo.role_name) AS roles \n" +
                "FROM \"user\" u \n" +
                "  LEFT JOIN user_organization uo ON (uo.user_id = u.id) \n" +
                "WHERE uo.organization_id = ? \n" +
                "GROUP BY u.email, u.firstName, u.lastName, u.superuser, uo.organization_id, u.created_at, u.id \n" +
                "ORDER BY u.lastName, u.firstName, u.email", organizationId);

        return mapToList(rows, row -> {

            User user = new User();
            user.id = row.user.id;
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

    public void createOrganization(final Organization org) {

        db.update("INSERT INTO organization (id, url, parent_id) VALUES (?,?,?)",
                org.id, org.url, org.parentId);
    }

    public void updateOrganization(final Organization org) {

        db.update("UPDATE organization SET url=?, removed=?, modified=now() WHERE id = ?",
                org.url, org.removed, org.id);
    }

    public void clearOrganizationTrans(UUID id) {

        db.update("DELETE FROM organization_trans ot where ot.organization_id = ?", id);
        updateOrganizationModifiedStamp(id);
    }

    public void createOrganizationTrans(final OrganizationTrans orgTrans) {
        db.update(
                "INSERT INTO organization_trans (organization_id, language, name, description) VALUES (?,?,?,?)",
                orgTrans.organizationId, orgTrans.language, orgTrans.name, orgTrans.description);
    }

    public void updateOrganizationTrans(final OrganizationTrans orgTrans) {

        db.update("UPDATE organization_trans SET name=?, description=? WHERE organization_id = ? AND language = ?",
                orgTrans.name, orgTrans.description, orgTrans.organizationId, orgTrans.language);
    }

    public void addUserToRoleInOrganization(final String userEmail,
                                            final String role,
                                            final UUID id) {

        db.update("INSERT INTO user_organization (user_id, organization_id, role_name) VALUES ((select id from \"user\" where email = ?), ?, ?)", userEmail, id, role);
        updateOrganizationModifiedStamp(id);
    }

    public void clearUserRoles(UUID id) {

        db.update("DELETE FROM user_organization uo where uo.organization_id = ?", id);
        updateOrganizationModifiedStamp(id);
    }

    public @NotNull List<String> getAllRoles() {
        return db.findAll(String.class, "SELECT name FROM role");
    }

    public String getOrganizationName(final UUID id) {
        String name;
        name = db.findUnique(String.class,
                "SELECT name FROM organization_trans WHERE organization_id=? AND language = ?", id,
                groupManagmentProperties.getDefaultLanguage());
        if (name == null) {
            List<String> names = db.findAll(String.class, "SELECT name FROM organization_trans WHERE organization_id=?",
                    id);
            if (names != null && names.size() > 0) {
                name = names.get(0);
            }
        }
        return name;
    }

    public void addUserRequest(UserRequestModel userRequest) {
        db.update("INSERT INTO request (user_id, organization_id, role_name, sent) VALUES ((select id from \"user\" where email = ?),?,?,?)",
            userRequest.email, userRequest.organizationId, userRequest.role, false);
        @NotNull List<UUID> userIds = db.findAll(UUID.class, "SELECT id from user where email = ?", userRequest.email);
    }

    public @NotNull List<UserRequestWithOrganization> getAllUserRequestsForOrganizations(@Nullable Set<UUID> organizations) {

        final QueryBuilder builder = new QueryBuilder(
                "SELECT r.id, us.email as user_email, r.organization_id, r.role_name, us.firstName, us.lastName, t.language, t.name, r.sent \n"
                        +
                        "FROM request r\n" +
                        "LEFT JOIN \"user\" us ON (us.id = r.user_id)\n" +
                        "LEFT JOIN organization org ON (org.id = r.organization_id)\n" +
                        "JOIN organization_trans t ON org.id = t.organization_id\n");

        if (organizations != null) {
            builder.append("WHERE r.organization_id in (").appendPlaceholders(organizations).append(")");
        }

        final List<UserRequestWithOrganizationTrans> rows = db.findAll(UserRequestWithOrganizationTrans.class,
                builder.build());
        return userRequestWithOrganizationTransToList(rows);
    }

    public void deleteUserRequest(int requestId) {
        db.update("DELETE FROM request WHERE id=?", requestId);
    }

    public @NotNull UserRequest getUserRequest(int requestId) {
        return db.findUnique(UserRequest.class,
            "SELECT r.id, u.email as user_email, r.user_id, r.organization_id, r.role_name, r.sent FROM request r \n" +
                "LEFT JOIN \"user\" u on (u.id = r.user_id) \n" +
                "WHERE r.id = ?", requestId);
    }

    public String createToken(final UUID userId) {
        return createToken(userId, null);
    }

    public String createToken(final UUID userId,
                              final String type) {

        final LocalDateTime createdAtLocalDateTime = now();
        final LocalDateTime invalidatedAtLocalDateTime = createdAtLocalDateTime.plusMonths(6);
        final Date createdAt = Date.from(createdAtLocalDateTime.atZone(ZoneId.of("UTC")).toInstant());
        final Date invalidatedAt = Date.from(invalidatedAtLocalDateTime.atZone(ZoneId.of("UTC")).toInstant());
        final int success;
        if ("tempuser".equalsIgnoreCase(type)) {
            success = db.update("UPDATE tempuser SET token_created_at = ?, token_invalidation_at = ? WHERE id = ?", createdAtLocalDateTime, invalidatedAtLocalDateTime, userId);
        } else {
            success = db.update("UPDATE \"user\" SET token_created_at = ?, token_invalidation_at = ? WHERE id = ?", createdAtLocalDateTime, invalidatedAtLocalDateTime, userId);
        }
        if (success == 1) {
            final Map<String, Object> claims = new HashMap<>();
            if (TYPE_TOKEN_TEMPUSER.equalsIgnoreCase(type)) {
                claims.put(KEY_TYPE, TYPE_TOKEN_TEMPUSER);
            }
            return tokenService.generateToken(userId, claims, createdAt, invalidatedAt);
        } else {
            throw new RuntimeException("No user found with ID: " + userId.toString());
        }
    }

    public boolean deleteToken(final UUID userId) {

        final int success = db.update("UPDATE \"user\" SET token_created_at = NULL, token_invalidation_at = NULL WHERE id = ?", userId);
        return success == 1;
    }

    public UUID getUserIdForEmail(final String email) {
        final List<UserRow> rows = db.findAll(UserRow.class, "SELECT u.id FROM \"user\" u WHERE u.email = ?", email);
        if (rows.size() == 1) {
            return rows.get(0).user.id;
        }
        return null;
    }

    void updateOrganizationModifiedStamp(UUID orgId) {
        db.update("UPDATE organization SET modified=now() WHERE id = ?", orgId);
    }

    public static class OrganizationTransListItemRow {

        public UUID id;
        public String language;
        public String name;
        public String description;

        public UUID getId() {
            return id;
        }
    }

    public @NotNull List<OrganizationListItem> organizationTransToList(
            @NotNull final List<OrganizationTransListItemRow> rows) {

        final Map<UUID, List<OrganizationTransListItemRow>> organizationTrans = rows.stream()
                .collect(Collectors.groupingBy(OrganizationTransListItemRow::getId));

        final List<OrganizationListItem> organizationListItems = new ArrayList<OrganizationListItem>();
        organizationTrans.forEach((k, v) -> organizationListItems.add(new OrganizationListItem(k, v)));
        return organizationListItems;
    }

    public @NotNull List<UserRequestWithOrganization> userRequestWithOrganizationTransToList(
            @NotNull final List<UserRequestWithOrganizationTrans> rows) {

        Function<UserRequestWithOrganizationTrans, List<Object>> compositeKey = rec -> Arrays
                .<Object>asList(rec.getId(), rec.getEmail(), rec.getOrganizationId(), rec.getRole(), rec.getFirstName(),
                        rec.getLastName(), rec.getSent());

        final Map<Object, List<UserRequestWithOrganizationTrans>> userRequestOrganizationTrans = rows.stream()
                .collect(Collectors.groupingBy(compositeKey, Collectors.toList()));

        final List<UserRequestWithOrganization> userRequestOrganizationListItems = new ArrayList<UserRequestWithOrganization>();
        userRequestOrganizationTrans
                .forEach((k, v) -> userRequestOrganizationListItems.add(new UserRequestWithOrganization(k, v)));

        return userRequestOrganizationListItems;
    }
}
