package java.de.cismet.commons.cismap.io.converters;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author mscholl
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    de.cismet.commons.cismap.io.converters.AbstractGeometryFromTextConverterTest.class,
    de.cismet.commons.cismap.io.converters.PolygonFromTextConverterTest.class, 
    de.cismet.commons.cismap.io.converters.PointFromTextConverterTest.class, 
    de.cismet.commons.cismap.io.converters.BoundingBoxFromTextConverterTest.class, 
    de.cismet.commons.cismap.io.converters.PolylineFromTextConverterTest.class,
    de.cismet.commons.cismap.io.converters.GeomFromWkbAsHexTextConverterTest.class, 
    de.cismet.commons.cismap.io.converters.GeomFromWktConverterTest.class
})
public class ConvertersSuite
{

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
    
}
