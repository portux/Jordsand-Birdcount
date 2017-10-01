package de.jordsand.birdcensus.infrastructure;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for an XML parser
 * @param <T> the type of objects to parse
 * @author Rico Bergmann
 */
public interface XMLParser<T> {

    /**
     * Parses an input stream containing the desired XML data and converts it into a list of T
     * @param in the stream containing the XML data
     * @return a list consisting of all the parsed objects
     */
    List<T> parse(InputStream in);

}
