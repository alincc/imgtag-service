package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mysema.query.annotations.QueryEntity;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@XmlRootElement
@Document(collection = "ImageTag")
@JsonPropertyOrder({ "tagId" })
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@QueryEntity
public class ImageTag extends ResourceSupport {

    @Id
    private String tagId;

    @Indexed
    @NotEmpty
    @Length(max = 100)
    @Pattern(regexp = "URN:NBN:no-nb_.*")
    private String urn;

    private String userId;
    private String userDisplayName;
    private String userEmail;

    private Date dateCreated;
    private Date dateModified;

    private String type;

    @Length(max = 300)
    private String comment;

    private Status status;
    private Boolean reported = false;
    private List<Report> reports;

    private PointPosition pointPosition;
    private Tag tag;

    @JsonProperty("tagId")
    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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

    /**
     * Coordinates must contain longitude and latitude with longitude in first position
     * @param double[] coordinates
     */

    public Boolean getReported() {
        return reported;
    }

    public void setReported(Boolean reported) {
        this.reported = reported;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("tagId", tagId).append("comment", comment)
                .append("userId", userId).append("dateCreated", dateCreated)
                .append("comment", comment).toString();
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public PointPosition getPointPosition() {
        return pointPosition;
    }

    public void setPointPosition(PointPosition pointPosition) {
        this.pointPosition = pointPosition;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public String getExpand() {
        return "reports";
    }

    public void mask() {
        this.reports = null;
        this.reported = null;
        this.userEmail = null;
        this.status = null;
    }
}