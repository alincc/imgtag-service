package no.nb.microservices.imgtag.repository;

import no.nb.microservices.imgtag.model.ImgTag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;import java.lang.String;

/**
 * Created by Andreas Bjørnådal (andreasb) on 27.08.14.
 */
public interface ImgTagRepository extends MongoRepository<ImgTag, String>, QueryDslPredicateExecutor<ImgTag> {

}
