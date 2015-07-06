package no.nb.microservices.imgtag.rest.assembler;

import no.nb.microservices.imgtag.config.ApplicationSettings;
import no.nb.microservices.imgtag.model.ImageTag;
import no.nb.microservices.imgtag.rest.controller.ImageTagController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by Andreas Bjørnådal (andreasb) on 26.08.14.
 */
@Component
public class ImageTagResourceAssembler implements ResourceAssembler<ImageTagPage, PagedResources<ImageTag>> {

    private final ApplicationSettings applicationSettings;

    @Autowired
    public ImageTagResourceAssembler(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public PagedResources<ImageTag> toResource(ImageTagPage imageTagPage) {
        Page page = imageTagPage.getPage();

        Collection<ImageTag> resources = new ArrayList<ImageTag>();
        for(Object o : page.getContent()) {
            ImageTag imageTag = (ImageTag)o;
            imageTag.add(linkTo(ImageTagController.class).slash(imageTag.getTagId()).withSelfRel());
            resources.add(imageTag);
        }

        List<Link> links = new ArrayList<Link>();
        links.add(linkTo(methodOn(ImageTagController.class).query(imageTagPage.getImageTagQuery(), page.getNumber(), page.getSize(), imageTagPage.getExpand())).withSelfRel());
        if (page.hasPrevious()) {
            links.add(linkTo(methodOn(ImageTagController.class).query(imageTagPage.getImageTagQuery(), page.previousPageable().getPageNumber(), page.getSize(), imageTagPage.getExpand())).withRel("prev"));
        }
        if (page.hasNext()) {
            links.add(linkTo(methodOn(ImageTagController.class).query(imageTagPage.getImageTagQuery(), page.nextPageable().getPageNumber(), page.getSize(), imageTagPage.getExpand())).withRel("next"));
        }
        if (!page.isFirst()) {
            links.add(linkTo(methodOn(ImageTagController.class).query(imageTagPage.getImageTagQuery(), 0, page.getSize(), imageTagPage.getExpand())).withRel("first"));
        }
        if (!page.isLast()) {
            links.add(linkTo(methodOn(ImageTagController.class).query(imageTagPage.getImageTagQuery(), page.getTotalPages() - 1, page.getNumberOfElements(), imageTagPage.getExpand())).withRel("last"));
        }

        PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());
        PagedResources<ImageTag> pagedResources = new PagedResources<ImageTag>(resources, metadata, links);

        return pagedResources;
    }
}