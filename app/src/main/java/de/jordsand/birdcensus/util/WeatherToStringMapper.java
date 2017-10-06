package de.jordsand.birdcensus.util;

import android.content.res.Resources;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.WeatherData.*;

/**
 * Helper class to retain the string-resource for a weather info
 * @author Rico Bergmann
 */
public class WeatherToStringMapper {
    private Resources res;

    /**
     * @param res the resources to use
     */
    public WeatherToStringMapper(Resources res) {
        this.res = res;
    }

    /**
     * @param windDirection the wind direction to pretty-print
     * @return the associated text
     */
    public String convertWindDirection(WindDirection windDirection) {
        return res.getStringArray(R.array.wind_dirs)[windDirection.ordinal() + 1];
    }

    /**
     * @param precipitation the precipitation to pretty-print
     * @return the associated text
     */
    public String convertPrecipitation(Precipitation precipitation) {
        return res.getStringArray(R.array.precipitation)[precipitation.ordinal()];
    }

    /**
     * @param visibility the visibility to pretty-print
     * @return the associated text
     */
    public String convertVisibility(Visibility visibility) {
        return res.getStringArray(R.array.visibility)[visibility.ordinal()];
    }

    /**
     * @param glaciationLvl the glaciation level to pretty print
     * @return the associated text
     */
    public String convertGlaciationLevel(GlaciationLevel glaciationLvl) {
        return res.getStringArray(R.array.glaciation_level)[glaciationLvl.ordinal()];
    }

}
