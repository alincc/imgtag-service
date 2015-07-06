package no.nb.microservices.imgtag.model;

import java.util.Date;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
public class PersonTag {
    private String firstname;
    private String surname;
    private String role;
    private Date dateOfBirth;
    private Date dateOfDeath;

    public PersonTag() {

    }

    public PersonTag(String firstname, String surname) {
        this.firstname = firstname;
        this.surname = surname;
    }

    public PersonTag(String firstname, String surname, String role) {
        this.firstname = firstname;
        this.surname = surname;
        this.role = role;
    }

    public PersonTag(String firstname, String surname, String role, Date dateOfBirth, Date dateOfDeath) {
        this.firstname = firstname;
        this.surname = surname;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public Date getDateOfDeath() {
        return dateOfDeath;
    }
    public void setDateOfDeath(Date dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }
}
