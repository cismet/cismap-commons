package de.cismet.cismap.commons.rasterservice;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.deegree.io.geotiff.GeoTiffException;
import org.deegree.io.geotiff.GeoTiffReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pd
 */
public class ImageFileRetrievalTest {

    public ImageFileRetrievalTest() {
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
    public void testGeoTiffReader() throws IOException, GeoTiffException, URISyntaxException {
        
        final String geoTiffName = "geotiff.tif";
        assertNotNull(this.getClass().getResource(geoTiffName));
        
        File file = new File(this.getClass().getResource(geoTiffName).toURI());
        assertTrue(file.canRead());

        final GeoTiffReader geoTiffReader = new GeoTiffReader(file);
        assertNotNull(geoTiffReader);
        assertNotNull(geoTiffReader.getTIFFImage());
        
        //System.out.println( geoTiffReader.getGTModelTypeGeoKey());
        //System.out.println( geoTiffReader.getBoundingBox().toString());
        
        assertEquals("<empty>", geoTiffReader.getHumanReadableCoordinateSystem());
        assertEquals(1, geoTiffReader.getGTModelTypeGeoKey());
        assertEquals("min = Position: 793450.4967766507 6342804.112485806 max = Position: 794471.126986027 6343386.195819735", 
                geoTiffReader.getBoundingBox().toString());
        
        assertEquals(163, geoTiffReader.getTIFFImage().getNumYTiles());
        assertEquals(487, geoTiffReader.getTIFFImage().getHeight());
        assertEquals(0, geoTiffReader.getTIFFImage().getMinTileX());
        assertEquals(1, geoTiffReader.getTIFFImage().getNumXTiles());
        
        
    }
}
