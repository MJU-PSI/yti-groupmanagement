package fi.vm.yti.groupmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("")
@Component
public class GroupManagmentProperties {
    private String defaultLanguage;

    public String getDefaultLanguage() {
        return this.defaultLanguage != null ? this.defaultLanguage : "en";
    }

    public void setDefaultLanguage(final String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}