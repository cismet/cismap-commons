package de.cismet.commons.cismap;

import de.cismet.cismap.commons.rasterservice.HTTPImageRetrieval;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.security.WebAccessManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pd
 */
public class ImageRetrievalTest implements RetrievalListener {

    final String wmsURL = "http://www2.demis.nl/wms/wms.asp?wms=WorldMap&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&BBOX=-184,-90,180,90&SRS=EPSG:4326&WIDTH=1471&HEIGHT=728&LAYERS=Countries&STYLES=&FORMAT=image/png&DPI=96&MAP_RESOLUTION=96&FORMAT_OPTIONS=dpi:96&TRANSPARENT=TRUE";
    HTTPImageRetrieval imageRetrieval = null;

    public ImageRetrievalTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        boolean canRead = true;
        imageRetrieval = new HTTPImageRetrieval(this);
        try {
            WebAccessManager.getInstance().doRequest(new URL(wmsURL));
        } catch (Exception ex) {
            System.out.println("WARNING: test010ImageRetieval NOT COMPLETED due to Exception: "
                    + ex.getMessage());
            canRead = false;
        }
        assumeTrue("Can read from " + wmsURL, canRead);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test010ImageRetieval() throws MalformedURLException, IOException {
        // NOI18N
        assertNotNull(imageRetrieval);
        imageRetrieval.setUrl(wmsURL);
        assertEquals(wmsURL, imageRetrieval.getUrl());

        imageRetrieval.start();
        try {
            imageRetrieval.join(6000);
        } catch (InterruptedException ex) {
            System.out.println("test010ImageRetieval(): Image Retrieval aborted: " + ex.getMessage());
            assumeTrue("Can read from " + wmsURL, false);
        }
    }

    @Override
    public void retrievalStarted(RetrievalEvent e) {
        //System.out.println("retrievalStarted: " + e.getPercentageDone());
        assertFalse(e.isIsComplete());
    }

    @Override
    public void retrievalProgress(RetrievalEvent e) {
        //System.out.println("retrievalProgress: " + e.getPercentageDone());
        assertFalse(e.isIsComplete());
    }

    @Override
    public void retrievalComplete(RetrievalEvent e) {
        //System.out.println("retrievalComplete: " + e.getPercentageDone());
        //System.out.println("getContentType: " + e..getContentType());
        //System.out.println("getRetrievedObject: " + e.getRetrievedObject());

        assertTrue("isComplete", e.isIsComplete());
        assertFalse("isHasErrors", e.isHasErrors());
        assertNotNull("getRetrievedObject not null", e.getRetrievedObject());
    }

    @Override
    public void retrievalAborted(RetrievalEvent e) {
        assertFalse("retrievalAborted", e.isIsComplete());
        //assertTrue("retrievalAborted", false);

    }

    @Override
    public void retrievalError(RetrievalEvent e) {
        assertTrue("e.getErrorType()", e.isHasErrors());
        //assertTrue(e.getErrorType(), false);
    }
}
