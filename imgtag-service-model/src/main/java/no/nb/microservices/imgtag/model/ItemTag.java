package no.nb.microservices.imgtag.model;

/**
 * Created by andreasb on 30.06.15.
 */
public class ItemTag {
    private String name;

    public ItemTag() {

    }

    public ItemTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
