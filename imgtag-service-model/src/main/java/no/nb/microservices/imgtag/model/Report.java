package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report {

    private String userID;
    private String userDisplayName;
    private String userEmail;
    private String comment;
    private Date date;

    public Report() {
    }

    public Report(String userID, String userDisplayName, String userEmail, String comment) {
        this.userID = userID;
        this.userDisplayName = userDisplayName;
        this.userEmail = userEmail;
        this.comment = comment;
        this.date = new Date();
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getUserDisplayName() {
        return userDisplayName;
    }
    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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