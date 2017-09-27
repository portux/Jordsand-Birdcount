package de.jordsand.birdcensus.core;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

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

    private Double waterGauge;
    private Integer windStrength;
    private WindDirection windDirection;
    private Precipitation precipitation;
    private Visibility visibility;
    private GlaciationLevel glaciationLevel;

    public WeatherData(@Nullable @FloatRange(from = 0.0) Double waterGauge, @Nullable @IntRange(from = 0) Integer windStrength, @Nullable WindDirection windDirection,
                       @Nullable Precipitation precipitation, @Nullable Visibility visibility, @Nullable GlaciationLevel glaciationLevel) {
        this.waterGauge = waterGauge;
        this.windStrength = windStrength;
        this.windDirection = windDirection;
        this.precipitation = precipitation;
        this.visibility = visibility;
        this.glaciationLevel = glaciationLevel;
    }

    @Nullable @FloatRange(from = 0.0)
    public Double getWaterGauge() {
        return waterGauge;
    }

    @Nullable @IntRange(from = 0)
    public Integer getWindStrength() {
        return windStrength;
    }

    @Nullable
    public WindDirection getWindDirection() {
        return windDirection;
    }

    @Nullable
    public Precipitation getPrecipitation() {
        return precipitation;
    }

    @Nullable
    public Visibility getVisibility() {
        return visibility;
    }

    @Nullable
    public GlaciationLevel getGlaciationLevel() {
        return glaciationLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeatherData that = (WeatherData) o;

        if (waterGauge != null ? !waterGauge.equals(that.waterGauge) : that.waterGauge != null)
            return false;
        if (windStrength != null ? !windStrength.equals(that.windStrength) : that.windStrength != null)
            return false;
        if (windDirection != that.windDirection) return false;
        if (precipitation != that.precipitation) return false;
        if (visibility != that.visibility) return false;
        return glaciationLevel == that.glaciationLevel;

    }

    @Override
    public int hashCode() {
        int result = waterGauge != null ? waterGauge.hashCode() : 0;
        result = 31 * result + (windStrength != null ? windStrength.hashCode() : 0);
        result = 31 * result + (windDirection != null ? windDirection.hashCode() : 0);
        result = 31 * result + (precipitation != null ? precipitation.hashCode() : 0);
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        result = 31 * result + (glaciationLevel != null ? glaciationLevel.hashCode() : 0);
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
