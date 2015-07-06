package no.nb.microservices.imgtag.service;

import no.nb.microservices.imgtag.model.ImageTag;
import no.nb.microservices.imgtag.model.ImageTagQuery;
import no.nb.microservices.imgtag.model.RequestInfo;
import org.springframework.data.domain.Page;

/**
 * Created by andreasb on 26.06.15.
 */
public interface IImageTagService {
    Page<ImageTag> query(ImageTagQuery imageTagQuery, int page, int size, String[] expand);

    ImageTag findOne(String id, String[] expand);

    void delete(String id);

    ImageTag save(ImageTag geoTag);

    ImageTag update(ImageTag geoTag);

    ImageTag report(String id, RequestInfo requestInfo);
}
