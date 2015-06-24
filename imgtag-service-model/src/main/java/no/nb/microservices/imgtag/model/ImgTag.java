package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.*;
import com.mysema.query.annotations.QueryEntity;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@XmlRootElement
@Document(collection = "ImgTag")
@JsonPropertyOrder({ "id" })
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties
@QueryEntity
public class ImgTag extends ResourceSupport {

    @Id
    private String id;

    private String userId;
    private String userDisplayName;
    private String userEmail;

    @Indexed
    @NotEmpty
    @Length(max = 100)
    @Pattern(regexp = "URN:NBN:no-nb_.*")
    private String urn;

    @NotEmpty
    @Length(min = 32, max = 32)
    private String sesamId;

    @Transient
    @Length(max = 160)
    private String title;

    @NotEmpty
    @Length(max = 30)
    private String type;

    private Date date;

    @Length(max = 300)
    private String comment;

    private Status status;
    private List<Status> statusHistory;
    private List<String> subjects;
    private double[] coordinates;

    @NotNull
    private Point point;

    private Place place;
    private Person person;

    private Boolean reported = false;
    private List<Report> reports;

    @JsonProperty("id")
    public String getTagId() {
        return id;
    }

    public void setTagId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Status> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<Status> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    /**
     * Coordinates must contain longitude and latitude with longitude in first position
     * @param double[] coordinates
     */
    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Boolean getReported() {
        return reported;
    }

    public void setReported(Boolean reported) {
        this.reported = reported;
    }

    @JsonIgnore
    public double getLongitude() {
        if (this.coordinates != null && this.coordinates.length == 2) {
            return this.coordinates[0];
        }
        return 0;
    }

    @JsonIgnore
    public double getLatitude() {
        if (this.coordinates != null && this.coordinates.length == 2) {
            return this.coordinates[1];
        }
        return 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("id", id).append("comment", comment)
                .append("userId", userId).append("date", date)
                .append("comment", comment).toString();
    }

    public String getSesamId() {
        return sesamId;
    }

    public void setSesamId(String sesamId) {
        this.sesamId = sesamId;
    }

    public String getTitle() {
        if (this.person != null) {
            return this.person.getFirstname() + " " + this.person.getSurname();
        }
        else if (this.place != null) {
            return this.place.getName();
        }
        else if (this.point != null) {
            return this.point.getDescription();

        }
        else {
            return "";
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public void mask() {
        this.reported = null;
        this.userEmail = null;
        this.status = null;
        this.statusHistory = null;
    }
}