package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mysema.query.annotations.QueryEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
public class PlaceTag {
    private String name;
    private double[] coordinates;

    public PlaceTag() {

    }

    public PlaceTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
}