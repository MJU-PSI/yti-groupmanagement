package fi.vm.yti.groupmanagement.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import fi.vm.yti.groupmanagement.dao.PublicApiDao.OrganizationRow;

import static java.util.Collections.unmodifiableMap;

public class PublicApiOrganization {

    private final UUID uuid;
    private final Map<String, String> prefLabel;
    private final Map<String, String> description;
    private final String url;
    private final boolean removed;
    private final UUID parentId;

    public PublicApiOrganization(final UUID uuid,
            final List<OrganizationRow> translations) {

        this.uuid = uuid;

        final HashMap<String, String> name = new HashMap<>(translations.size());
        translations.forEach(translation -> {
            name.put(translation.language, translation.name);
        });
        final HashMap<String, String> description = new HashMap<>(translations.size());
        translations.forEach(translation -> {
            description.put(translation.language, translation.description);
        });

        this.prefLabel = unmodifiableMap(name);
        this.description = unmodifiableMap(description);
        OrganizationRow data = translations.get(0);
        this.url = data != null ? data.url : null;
        this.removed = data != null ? data.removed : null;
        this.parentId = data != null ? data.parentId : null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, String> getPrefLabel() {
        return prefLabel;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public boolean getRemoved() {
        return removed;
    }

    public UUID getParentId() { return parentId; }

}