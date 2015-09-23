package org.jbpm.vdml.services.impl.model.runtime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Type;

import java.util.Locale;

@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String unit;
    private String complexName;
    private String streetNumber;
    private String street;
    private String suburb;
    private String city;
    @Type(type="org.hibernate.spatial.GeometryType")
    private Point location;
    private String localeCode=Locale.getDefault().toLanguageTag();

    public Address() {
    }

    public Address(Point point) {
        this.location=point;
    }

    public Long getId() {
        return id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getComplexName() {
        return complexName;
    }

    public void setComplexName(String complexName) {
        this.complexName = complexName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Point getLocation() {
        return location;
    }
    public Locale getLocale(){
        return Locale.forLanguageTag(localeCode);
    }
    public void setLocale(Locale a){
        this.localeCode=a.toLanguageTag();
    }
    public void setLocation(Point location) {
        this.location = location;
    }
}
