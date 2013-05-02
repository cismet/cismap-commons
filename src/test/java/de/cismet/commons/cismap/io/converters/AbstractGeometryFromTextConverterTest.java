package de.cismet.commons.cismap.io.converters;



import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import de.cismet.commons.converter.ConversionException;
import java.text.NumberFormat;
import java.util.Locale;
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
public class AbstractGeometryFromTextConverterTest
{

    public AbstractGeometryFromTextConverterTest()
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
        
        final AbstractGeometryFromTextConverterImpl conv = new AbstractGeometryFromTextConverterImpl();
        
        String from = "1 2:3;4\t5\n6  7     \n\t\n\t\t\n    8";
        String[] params = new String[] {"4326"};
        
        conv.convertForward(from, params);
        
        Coordinate[] expected = new Coordinate[]{
            new Coordinate(1, 2),
            new Coordinate(3, 4),
            new Coordinate(5, 6),
            new Coordinate(7, 8)
        };
        
        assertEquals(4, conv.coordinates.length);
        assertArrayEquals(expected, conv.coordinates);
        assertEquals(4326, conv.geomFactory.getSRID());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardNullFrom() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        AbstractGeometryFromTextConverter conf = new AbstractGeometryFromTextConverterImpl();
        
        conf.convertForward(null, (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardEmptyFrom() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        AbstractGeometryFromTextConverter conf = new AbstractGeometryFromTextConverterImpl();
        
        conf.convertForward("", (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardNullParams() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        AbstractGeometryFromTextConverter conf = new AbstractGeometryFromTextConverterImpl();
        
        conf.convertForward("abc", (String[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConvertForwardTooFewParams() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        AbstractGeometryFromTextConverter conf = new AbstractGeometryFromTextConverterImpl();
        
        conf.convertForward("abc", new String[]{});
    }

    @Test(expected=ConversionException.class)
    public void testConvertForwardUnEvenNumberOfCoords() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        AbstractGeometryFromTextConverter conf = new AbstractGeometryFromTextConverterImpl();
        
        conf.convertForward("abcde", "12345");
    }

    @Test(expected=ConversionException.class)
    public void testConvertForwardUnsupportedEPSG() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        AbstractGeometryFromTextConverter conf = new AbstractGeometryFromTextConverterImpl();
        
        conf.convertForward("abcde fghij", "myEpsg");
    }

    @Test
    public void testConvertBackward() throws Exception
    {
        System.out.println("TEST " + getCurrentMethodName());
        
        final AbstractGeometryFromTextConverterImpl conv = new AbstractGeometryFromTextConverterImpl();
        
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        
        GeometryFactory gf = new GeometryFactory(pm, 4326);
        Geometry g = gf.createPoint(new Coordinate(54.65, 23.44));
        String result = conv.convertBackward(g, (String[])null);
        String expected = createExpected(g);
        assertEquals(expected, result);
        
        g = gf.createLineString(new Coordinate[] {});
        result = conv.convertBackward(g, "");
        expected = createExpected(g);
        assertEquals(expected, result);
        
        g = gf.createLineString(new Coordinate[] {
            new Coordinate(33.33, 44.44),
            new Coordinate(33.32, 2),
            new Coordinate(37.33, 4),
            new Coordinate(32.45, 22),
            new Coordinate(3.9, 55.3),
            new Coordinate(1.0, 44.45),
        });
        result = conv.convertBackward(g, "");
        expected = createExpected(g);
        assertEquals(expected, result);
    }
    
    private String createExpected(final Geometry g){
        StringBuilder expected = new StringBuilder();
        
        final String[] coordString = getCoorindateString(g.getCoordinates());
        for(int i = 0; i < coordString.length; ++i)
        {
            expected.append(coordString[i]);
            expected.append(i % 2 == 0 ? ' ' : '\n');
        }
        
        return expected.toString();
    }
    
    private String[] getCoorindateString(final Coordinate[] coords){
        final NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        final String[] result = new String[coords.length * 2];
        for(int i = 0; i < coords.length; ++i){
            result[i * 2] = nf.format(coords[i].x);
            result[(i * 2) + 1] = nf.format(coords[i].y);
        }
        
        return result;
    }

    private class AbstractGeometryFromTextConverterImpl extends AbstractGeometryFromTextConverter
    {
        Coordinate[] coordinates;
        GeometryFactory geomFactory;
        
        public Geometry createGeometry(Coordinate[] coordinates, GeometryFactory geomFactory) throws ConversionException
        {
            this.coordinates = coordinates;
            this.geomFactory = geomFactory;
            
            return null;
        }

        @Override
        public String getFormatName()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFormatDisplayName()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFormatHtmlName()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFormatDescription()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFormatHtmlDescription()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getFormatExample()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}