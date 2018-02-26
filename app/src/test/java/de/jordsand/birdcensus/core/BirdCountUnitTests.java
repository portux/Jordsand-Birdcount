package de.jordsand.birdcensus.core;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BirdCountUnitTests {

    private MonitoringArea areaMock;
    private Species speciesMock;
    private WeatherData weatherMock;

    private Species kestrel;
    private Species blackbird;
    private Species chaffinch;
    private Species crow;
    private Species[] observations = {kestrel, blackbird, chaffinch};

    private MonitoringArea bay;
    private MonitoringArea pond;

    @Before
    public void setUp() {
        areaMock  = mock(MonitoringArea.class);
        when(areaMock.getCode()).thenReturn("SB");
        when(areaMock.getName()).thenReturn("Southern Bay");

        speciesMock = mock(Species.class);
        when(speciesMock.getName()).thenReturn("Common kestrel");
        when(speciesMock.getScientificName()).thenReturn("Falco tinnunculus");

        weatherMock = mock(WeatherData.class);

        kestrel =  speciesMock;
        blackbird = mock(Species.class);
        when(blackbird.getName()).thenReturn("Common blackbird");
        when(blackbird.getScientificName()).thenReturn("Turdus merula");
        chaffinch = mock(Species.class);
        when(chaffinch.getName()).thenReturn("Common chaffinch");
        when(chaffinch.getScientificName()).thenReturn("Fringilla coelebs");
        crow = mock(Species.class);
        when(crow.getName()).thenReturn("Carrion crow");
        when(crow.getScientificName()).thenReturn("Corvus corone");

        bay = areaMock;
        pond = mock(MonitoringArea.class);
        when(pond.getCode()).thenReturn("GP");
        when(pond.getName()).thenReturn("Great Pond");
    }

    @Test
    public void simpleGettersAreAllImplemented() {
        Date start = Date.from(Instant.now());
        Date end  = Date.from(Instant.now().plus(10L, ChronoUnit.DAYS));
        BirdCount birdCount = new BirdCount(start, end, "Tom Fool", weatherMock, new HashMap<MonitoringArea, WatchList>());

        // don't test the end time here, it is covered by other tests
        assertThat(birdCount.getStartTime()).isEqualTo(start);
        assertThat(birdCount.getObserverName()).isEqualTo("Tom Fool");
        assertThat(birdCount.getWeatherInfo()).isEqualTo(weatherMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsIllegalStartEndTimeCombinations() {
        Date start = Date.from(Instant.now());
        Date end  = Date.from(Instant.now().minus(10L, ChronoUnit.DAYS));
        new BirdCount(start, end, "Tom Fool", weatherMock, new HashMap<MonitoringArea, WatchList>());
    }

    @Test
    public void constructorDoesNotRejectLegalStartTimeEndTimeCombinations() {
        Date start  = Date.from(Instant.now().minus(10L, ChronoUnit.DAYS));
        Date end = Date.from(Instant.now());
        new BirdCount(start, end, "Tom Fool", weatherMock, new HashMap<MonitoringArea, WatchList>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void observedSpeciesAreImmutable() {
        complicatedBirdCount().getObservedSpecies().clear();
    }

    @Test
    public void endTimeIsNullIfOngoing() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        assertThat(birdCount.getEndTime()).isNull();
    }

    @Test
    public void endTimeIsSetAfterTermination() throws InterruptedException {
        Date beforeCreation = Date.from(Instant.now());
        Thread.sleep(100L);
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        Thread.sleep(100L);
        birdCount.terminate();
        Thread.sleep(100L);
        Date afterTermination = Date.from(Instant.now());
        assertThat(birdCount.getEndTime()).isBetween(beforeCreation, afterTermination);
    }

    @Test
    public void observationSummaryDoesNotRequireTermination() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.addToWatchlist(areaMock, speciesMock, 1);
        assertThat(birdCount.getObservationSummary()).isNotNull();
    }

    @Test
    public void observationSummaryIsEmptyIfNoObservations() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.terminate();
        assertThat(birdCount.getObservationSummary()).isEmpty();
    }

    @Test
    public void observationSummaryContainsAllObservations() {
        Species[] observations = {kestrel, blackbird, chaffinch};
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.addToWatchlist(areaMock, kestrel, 1);
        birdCount.addToWatchlist(pond, blackbird, 2);
        birdCount.addToWatchlist(areaMock, chaffinch, 7);

        assertThat(birdCount.getObservationSummary()).hasSize(observations.length);
    }

    @Test
    public void observationSummaryHasTotalizedObservations() {
        BirdCount birdCount = complicatedBirdCount();

        assertThat(birdCount.getObservationSummary()).hasSize(observations.length);
        assertThat(birdCount.getObservationSummary().get(blackbird)).isEqualTo(3);
    }

    @Test
    public void differentSpeciesCountMergesAreas() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getDifferentSpeciesCount()).isEqualTo(observations.length);
    }

    @Test
    public void totalObservationsMergesAreas() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getTotalObservedSpeciesCount()).isEqualTo(11);
    }

    @Test
    public void observedCountMergesAreas() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getObservedCountOf(blackbird)).isEqualTo(3);
    }

    @Test
    public void observedCountIsZeroIfSpeciesUnobserved() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getObservedCountOf(crow)).isZero();
    }

    @Test
    public void observedCountByCodeIsZeroIfAreaIsUnknown() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.addToWatchlist(pond, kestrel, 1);
        assertThat(birdCount.getObservedCountOf(kestrel, bay.getCode())).isZero();
    }

    @Test
    public void observedCountByAreaIsZeroIfAreaIsUnknown() {
        BirdCount birdCount = new BirdCount( //
                Date.from(Instant.now()), //
                "Tom Fool", //
                weatherMock);
        birdCount.addToWatchlist(pond, kestrel, 1);
        assertThat(birdCount.getObservedCountOf(kestrel, bay)).isZero();
    }

    @Test
    public void observedCountForAreaDoesNotMerge() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getObservedCountOf(blackbird, bay)).isEqualTo(1);
    }

    @Test
    public void observedCountForAreaIsZeroIfSpeciesUnobserved() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getObservedCountOf(kestrel, pond)).isZero();
    }

    @Test
    public void observedCountForAreaCodeDoesNotMerge() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getObservedCountOf(blackbird, "GP")).isEqualTo(2);
    }

    @Test
    public void observedCountForAreaCodeIsZeroIfSpeciesUnobserved() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.getObservedCountOf(crow, "GP")).isZero();
    }

    @Test
    public void findsObservedSpecies() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.wasObserved(chaffinch)).isTrue();
    }

    @Test
    public void wasObservedIsFalseIfUnobserved() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.wasObserved(crow)).isFalse();
    }

    @Test
    public void findsObservedSpeciesInArea() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.wasObservedIn(chaffinch, areaMock)).isTrue();
    }

    @Test
    public void wasObservedInIsFalseIfUnobserved() {
        BirdCount birdCount = complicatedBirdCount();

        assertThat(birdCount.wasObservedIn(kestrel, pond)) //
            .describedAs("Species was observed in another area") //
            .isFalse();

        assertThat(birdCount.wasObservedIn(crow, pond)) //
                .describedAs("Species was not observed at all") //
                .isFalse();
    }

    @Test
    public void wasObservedInIsFalseIfUnknownArea() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.addToWatchlist(pond, kestrel, 1);

        assertThat(birdCount.wasObservedIn(kestrel, bay)).isFalse();
    }

    @Test
    public void findsObservedSpeciesInAreaByCode() {
        BirdCount birdCount = complicatedBirdCount();
        assertThat(birdCount.wasObservedIn(chaffinch, areaMock.getCode())).isTrue();
    }

    @Test
    public void wasObservedInForCodeIsFalseIfUnobserved() {
        BirdCount birdCount = complicatedBirdCount();

        assertThat(birdCount.wasObservedIn(kestrel, pond.getCode())) //
                .describedAs("Species was observed in another area") //
                .isFalse();

        assertThat(birdCount.wasObservedIn(crow, pond.getCode())) //
                .describedAs("Species was not observed at all") //
                .isFalse();
    }

    @Test
    public void wasObservedInForCodeIsFalseIfUnknownArea() {
        BirdCount birdCount = new BirdCount( //
                Date.from(Instant.now()), //
                "Tom Fool", //
                weatherMock);
        birdCount.addToWatchlist(pond, kestrel, 1);

        assertThat(birdCount.wasObservedIn(kestrel, bay.getCode())).isFalse();
    }

    @Test
    public void isTerminatedIsFalseIfOngoing() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);

        assertThat(birdCount.isTerminated()).isFalse();
    }

    @Test
    public void isTerminatedUpdatesCorrectly() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.terminate();
        assertThat(birdCount.isTerminated()).isTrue();
    }

    @Test(expected = BirdCountTerminatedException.class)
    public void mayNotTerminateMultipleTimes() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);

        birdCount.terminate();
        birdCount.terminate();
    }

    @Test(expected = BirdCountTerminatedException.class)
    public void mayNotAddObservationsAfterTermination() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.terminate();

        birdCount.addToWatchlist(areaMock, speciesMock, 42);
    }

    @Test
    public void equalsComparesDatesOnly() {
        Date start = Date.from(Instant.now());
        BirdCount firstCount = new BirdCount(start, "Tom Fool", weatherMock);
        BirdCount secondCount = new BirdCount(start, "John Doe", weatherMock);
        assertThat(firstCount).isEqualTo(secondCount);

        BirdCount thirdCount = new BirdCount(Date.from(Instant.now().plusSeconds(1000L)), "John Doe", weatherMock);
        assertThat(firstCount).isNotEqualTo(thirdCount);
    }

    @Test
    public void hashCodeWorksForOngoingAndTerminated() {
        BirdCount birdCount = new BirdCount( //
            Date.from(Instant.now()), //
            "Tom Fool", //
            weatherMock);
        birdCount.hashCode();
        birdCount.terminate();
        birdCount.hashCode();
    }

    @Test
    public void toStringWorksForOngoingAndTerminated() {
        BirdCount birdCount = new BirdCount( //
                Date.from(Instant.now()), //
                "Tom Fool", //
                weatherMock);
        birdCount.toString();
        birdCount.terminate();
        birdCount.toString();
    }

    /**
     * Constructs a bird count with 4 observations:
     * <ul>
     *     <li>1 Common Kestrel in SB</li>
     *     <li>2 Common blackbirds in GB and 1 in SB</li>
     *     <li>7 Common chaffinch in SB</li>
     * </ul>
     * @return the terminated bird count
     */
    private BirdCount complicatedBirdCount() {

        BirdCount birdCount = new BirdCount( //
                Date.from(Instant.now()), //
                "Tom Fool", //
                weatherMock);
        birdCount.addToWatchlist(bay, kestrel, 1);
        birdCount.addToWatchlist(pond, blackbird, 2);
        birdCount.addToWatchlist(bay, chaffinch, 7);
        birdCount.addToWatchlist(bay, blackbird, 1);
        birdCount.terminate();

        return birdCount;
    }

}
