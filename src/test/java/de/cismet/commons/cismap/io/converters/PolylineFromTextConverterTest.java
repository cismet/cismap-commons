package de.cismet.commons.cismap.io.converters;



import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import de.cismet.commons.converter.ConversionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class PolylineFromTextConverterTest
{

    public PolylineFromTextConverterTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }


    private String getCurrentMethodName()
    {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    @Test
    public void testCreateGeometry() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final PolylineFromTextConverter conv = new PolylineFromTextConverter();
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        final GeometryFactory gf = new GeometryFactory(pm, 4326);
        
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(1, 2),
            new Coordinate(3, 4),
            new Coordinate(5, 6)
        };
        
        Geometry result = conv.createGeometry(coords, gf);
        
        assertNotNull(result);
        Coordinate[] resCoords = result.getCoordinates();
        Coordinate[] expCoords = new Coordinate[]{
            new Coordinate(1, 2),
            new Coordinate(3, 4),
            new Coordinate(5, 6)
        };
        assertArrayEquals(expCoords, resCoords);
        assertEquals("LineString", result.getGeometryType());
    }

    @Test(expected=ConversionException.class)
    public void testCreateGeometryTooFewCoords() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final PolylineFromTextConverter conv = new PolylineFromTextConverter();
        final Coordinate[] coords = new Coordinate[]{ new Coordinate(1, 2)};
        
        conv.createGeometry(coords, null);
    }
}