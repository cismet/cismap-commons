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
 * <code></code>
 * @author martin.scholl@cismet.de
 */
public class GeomFromWkbAsHexTextConverterTest
{

    public GeomFromWkbAsHexTextConverterTest()
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
    public void testConvertForward() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        final String wkb = "010100000014ae47e17a14f23f0000000000000040";
        Geometry resGeom = conv.convertForward(wkb, new String[]{"4326"});
        
        assertNotNull(resGeom);
        assertEquals(4326, resGeom.getSRID());
        assertEquals(1, resGeom.getCoordinates().length);
        assertEquals(new Coordinate(1.13, 2), resGeom.getCoordinates()[0]);
        assertEquals("Point", resGeom.getGeometryType());
        
        final String ewkb = "0101000020cd0b000014ae47e17a14f23f0000000000000040";
        resGeom = conv.convertForward(ewkb, new String[]{"4326"});
        
        assertNotNull(resGeom);
        assertEquals(3021, resGeom.getSRID());
        assertEquals(1, resGeom.getCoordinates().length);
        assertEquals(new Coordinate(1.13, 2), resGeom.getCoordinates()[0]);
        assertEquals("Point", resGeom.getGeometryType());
    }

    @Test(expected=ConversionException.class)
    public void testConvertForwardIllegalWkb() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        conv.convertForward("abc", new String[]{"4326"});
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardNullFrom() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        conv.convertForward(null, (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardEmtpyFrom() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        conv.convertForward("", (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardNullParams() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        conv.convertForward("abc", (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardTooFewParams() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        conv.convertForward("abc", new String[]{});
    }

    @Test
    public void testConvertBackward() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        final GeometryFactory gf = new GeometryFactory(pm, 4326);
        
        Geometry geom = gf.createPoint(new Coordinate(1.13, 2));
        
        final String wkt = conv.convertBackward(geom, (String[])null);
        
        assertNotNull(wkt);
        assertEquals("010100000014ae47e17a14f23f0000000000000040".toLowerCase(), wkt.toLowerCase());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertBackwardToNull() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWkbAsHexTextConverter conv = new GeomFromWkbAsHexTextConverter();
        
        conv.convertBackward(null, (String[])null);
    }
}