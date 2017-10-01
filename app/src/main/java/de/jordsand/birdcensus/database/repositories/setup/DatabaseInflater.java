package de.jordsand.birdcensus.database.repositories.setup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.core.MonitoringAreaRepository;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.SpeciesRepository;

/**
 * Sets up the database with all information that needs to be present during runtime
 * @author Rico Bergmann
 */
public abstract class DatabaseInflater {
    protected SpeciesRepository speciesRepo;
    protected MonitoringAreaRepository areaRepo;

    /**
     * Creates a new inflater which parses information about the management indicator species and
     * the monitoring areas from two different XML files
     * @param managementIndicatorSpeciesXML the stream opening the indicator species XML file
     * @param monitoringAreaXML the stream opening the monitoring area XML file
     * @param speciesRepo the repository to save the parsed species in
     * @param areaRepo the repository to save the parsed monitoring areas in
     * @return the inflater
     */
    public static DatabaseInflater fromXML(InputStream managementIndicatorSpeciesXML, InputStream monitoringAreaXML, SpeciesRepository speciesRepo, MonitoringAreaRepository areaRepo) {
        return new SplitXmlSpecificationInflater(managementIndicatorSpeciesXML, monitoringAreaXML, speciesRepo, areaRepo);
    }

    /**
     * Creates a new inflater which parses a single XML file containing information about monitoring
     * areas as well as management indicator species
     * @param mergedSpecification the stream opening the XML file
     * @param speciesRepo the repository to save the parsed species in
     * @param areaRepo the repository to save the parsed monitoring areas in
     * @return the inflater
     */
    public static DatabaseInflater fromXML(InputStream mergedSpecification, SpeciesRepository speciesRepo, MonitoringAreaRepository areaRepo) {
        return new MergedXmlSpecificationInflater(mergedSpecification, speciesRepo, areaRepo);
    }

    /**
     * The inflater should not be instantiated directly, hence the constructor is private
     * @param speciesRepo the repository used to save the parsed management indicator species
     * @param areaRepo the repository to save the parsed monitoring areas in
     */
    private DatabaseInflater(SpeciesRepository speciesRepo, MonitoringAreaRepository areaRepo) {
        this.speciesRepo = speciesRepo;
        this.areaRepo = areaRepo;
    }

    /**
     * Fills the database
     * @throws IOException if the stream could not be handled properly
     */
    public abstract void inflate() throws IOException;

    /**
     * Implementation of the {@link DatabaseInflater} which uses a single XML file as input source.
     * Corresponds to {@link DatabaseInflater#fromXML(InputStream, SpeciesRepository, MonitoringAreaRepository)}
     * <br><br>
     * <em>This class is not yet implemented!</em>
     */
    private static class MergedXmlSpecificationInflater extends DatabaseInflater {

        MergedXmlSpecificationInflater(InputStream mergedSpecification, SpeciesRepository speciesRepo, MonitoringAreaRepository areaRepo) {
            super(speciesRepo, areaRepo);
        }

        @Override
        public void inflate() throws IOException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

    }

    /**
     * Implementation of the {@link DatabaseInflater} which uses two (different) XML files as
     * input sources. Corresponds to {@link DatabaseInflater#fromXML(InputStream, InputStream, SpeciesRepository, MonitoringAreaRepository)}
     */
    private static class SplitXmlSpecificationInflater extends DatabaseInflater {
        private InputStream speciesXML;
        private InputStream areasXML;
        private ManagementIndicatorSpeciesParser speciesParser;
        private MonitoringAreaParser areaParser;

        SplitXmlSpecificationInflater(InputStream managementIndicatorSpeciesXML, InputStream monitoringAreaXML, SpeciesRepository speciesRepo, MonitoringAreaRepository areaRepo) {
            super(speciesRepo, areaRepo);
            this.speciesXML = managementIndicatorSpeciesXML;
            this.areasXML = monitoringAreaXML;
            this.speciesParser = new ManagementIndicatorSpeciesParser();
            this.areaParser = new MonitoringAreaParser();
        }

        @Override
        public void inflate() throws IOException {
            List<Species> species = speciesParser.parse(speciesXML);
            List<MonitoringArea> areas = areaParser.parse(areasXML);

            for (Species s : species) {
                speciesRepo.save(s);
            }

            for (MonitoringArea a : areas) {
                areaRepo.save(a);
            }
        }
    }
}
