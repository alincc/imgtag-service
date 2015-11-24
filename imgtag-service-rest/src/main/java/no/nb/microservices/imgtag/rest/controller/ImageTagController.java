package no.nb.microservices.imgtag.rest.controller;

import no.nb.microservices.imgtag.model.ImageTag;
import no.nb.microservices.imgtag.model.ImageTagQuery;
import no.nb.microservices.imgtag.model.RequestInfo;
import no.nb.microservices.imgtag.repository.ImageTagRepository;
import no.nb.microservices.imgtag.rest.assembler.ImageTagPage;
import no.nb.microservices.imgtag.rest.assembler.ImageTagResourceAssembler;
import no.nb.microservices.imgtag.service.IImageTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriTemplate;

import javax.validation.Valid;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@RestController
@RequestMapping("/v1")
public class ImageTagController {

    final Logger LOG = LoggerFactory.getLogger(ImageTagController.class);

    private final ImageTagRepository imageTagRepository;
    private final IImageTagService imgTagService;
    private final ImageTagResourceAssembler assembler;

    @Autowired
    public ImageTagController(ImageTagRepository imageTagRepository, IImageTagService imgTagService, ImageTagResourceAssembler assembler) {
        this.imageTagRepository = imageTagRepository;
        this.imgTagService = imgTagService;
        this.assembler = assembler;
    }

    @RequestMapping(value = "/imgtags", method = RequestMethod.GET, produces = {"application/json"})
    public ResponseEntity<PagedResources<ImageTag>> query(ImageTagQuery query,
                                                        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                        @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                        @RequestParam(required = false) String[] fields) {
        Page<ImageTag> pages = imgTagService.query(query, page, size, fields);
        PagedResources<ImageTag> pagedResources = assembler.toResource(new ImageTagPage(pages, query, fields));
        return new ResponseEntity<PagedResources<ImageTag>>(pagedResources, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/imgtags", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {"application/json"})
    public ResponseEntity<ImageTag> save(@Valid @RequestBody ImageTag imageTag) {
        ImageTag savedTag = imgTagService.save(imageTag);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new UriTemplate("/v1/imgtags/{imgtagid}").expand(savedTag.getTagId()));

        return new ResponseEntity<ImageTag>(savedTag, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/imgtags/{imgTagID}", method = RequestMethod.GET, produces = {"application/json"})
    public ResponseEntity<ImageTag> findOne(@PathVariable String imgTagID,
                                          @RequestParam(required = false) String[] fields) {
        ImageTag imageTag = imgTagService.findOne(imgTagID, fields);
        imageTag.add(linkTo(methodOn(ImageTagController.class).findOne(imgTagID, fields)).withSelfRel());
        return new ResponseEntity<ImageTag>(imageTag, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/imgtags/{imgTagID}", method = RequestMethod.POST)
    public ResponseEntity<ImageTag> partialUpdate(ImageTag imageTag,
                                                @PathVariable(value = "imgTagID") String id)
    {
        ImageTag updatedTag = imgTagService.update(imageTag);
        return new ResponseEntity<ImageTag>(updatedTag, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_TagsAdmin')")
    @RequestMapping(value = "/imgtags/{imgTagID}", method = RequestMethod.PUT)
    public ResponseEntity<ImageTag> fullyUpdate(@Valid ImageTag imageTag,
                                              @PathVariable(value = "imgTagID") String id)
    {
        imageTag.setTagId(id);
        ImageTag updatedTag = imageTagRepository.save(imageTag);
        return new ResponseEntity<ImageTag>(updatedTag, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/imgtags/{imgTagID}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable(value = "imgTagID") String id) {
        imgTagService.delete(id);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/imgtags/{imgTagID}/report", method = { RequestMethod.PUT, RequestMethod.POST })
    public ResponseEntity<ImageTag> report (@PathVariable(value = "imgTagID") String id,
                                          @Valid @RequestBody RequestInfo requestInfo)
    {
        ImageTag savedImageTag = imgTagService.report(id, requestInfo);
        return new ResponseEntity<ImageTag>(savedImageTag, HttpStatus.OK);
    }


}
