package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A station which will be used for observing the {@link MonitoringArea monitoring areas} during a
 * bird count.
 * @author Rico Bergmann
 */
public class MonitoringStation extends MonitoringArea {
    private List<MonitoringArea> observedAreas;

    public MonitoringStation(@NonNull String name, @NonNull String code, @NonNull Location location) {
        super(name, code, location);
        this.observedAreas = new ArrayList<>();
    }

    @NonNull
    public Iterable<MonitoringArea> getObservedAreas() {
        return observedAreas;
    }

    public void setObservedAreas(@NonNull List<MonitoringArea> observedAreas) {
        this.observedAreas = observedAreas;
    }
}
