package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;import java.lang.String;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@Document
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place {
    private String name;

    public Place() {}

    public Place(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}