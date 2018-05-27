package org.codethechange.culturemesh.models;

import android.arch.persistence.room.Entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link Country} is a specific kind of {@link Place} that stores the ID and name of a country.
 * No instance field should ever be set to {@link Place#NOWHERE}.
 */
@Entity
public class Country extends Place {
    // TODO: How does the isoA2 long get turned into 2-letter country codes?

    /**
     * 2-Letter ISO country code. This is not currently used.
     */
    public long isoA2;

    /**
     * Name of country
     */
    public String name;

    /**
     * Initialize instance fields and those of superclass with provided parameters
     * @param id ID of country
     * @param name Name of country
     * @param latLng Latitude and longitude coordinates of the region
     * @param population Population of the region
     * @param featureCode Region's feature code
     * @param isoA2 2-Letter ISO country code
     */
    public Country(long id, String name, Point latLng, long population, String featureCode, long isoA2) {
        super(id, Location.NOWHERE, Location.NOWHERE, latLng, population, featureCode);
        this.name = name;
        this.isoA2 = isoA2;
    }

    /**
     * Initialize instance fields and those of superclass based on provided JSON
     * It requires that the key {@code name} exist, as its value will be used as the country's name
     * @param json JSON object describing the country to create
     * @throws JSONException May be thrown in response to invalid JSON object
     */
    public Country(JSONObject json) throws JSONException {
        super(json);
        this.name = json.getString("name");
        this.isoA2 = json.getLong("iso_a2");
    }

    /**
     * Empty constructor for database use only. This should never be called by our code.
     */
    public Country() {}


    /**
     * Get name of country, which is suitable for display in UI. Equivalent to
     * {@link Country#getName()}, but required to implement
     * {@link org.codethechange.culturemesh.Listable}
     * @return Name of country
     */
    public String getListableName() {
        return name;
    }

    /**
     * Get name of country
     * @return Name of country
     */
    public String getName() {
        return name;
    }
}
