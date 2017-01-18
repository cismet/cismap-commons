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
import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * @author martin.scholl@cismet.de
 */
public class GeomFromWktConverterTest
{

    public GeomFromWktConverterTest()
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
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        final String wkt = "POINT(1.13 2)";
        Geometry resGeom = conv.convertForward(wkt, new String[]{"4326"});
        
        assertNotNull(resGeom);
        assertEquals(4326, resGeom.getSRID());
        assertEquals(1, resGeom.getCoordinates().length);
        assertEquals(new Coordinate(1.13, 2), resGeom.getCoordinates()[0]);
        assertEquals("Point", resGeom.getGeometryType());
        
        final String ewkt = "SRID=3021;POINT(1.13 2)";
        resGeom = conv.convertForward(ewkt, new String[]{"4326"});
        
        assertNotNull(resGeom);
        assertEquals(3021, resGeom.getSRID());
        assertEquals(1, resGeom.getCoordinates().length);
        assertEquals(new Coordinate(1.13, 2), resGeom.getCoordinates()[0]);
        assertEquals("Point", resGeom.getGeometryType());
    }

    @Test(expected=ConversionException.class)
    public void testConvertForwardIllegalEwkt() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        conv.convertForward("SRID=abc;bla", new String[]{"4326"});
    }

    @Test(expected=ConversionException.class)
    public void testConvertForwardIllegalWkt() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        conv.convertForward("abc", new String[]{"4326"});
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardNullFrom() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        conv.convertForward(null, (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardEmptyFrom() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        GeomFromWktConverter conf = new GeomFromWktConverter();
        
        conf.convertForward("", (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardNullParams() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        conv.convertForward("abc", (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardTooFewParams() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        conv.convertForward("abc", new String[]{});
    }

    @Test
    public void testConvertBackward() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        final GeometryFactory gf = new GeometryFactory(pm, 4326);
        
        Geometry geom = gf.createPoint(new Coordinate(1.13, 2));
        
        final String wkt = conv.convertBackward(geom, (String[])null);
        
        assertNotNull(wkt);
        assertEquals("POINT (1.13 2)", wkt);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertBackwardToNull() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final GeomFromWktConverter conv = new GeomFromWktConverter();
        
        conv.convertBackward(null, (String[])null);
    }
}