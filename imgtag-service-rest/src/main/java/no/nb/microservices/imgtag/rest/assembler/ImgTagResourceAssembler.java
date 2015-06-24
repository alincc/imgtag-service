package no.nb.microservices.imgtag.rest.assembler;

import no.nb.microservices.imgtag.config.ApplicationSettings;
import no.nb.microservices.imgtag.model.ImgTag;
import no.nb.microservices.imgtag.rest.controller.ImgTagController;
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
public class ImgTagResourceAssembler implements ResourceAssembler<ImgTagPage, PagedResources<ImgTag>> {

    private final ApplicationSettings applicationSettings;

    @Autowired
    public ImgTagResourceAssembler(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public PagedResources<ImgTag> toResource(ImgTagPage imgTagPage) {
        Page page = imgTagPage.getPage();

        Collection<ImgTag> resources = new ArrayList<ImgTag>();
        for(Object o : page.getContent()) {
            ImgTag imgTag = (ImgTag)o;
            imgTag.add(linkTo(ImgTagController.class).slash(imgTag.getTagId()).withSelfRel());
            resources.add(imgTag);
        }

        List<Link> links = new ArrayList<Link>();
        links.add(linkTo(methodOn(ImgTagController.class).getImgTags(imgTagPage.getUrn(), imgTagPage.getType(), imgTagPage.getUser(), imgTagPage.getStatus(), imgTagPage.isReported(), page.getNumber(), page.getSize())).withSelfRel());
        if (page.hasPrevious()) {
            links.add(linkTo(methodOn(ImgTagController.class).getImgTags(imgTagPage.getUrn(), imgTagPage.getType(), imgTagPage.getUser(), imgTagPage.getStatus(), imgTagPage.isReported(),   page.previousPageable().getPageNumber(), page.getSize())).withRel("prev"));
        }
        if (page.hasNext()) {
            links.add(linkTo(methodOn(ImgTagController.class).getImgTags(imgTagPage.getUrn(), imgTagPage.getType(), imgTagPage.getUser(), imgTagPage.getStatus(), imgTagPage.isReported(),   page.nextPageable().getPageNumber(), page.getSize())).withRel("next"));
        }
        if (!page.isFirst()) {
            links.add(linkTo(methodOn(ImgTagController.class).getImgTags(imgTagPage.getUrn(), imgTagPage.getType(), imgTagPage.getUser(), imgTagPage.getStatus(), imgTagPage.isReported(),   0, page.getSize())).withRel("first"));
        }
        if (!page.isLast()) {
            links.add(linkTo(methodOn(ImgTagController.class).getImgTags(imgTagPage.getUrn(), imgTagPage.getType(), imgTagPage.getUser(), imgTagPage.getStatus(), imgTagPage.isReported(),   page.getTotalPages() - 1, page.getNumberOfElements())).withRel("last"));
        }

        PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());
        PagedResources<ImgTag> pagedResources = new PagedResources<ImgTag>(resources, metadata, links);

        return pagedResources;
    }
}