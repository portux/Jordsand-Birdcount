package de.jordsand.birdcensus.services.census;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.BirdCountRepository;
import de.jordsand.birdcensus.core.ExistingSpeciesException;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.core.MonitoringAreaRepository;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.SpeciesRepository;
import de.jordsand.birdcensus.core.WeatherData;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteBirdCountRepository;
import de.jordsand.birdcensus.database.repositories.SQLiteMonitoringAreaRepository;
import de.jordsand.birdcensus.database.repositories.SQLiteSpeciesRepository;

/**
 * Implementation of the {@link BirdCountService} to connect through different activities.
 * <p>
 *     <h2>Overview</h2>
 * If a new bird count is started through an activity, the {@link BirdCount} object would have to be
 * passed to every sub-activity. Thus the {@link BirdCount} class and each subclass would have to
 * implement the {@link android.os.Parcelable} interface for reasonable performance. By using a
 * separate service we circumvent this and provide an elegant solution for accessing the current
 * bird count as well as adding observations.
 * </p>
 * <p>
 *     <h2>Technical details</h2>
 * See: <a href="https://developer.android.com/guide/components/services.html">https://developer.android.com/guide/components/services.html</a>
 * <br>
 * To create a service we could either
 * <ul>
 *     <li>a bound service, or</li>
 *     <li>a started service</li>
 * </ul>
 * What we want is to connect to the service through each activity. It could then modify the current
 * bird count as it needs to. Thus using a bound service seems OK. However we face some implementation
 * problems, as a bound service will automatically destroyed when no more activities are connected to it
 * and each activity should disconnect not later than at {@link android.app.Activity#onDestroy()}.
 * Thus the last connections may already be closed before the newly started activity was able to connect
 * and the service might be closed.
 * To prevent this from happening, we use a started service: the first activity which may need the service
 * will start it if necessary and the service will automatically stop itself when the census is finished.
 * All activities will connect to the service the usual way.
 * </p>
 */
public class SimpleBirdCountService extends Service implements BirdCountService {
    private static final String TAG = SimpleBirdCountService.class.getSimpleName();

    /**
     * Flag to indicate whether the service is started
     */
    private static boolean running = false;

    /**
     * The binder used to connect to the service instance
     */
    private final IBinder mBinder = new BirdCountBinder();

    private BirdCountRepository birdCountRepository;
    private SpeciesRepository speciesRepository;
    private MonitoringAreaRepository areaRepository;

    private BirdCount currentBirdCount = null;

    private LongSparseArray<Species> speciesCache;
    private Map<String, MonitoringArea> areaCache;

    /**
     * @return {@code true} if the service is running, {@code false} otherwise
     */
    public static boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
        birdCountRepository = new SQLiteBirdCountRepository(openHandler.getWritableDatabase());
        speciesRepository = new SQLiteSpeciesRepository(openHandler.getWritableDatabase());
        areaRepository = new SQLiteMonitoringAreaRepository(openHandler.getReadableDatabase());

        speciesCache = new LongSparseArray<>();
        areaCache = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

        return START_REDELIVER_INTENT;
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Nullable @Override
    public BirdCount getCurrentBirdCount() {
        return currentBirdCount;
    }

    @NonNull @Override
    public MonitoringAreaRepository getAreaRepository() {
        return areaRepository;
    }

    @NonNull @Override
    public SpeciesRepository getSpeciesRepository() {
        return speciesRepository;
    }

    @Override
    public void startBirdCount(@NonNull Date startDate, @NonNull String observerName, @NonNull WeatherData weatherData) {
        if (currentBirdCount != null) {
            throw new IllegalStateException("Another bird count has already started");
        }
        currentBirdCount = new BirdCount(startDate, observerName, weatherData);
    }

    @Override
    public void addSightingToCurrentBirdCount(@NonNull String areaCode, @NonNull Species species, int count) {
        if (currentBirdCount == null) {
            throw new IllegalStateException("No bird count ongoing");
        } else if (count == 0) {
            return;
        }
        MonitoringArea area = retrieveMonitoringArea(areaCode);
        currentBirdCount.addToWatchlist(area, species, count);
    }

    @Nullable @Override
    public Species addNewSpecies(@NonNull String name, @Nullable String scientificName) {
        if (scientificName != null && !scientificName.isEmpty() && speciesRepository.findByScientificName(scientificName) != null) {
            throw new ExistingSpeciesException(new Species(name, scientificName));
        } else if (scientificName != null && scientificName.isEmpty() && !speciesRepository.findByName(name).isEmpty()) {
            throw new ExistingSpeciesException(new Species(name, scientificName));
        }

        Species species = new Species(name, scientificName);
        long id = speciesRepository.save(species);
        if (id < 0) {
            return null;
        }
        speciesCache.append(id, species);
        return species;
    }

    @Override
    public void terminateBirdCount() {
        if (currentBirdCount == null) {
            throw new IllegalStateException("No ongoing bird count");
        }
        currentBirdCount.terminate();
        birdCountRepository.save(currentBirdCount);

        currentBirdCount = null;
        running = false;
        stopSelf();
    }

    @Override
    public void abortBirdCount() {
        currentBirdCount = null;
        running = false;
        stopSelf();
    }

    @Override
    public boolean isBirdCountOngoing() {
        return currentBirdCount != null;
    }

    /**
     * Searches for a species by its id
     * @param id the species' id
     * @return the corresponding species
     */
    private Species retrieveSpecies(long id) {
        Species species = speciesCache.get(id);
        if (species == null) {
            species = speciesRepository.findOne(id);

        }
        if (species != null) {
            speciesCache.append(id, species);
        }
        return species;
    }

    /**
     * Searches for a monitoring area by its code
     * @param code the monitoring area's code
     * @return the matching monitoring area
     */
    private MonitoringArea retrieveMonitoringArea(String code) {
        MonitoringArea area = areaCache.get(code);
        if (area == null) {
            area = areaRepository.findOne(code);
        }
        if (area != null) {
            areaCache.put(code, area);
        }
        return area;
    }

    /**
     * Our binder
     */
    public class BirdCountBinder extends Binder {

        /**
         * Provides access to the service instance
         * @return the service
         */
        public SimpleBirdCountService getService() {
            return SimpleBirdCountService.this;
        }

    }
}
