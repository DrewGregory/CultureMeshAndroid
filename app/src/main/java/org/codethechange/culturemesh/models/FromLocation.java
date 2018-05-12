package org.codethechange.culturemesh.models;

/**
 * Wrapper for {@code DatabaseLocation} that is for From locations. See the documentation for
 * {@code DatabaseLocation} for information as to why this redundancy is necessary. All of these
 * instance fields will be stored in the local cached database.
 */
public class FromLocation extends DatabaseLocation {

    /**
     * These instance fields mirror those in Location, but are needed for database storage
     */
    public long from_country_id;
    public long from_region_id;
    public long from_city_id;

    /**
     * Name of the country specified by the location
     */
    public String from_country;

    /**
     * Name of the region specified by the location
     */
    public String from_region;

    /**
     * Name of the city specified by the location
     */
    public String from_city;

    /**
     * Population of the location
     */
    public long from_population;

    // TODO: Handle undefined geographical areas (e.g. no region defined)
    /**
     * Initialize instance fields with provided parameters
     * @param cityId ID of city
     * @param regionId ID of region
     * @param countryId ID of country
     * @param cityName Name of city
     * @param regionName Name of region
     * @param countryName Name of country
     * @param population Population of location
     */
    public FromLocation(long cityId, long regionId,long countryId, String cityName,
                        String regionName, String countryName, long population) {
        super(cityId, regionId, countryId);
        initialize(cityName, regionName, countryName, population);
    }

    /**
     * Initialize this class's instance fields based on those provided and those from superclass
     * methods. This is what keeps the instance fields in sync with those of Location.
     * @param cityName Name of city
     * @param regionName Name of region
     * @param countryName Name of country
     * @param population Population of location
     */
    private void initialize(String cityName, String regionName, String countryName, long population) {
        from_country_id = getCountryId();
        from_region_id = getRegionId();
        from_city_id = getCityId();

        from_country = countryName;
        from_region = regionName;
        from_city = cityName;

        from_population = population;
    }

    /**
     * Get the name of the city
     * @return City name
     */
    public String getCityName() {
        return from_city;
    }

    /**
     * Get the name of the region
     * @return Region name
     */
    public String getRegionName() {
        return from_region;
    }

    /**
     * Get the name of the country
     * @return Country name
     */
    public String getCountryName() {
        return from_country;
    }

    /**
     * Get the population of the location
     * @return Location population
     */
    public long getNumUsers() {
        return from_population;
    }

}
