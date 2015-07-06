package no.nb.microservices.imgtag.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.microservices.imgtag.service.ImageTagService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.UnknownHostException;

/**
 * Created by andreasb on 03.07.15.
 */
@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "tagdb";
    }

    @Override
    public Mongo mongo() throws UnknownHostException {
        return new MongoClient("127.0.0.1", WebConfig.MONGO_PORT);
    }

    @Override
    protected String getMappingBasePackage() {
        return "no.nb.microservices.imgtag.repository";
    }
}
