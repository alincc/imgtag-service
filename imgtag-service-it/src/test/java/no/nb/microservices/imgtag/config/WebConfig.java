package no.nb.microservices.imgtag.config;

import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.microservices.imgtag.service.ImageTagService;
import no.nb.microservices.imgtag.service.NBUserService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.security.oauth2.resource.EnableOAuth2Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * Created by andreasb on 03.07.15.
 */
@Configuration
public class WebConfig {

    public static final int MONGO_PORT = 12340;

    @Bean
    public ApplicationSettings applicationSettings() {
        ApplicationSettings settings = new ApplicationSettings();
        settings.setNbsokContentUrl("http://www.nb.no/nbsok/nb/{sesamid}");
        settings.setFotoContentUrl("http://www.nb.no/foto/nb/{sesamid}");
        return settings;
    }

    @Bean
    public NBUserService nbUserService() {
        return new MockNBUserService();
    }
}