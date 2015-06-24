package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Andreas Bjørnådal (andreasb) on 15.10.14.
 */
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestInfo {
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
