package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@Document
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Point {
    private String description;
    private int pgid;
    private int x;
    private int y;

    public Point() {

    }

    public Point(String description, int pgid, int x, int y) {
        this.description = description;
        this.pgid = pgid;
        this.x = x;
        this.y = y;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getPgid() {
        return pgid;
    }
    public void setPgid(int pgid) {
        this.pgid = pgid;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
}