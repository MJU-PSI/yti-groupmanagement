package fi.vm.yti.groupmanagement.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.vm.yti.groupmanagement.dao.FrontendDao.OrganizationTransListItemRow;

public final class OrganizationListItem {

    private final UUID id;
    private final Map<String, String> name;
    private final Map<String, String> description;

    public OrganizationListItem(final UUID id,
            final List<OrganizationTransListItemRow> translations) {

        final HashMap<String, String> name = new HashMap<>(translations.size());
        translations.forEach(translation -> {
            name.put(translation.language, translation.name);
        });
        final HashMap<String, String> description = new HashMap<>(translations.size());
        translations.forEach(translation -> {
            name.put(translation.language, translation.description);
        });

        this.id = id;
        this.name = name;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public Map<String, String> getName() {
        return name;
    }

    public Map<String, String> getDescription() {
        return description;
    }
}
