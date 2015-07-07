package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mysema.query.annotations.QueryEntity;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@QueryEntity
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Status {

    private String userId;
    private String displayName;

    @Indexed
    private String name;

    private String comment;
    private Date date;

    public Status() {
    }

    public Status(String name) {
        this.name = name;
        this.date = new Date();
        this.userId = "auto";
    }

    public Status(String name, String comment) {
        this.name = name;
        this.comment = comment;
        this.date = new Date();
    }

    public Status(String userId, String displayName, String name) {
        this.userId = userId;
        this.displayName = displayName;
        this.name = name;
        this.date = new Date();
    }

    public Status(String userId, String displayName, String name, String comment) {
        this.userId = userId;
        this.displayName = displayName;
        this.name = name;
        this.comment = comment;
        this.date = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}