package de.cismet.commons.cismap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.IOUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.feature.DefaultFeature;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author pd
 */
public class GMLFeatureCollectionTest {

    public GMLFeatureCollectionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void test010SLDParser() throws XMLStreamException, IOException, SAXException, XMLParsingException {
        assertNotNull(this.getClass().getResource("/wfsResponse.xml"));
        final InputStreamReader inputStreamReader
                = new InputStreamReader(this.getClass().getResourceAsStream("/wfsResponse.xml"));
        assertTrue(inputStreamReader.ready());

        final String wfsResponse = IOUtils.toString(inputStreamReader);
        assertFalse(wfsResponse.isEmpty());

        final GMLFeatureCollectionDocument featureCollectionDocument = new GMLFeatureCollectionDocument();
        final StringReader stringReader = new StringReader(wfsResponse);
        featureCollectionDocument.load(stringReader, "http://dummyID");
        assertFalse(featureCollectionDocument.getFeatureCount() == 0);
   
        final FeatureCollection featureCollection = featureCollectionDocument.parse();
        assertTrue(featureCollection.size() == 2);
        
        final Feature feature = featureCollection.getFeature(0);
        assertEquals("ID_9699", feature.getId());
        assertEquals("route", feature.getFeatureType().getName().getLocalName());
        assertEquals(6, feature.getProperties().length);
        
        final FeatureProperty featureProperty = feature.getProperties()[1];
        assertEquals("gwk", featureProperty.getName().getLocalName());
        assertEquals("345328242123", featureProperty.getValue());
    }
}
