package fi.vm.yti.groupmanagement.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class AjpConfig {

    @Bean
    public TomcatServletWebServerFactory servletContainer(@Value("${tomcat.ajp.port:}") Integer ajpPort) throws UnknownHostException {

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        if (ajpPort != null) {
            Connector ajpConnector = new Connector("AJP/1.3");
            ajpConnector.setPort(ajpPort);
            ajpConnector.setSecure(false);
            ajpConnector.setAllowTrace(false);
            ajpConnector.setScheme("http");
            ajpConnector.setProperty("allowedRequestAttributesPattern", ".*");

            AjpNioProtocol protocol = (AjpNioProtocol)ajpConnector.getProtocolHandler();
            protocol.setSecretRequired(false);
            protocol.setAddress(InetAddress.getByName("0.0.0.0"));

            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }

        return tomcat;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {

        var source = new ResourceBundleMessageSource();
        source.setBasenames("messages/labels");
        source.setUseCodeAsDefaultMessage(true);

        return source;
    }
}
