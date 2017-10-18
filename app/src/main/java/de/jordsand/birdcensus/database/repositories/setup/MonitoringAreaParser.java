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

import de.jordsand.birdcensus.core.Location;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.infrastructure.MalformedXMLException;
import de.jordsand.birdcensus.infrastructure.XMLParser;

/**
 * Parser to extract the {@link MonitoringArea} information from a XML file
 * @author Rico Bergmann
 */
public class MonitoringAreaParser implements XMLParser<MonitoringArea> {
    private static final String TAG = MonitoringAreaParser.class.getSimpleName();
    private static final String START_TAG = "AreaList";
    private static final String AREA_TAG = "MonitoringArea";
    private static final String AREA_NAME_ATTR = "name";
    private static final String AREA_CODE_ATTR = "code";
    private static final String POSITION_TAG = "position";
    private static final String POSITION_LON_ATTR = "lon";
    private static final String POSITION_LAT_ATTR = "lat";

    @Override
    public List<MonitoringArea> parse(InputStream in) {
        Element areaList = openXml(in);
        NodeList areaNodes = areaList.getElementsByTagName(AREA_TAG);
        return readNodes(areaNodes);
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
     * @param areaNodes the tree to read
     * @return all extracted monitoring areas
     */
    private List<MonitoringArea> readNodes(NodeList areaNodes) {
        List<MonitoringArea> areas = new ArrayList<>(areaNodes.getLength());

        for (int i = 0; i < areaNodes.getLength(); ++i) {
            Node currArea = areaNodes.item(i);
            areas.add(readAttributes(currArea));
        }
        return areas;
    }

    /**
     * Extracts the area description from an XML node
     * @param node the node
     * @return the monitoring area
     * @throws MalformedXMLException if the node did not match the expected XML schema
     */
    private MonitoringArea readAttributes(Node node) {
        String name = null, code = null;
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.equals(AREA_NAME_ATTR)) {
                name = attr.getNodeValue();
            } else if (attrName.equals(AREA_CODE_ATTR)) {
                code = attr.getNodeValue();
            }
        }

        if (name == null || code == null) {
            throw new MalformedXMLException("Missing name or code attribute");
        }

        Location location = readLocation(node);

        return new MonitoringArea(name, code, location);
    }

    /**
     * Extracts the position from an area node. The position is expected to be a sub-node.
     * @param node the node
     * @return the location
     * @throws MalformedXMLException if the node did not match the expected XML schema
     */
    private Location readLocation(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(POSITION_TAG)) {
                return readLocationAttributes(child);
            }
        }
        throw new MalformedXMLException("Missing position node");
    }

    /**
     * Extracts the position from an XML node
     * @param node the node
     * @return the location
     * @throws MalformedXMLException if the node did not match the expected XML schema
     */
    private Location readLocationAttributes(Node node) {
        Double lon = null, lat = null;
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.equals(POSITION_LON_ATTR)) {
                lon = Double.parseDouble(attr.getNodeValue());
            } else if (attrName.equals(POSITION_LAT_ATTR)) {
                lat = Double.parseDouble(attr.getNodeValue());
            }
        }

        if (lon == null || lat == null) {
            throw new MalformedXMLException("Missing latitude or longitude attribute");
        }
        return new Location(lat, lon);
    }

}
