package de.cismet.commons.cismap;

import de.cismet.security.WebAccessManager;
import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
import de.cismet.security.exceptions.NoHandlerForURLException;
import de.cismet.security.exceptions.RequestFailedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.IOUtils;
import org.deegree.style.persistence.sld.SLDParser;
import org.deegree.style.se.unevaluated.Style;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author pd
 */
public class WebAccessManagerTest {

    public WebAccessManagerTest() {
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

//    @Test
//    public void test010WebAccessManager() throws MalformedURLException, IOException, Exception {
//        // NOI18N
//        final URL getCapURL = new URL("http://wms.fis-wasser-mv.de/services?REQUEST=GetCapabilities&version=1.1.1&service=WMS");
//
//        InputStream inputStream;
//        try {
//            inputStream = WebAccessManager.getInstance().doRequest(getCapURL);
//            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//            //assertTrue(bufferedReader.ready());
//
//            final String wmsResponse = IOUtils.toString(bufferedReader);
//            assertFalse(wmsResponse.isEmpty());
//            
//            final StringReader stringReader = new StringReader(wmsResponse);
//            
//            final XMLInputFactory factory = XMLInputFactory.newInstance();
//            final XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(stringReader);
//            assertTrue(xmlStreamReader.hasNext());
//
//            //System.out.println(wmsResponse);
//        } catch (UnknownHostException | SocketException ex) {
//            System.out.println("WARNING: test010WebAccessManager NOT COMPLETED due to UnknownHost/SocketException: "
//                    + ex.getMessage());
//        }
//    }
}
