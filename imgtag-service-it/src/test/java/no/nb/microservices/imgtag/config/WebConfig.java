package no.nb.microservices.imgtag.config;

import no.nb.microservices.imgtag.service.NBUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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