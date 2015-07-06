package no.nb.microservices.imgtag.model;

/**
 * Created by andreasb on 03.07.15.
 */
public class Tag {
    private ItemTag itemTag;
    private PersonTag personTag;
    private PlaceTag placeTag;

    public Tag() {
    }

    public Tag(ItemTag itemTag) {
        this.itemTag = itemTag;
    }

    public Tag(PersonTag personTag) {
        this.personTag = personTag;
    }

    public Tag(PlaceTag placeTag) {
        this.placeTag = placeTag;
    }

    public ItemTag getItemTag() {
        return itemTag;
    }

    public void setItemTag(ItemTag itemTag) {
        this.itemTag = itemTag;
    }

    public PersonTag getPersonTag() {
        return personTag;
    }

    public void setPersonTag(PersonTag personTag) {
        this.personTag = personTag;
    }

    public PlaceTag getPlaceTag() {
        return placeTag;
    }

    public void setPlaceTag(PlaceTag placeTag) {
        this.placeTag = placeTag;
    }
}
