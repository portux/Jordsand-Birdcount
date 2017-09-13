package de.jordsand.birdcensus.core;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import de.jordsand.birdcensus.util.Assert;

/**
 * Information about the weather during a bird count. Fields should be self-explanatory.
 * @author Rico Bergmann
 * @see BirdCount
 */
public class WeatherData {
    public enum WindDirection {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST
    }

    public enum Precipitation {
        NONE,
        DRIZZLE,
        RAIN
    }

    public enum Visibility {
        CLEAR,
        MISTY,
        FOGGY
    }

    public enum GlaciationLevel {
        NONE,
        RIPARIAN_ZONE,
        COMPLETE
    }

    private double waterGauge;
    private int windStrength;
    private WindDirection windDirection;
    private Precipitation precipitation;
    private Visibility visibility;
    private GlaciationLevel glaciationLevel;

    public WeatherData(@FloatRange(from = 0.0) double waterGauge, @IntRange(from = 0) int windStrength, @NonNull WindDirection windDirection,
                       @NonNull Precipitation precipitation, @NonNull Visibility visibility, @NonNull GlaciationLevel glaciationLevel) {
        Object[] params = {windDirection, precipitation, visibility, glaciationLevel};
        Assert.elemsNotNull(params);
        this.waterGauge = waterGauge;
        this.windStrength = windStrength;
        this.windDirection = windDirection;
        this.precipitation = precipitation;
        this.visibility = visibility;
        this.glaciationLevel = glaciationLevel;
    }

    public double getWaterGauge() {
        return waterGauge;
    }

    public int getWindStrength() {
        return windStrength;
    }

    @NonNull
    public WindDirection getWindDirection() {
        return windDirection;
    }

    @NonNull
    public Precipitation getPrecipitation() {
        return precipitation;
    }

    @NonNull
    public Visibility getVisibility() {
        return visibility;
    }

    @NonNull
    public GlaciationLevel getGlaciationLevel() {
        return glaciationLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeatherData that = (WeatherData) o;

        if (Double.compare(that.waterGauge, waterGauge) != 0) return false;
        if (windStrength != that.windStrength) return false;
        if (windDirection != that.windDirection) return false;
        if (precipitation != that.precipitation) return false;
        if (visibility != that.visibility) return false;
        return glaciationLevel == that.glaciationLevel;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(waterGauge);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + windStrength;
        result = 31 * result + windDirection.hashCode();
        result = 31 * result + precipitation.hashCode();
        result = 31 * result + visibility.hashCode();
        result = 31 * result + glaciationLevel.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WeatherData [" +
                "waterGauge=" + waterGauge +
                "; windStrength=" + windStrength +
                "; windDirection=" + windDirection +
                "; precipitation=" + precipitation +
                "; visibility=" + visibility +
                "; glaciationLevel=" + glaciationLevel +
                ']';
    }
}
