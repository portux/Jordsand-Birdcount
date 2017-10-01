package de.jordsand.birdcensus.database.repositories.setup;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.infrastructure.MalformedXMLException;
import de.jordsand.birdcensus.infrastructure.XMLParser;

/**
 * Parser to extract the {@link Species} information from a XML file
 * @author Rico Bergmann
 */
public class ManagementIndicatorSpeciesParser implements XMLParser<Species> {
    private static final String TAG = ManagementIndicatorSpeciesParser.class.getSimpleName();
    private static final String SPECIES_TAG = "Species";
    private static final String SPECIES_NAME_ATTR = "name";
    private static final String SPECIES_SCIENTIFIC_NAME_ATTR = "scientific";

    @Override
    public List<Species> parse(InputStream in) {
        Element speciesList = openXml(in);
        NodeList speciesNodes = speciesList.getElementsByTagName(SPECIES_TAG);
        return readNodes(speciesNodes);
    }

    /**
     * Sets up the XML stream for further parsing
     * @param in the input source
     * @return the XML tree
     */
    private Element openXml(InputStream in) {
        Document doc;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource();
            source.setCharacterStream(new InputStreamReader(in));
            doc = builder.parse(source);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.e(TAG, "Could not parse: " + e);
            return null;
        }
        return doc.getDocumentElement();
    }

    /**
     * Parses the XML tree
     * @param speciesNodes the tree to read
     * @return all extracted species
     */
    private List<Species> readNodes(NodeList speciesNodes) {
        List<Species> species = new ArrayList<>(speciesNodes.getLength());

        for (int i = 0; i < speciesNodes.getLength(); ++i) {
            Node currSpecies = speciesNodes.item(i);
            species.add(readAttributes(currSpecies));
        }

        return species;
    }

    /**
     * Extracts the species information from an XML node
     * @param node the node
     * @return the species
     * @throws MalformedXMLException if the node did not match the expected XML schema
     */
    private Species readAttributes(Node node) {
        String name = null, scientific = null;
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.equals(SPECIES_NAME_ATTR)) {
                name = attr.getNodeValue();
            } else if (attrName.equals(SPECIES_SCIENTIFIC_NAME_ATTR)) {
                scientific = attr.getNodeValue();
            }
        }

        if (name == null || scientific == null) {
            throw new MalformedXMLException();
        }
        return new Species(name, scientific);
    }
}
