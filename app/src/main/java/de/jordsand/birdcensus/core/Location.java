package de.jordsand.birdcensus.core;

/**
 * Just a location consisting of longitude and latitude
 * @author Rico Bergmann
 */
public class Location {
    private double longitude;
    private double latitude;

    /**
     * @param latitude the latitude
     * @param longitude the longitude
     */
    public Location(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }
}
