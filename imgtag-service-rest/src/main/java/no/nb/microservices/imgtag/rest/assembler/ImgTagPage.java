package no.nb.microservices.imgtag.rest.assembler;

import no.nb.microservices.imgtag.model.ImgTag;
import org.springframework.data.domain.Page;

/**
 * Created by Andreas Bjørnådal (andreasb) on 27.08.14.
 */
public class ImgTagPage {

    private Page<ImgTag> page;
    private String urn;
    private String type;
    private String user;
    private Boolean reported;
    private String status;

    public ImgTagPage(Page<ImgTag> page, String urn, String type, String user, Boolean reported, String status) {
        this.page = page;
        this.urn = urn;
        this.type = type;
        this.user = user;
        this.reported = reported;
        this.status = status;
    }

    public Page<ImgTag> getPage() {
        return page;
    }

    public void setPage(Page<ImgTag> page) {
        this.page = page;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Boolean isReported() {
        return reported;
    }

    public void setReported(Boolean reported) {
        this.reported = reported;
    }

    public Boolean getReported() {
        return reported;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
