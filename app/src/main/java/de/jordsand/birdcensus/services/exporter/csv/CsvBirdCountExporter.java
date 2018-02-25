package de.jordsand.birdcensus.services.exporter.csv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.services.census.BirdCountIterator;
import de.jordsand.birdcensus.services.census.BirdCountSummaryIterator;
import de.jordsand.birdcensus.core.Observation;
import de.jordsand.birdcensus.services.exporter.BirdCountExporter;

/**
 * A {@link BirdCountExporter} which prints the observations into a CSV file.
 *
 * As CSV files only contain data in one format, meta data such as the weather, observer, etc. will
 * not be exported. The first row printed will contain the column names.
 */
public class CsvBirdCountExporter implements BirdCountExporter {

    private final CSVPrinter printer;
    private final String colArea;
    private final String colSpeciesName;
    private final String colSpeciesScientific;
    private final String colCount;

    /**
     * Creates a new exporter
     * @param outputStream the stream to print the data into
     * @param ctx application context. Necessary to localize the data printed (i.e. the column names)
     */
    public CsvBirdCountExporter(@NonNull OutputStreamWriter outputStream, Context ctx) {
        try {
            this.printer = new CSVPrinter(outputStream, CSVFormat.DEFAULT);
        } catch (IOException e) {
            throw new ExportException(e);
        }

        this.colArea = ctx.getString(R.string.census_export_column_area);
        this.colSpeciesName = ctx.getString(R.string.census_export_column_species);
        this.colSpeciesScientific = ctx.getString(R.string.census_export_column_species_scientific);
        this.colCount = ctx.getString(R.string.census_export_column_count);
    }

    @Override
    public void exportCompleteBirdCount(BirdCount birdCount) {
        BirdCountIterator iterator = BirdCountIterator.forCensus(birdCount);

        try {
            // print header
            printer.printRecord(colArea, colSpeciesName, colSpeciesScientific, colCount);

            // print observations
            while (iterator.hasNext()) {
                Observation obs = iterator.next();
                printer.printRecord(obs.getLocation().getCode(), obs.getSpecies().getName(), obs.getSpecies().getScientificName(), obs.getCount());
            }
            printer.close();
        } catch (IOException e) {
            throw new ExportException(e);
        }
    }

    @Override
    public void exportBirdCountSummary(BirdCount birdCount) {
        BirdCountSummaryIterator iterator = BirdCountSummaryIterator.forCensus(birdCount);

        try {
            // print header
            printer.printRecord(colSpeciesName, colSpeciesScientific, colCount);

            // print the observations
            while (iterator.hasNext()) {
                Pair<Species, Integer> obs = iterator.next();
                Species species = obs.first;
                printer.printRecord(species.getName(), species.getScientificName(),obs.second);
            }
            printer.close();
        } catch (IOException e) {
            throw new ExportException(e);
        }
    }

}
