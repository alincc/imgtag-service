package no.nb.microservices.imgtag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.String;import java.util.Date;

/**
 * Created by Andreas Bjørnådal (andreasb) on 19.08.14.
 */
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties
public class Person {
    private String firstname;
    private String surname;
    private String role;
    private Date dob;
    private Date dod;

    public Person() {}

    public Person(String firstname, String surname, String role, Date dob, Date dod) {
        this.firstname = firstname;
        this.surname = surname;
        this.role = role;
        this.dob = dob;
        this.dod = dod;
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
    public Date getDob() {
        return dob;
    }
    public void setDob(Date dob) {
        this.dob = dob;
    }
    public Date getDod() {
        return dod;
    }
    public void setDod(Date dod) {
        this.dod = dod;
    }
}
