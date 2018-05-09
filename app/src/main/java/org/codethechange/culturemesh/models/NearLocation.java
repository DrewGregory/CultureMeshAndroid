package org.codethechange.culturemesh.models;

import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import org.codethechange.culturemesh.Listable;

/**
 * Created by Drew Gregory on 2/19/18.
 * Exact copy of Location, but used as Embedded Entity in SQLite Database.
 */

public class NearLocation implements Listable {
    /**
     * When stored in the Database, we will store just the id's. The object returned near the API
     * will have the country, region, and city updated.
     * The default value for country, region, city is 0.
     */
    public long near_country_id;
    public long near_region_id;
    public long near_city_id;

    @Ignore
    public String near_country;
    @Ignore
    public String near_region;
    @Ignore
    public String near_city;

    //TODO: private Point[] points;

    public NearLocation() {

    }

    public NearLocation(String near_country, String near_region, String near_city, Point[] points) {
        this.near_country = near_country;
        this.near_region = near_region;
        this.near_city = near_city;
        //TODO: this.points = points
    }

    public NearLocation(long cityId, long regionId, long countryId) {
        this.near_country = "";
        this.near_region = "";
        this.near_city = "";
        this.near_city_id = cityId;
        this.near_country_id = countryId;
        this.near_region_id = regionId;
    }



    public String toString() {
        String string = "";
        string += near_country;
        String region = this.near_region;
        if (region != null) {
            string += ", " + region;
        }
        if (near_city != null) {
            string += ", " + near_city;
        }
        return string;
    }

    public String shortName() {
        if (this.near_city != null) {
            return this.near_city;
        } else if (this.near_region != null) {
            return this.near_region;
        } else if (this.near_country != null) {
            return this.near_country;
        } else {
            return this.near_city_id + "," + this.near_region_id + "," + this.near_country_id;
        }
    }

    public String getListableName() {
        return shortName();
    }

    public int getNumUsers() {
        // TODO: Store number of members in Location or query API
        return 0;
    }

}
