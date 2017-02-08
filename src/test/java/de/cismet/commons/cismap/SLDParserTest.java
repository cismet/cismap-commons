package de.cismet.commons.cismap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.deegree.style.persistence.sld.SLDParser;
import org.deegree.style.se.unevaluated.Style;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pd
 */
public class SLDParserTest {

    public SLDParserTest() {
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
    public void test010SLDParser() throws XMLStreamException, IOException {
        assertNotNull(this.getClass().getResource("/testSLD.xml"));
        final InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/testSLD.xml"));
        assertTrue(inputStreamReader.ready());
        
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(inputStreamReader);
        assertTrue(xmlStreamReader.hasNext());
        
        final Map<String, LinkedList<Style>> stylesMap 
                = SLDParser.getStyles(xmlStreamReader);
        assertFalse(stylesMap.isEmpty());
        assertTrue(stylesMap.containsKey("default"));
        
        final LinkedList<Style> stylesList = stylesMap.values().iterator().next();
        assertFalse(stylesList.isEmpty());
        
        final Style style = stylesList.element();
        assertEquals("default", style.getName());
        assertFalse(style.getRules().isEmpty());
    }
}
