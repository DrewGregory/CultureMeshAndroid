package org.codethechange.culturemesh.models;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by nathaniel on 11/10/17.
 */

public class Location implements Serializable{
    private BigInteger id;
    private String country;
    private String region;
    private String city;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    private Point[] points;

    public Location(String country, String region, String city, Point[] points) {
        this.country = country;
        this.region = region;
        this.city = city;
        this.points = points;
    }

    public Location() {
        this.country = "";
        this.region = "";
        this.city = "";
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public String toString() {
        String string = "";
        string += getCountry();
        String region  = getRegion();
        if (region != null) {
            string += ", " + region;
        }
        if (city != null) {
            string += ", " + city;
        }
        return string;
    }

    public String shortName() {
        //We'll return the lowest level location.
        String city = getCity();
        if (city != null) return city;
        String region = getRegion();
        if (region != null) return region;
        return getCountry();


    }
}

