package no.nb.microservices.imgtag.rest.assembler;

import no.nb.microservices.imgtag.model.ImageTag;
import no.nb.microservices.imgtag.model.ImageTagQuery;
import org.springframework.data.domain.Page;

/**
 * Created by Andreas Bjørnådal (andreasb) on 27.08.14.
 */
public class ImageTagPage {

    private Page<ImageTag> page;
    private ImageTagQuery imageTagQuery;
    private String[] expand;

    public ImageTagPage(Page<ImageTag> page, ImageTagQuery imageTagQuery, String[] expand) {
        this.page = page;
        this.imageTagQuery = imageTagQuery;
        this.expand = expand;
    }

    public Page<ImageTag> getPage() {
        return page;
    }

    public void setPage(Page<ImageTag> page) {
        this.page = page;
    }

    public ImageTagQuery getImageTagQuery() {
        return imageTagQuery;
    }

    public void setImageTagQuery(ImageTagQuery imageTagQuery) {
        this.imageTagQuery = imageTagQuery;
    }

    public String[] getExpand() {
        return expand;
    }

    public void setExpand(String[] expand) {
        this.expand = expand;
    }
}
