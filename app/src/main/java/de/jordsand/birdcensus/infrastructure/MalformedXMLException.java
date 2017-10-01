package de.jordsand.birdcensus.infrastructure;

/**
 * Exception to indicate that a XML file did not match the schema definition
 * @author Rico Bergmann
 */
public class MalformedXMLException extends RuntimeException {
    public MalformedXMLException() {}

    public MalformedXMLException(String message) {
        super(message);
    }

    public MalformedXMLException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedXMLException(Throwable cause) {
        super(cause);
    }

}
