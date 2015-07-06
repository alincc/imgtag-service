package no.nb.microservices.imgtag.repository;

import no.nb.microservices.imgtag.model.ImageTag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by Andreas Bjørnådal (andreasb) on 27.08.14.
 */
public interface ImageTagRepository extends MongoRepository<ImageTag, String>, QueryDslPredicateExecutor<ImageTag> {

}
