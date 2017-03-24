/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.RectangleMath.getPointFromStartByFraction;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.RectangleMath.getPointPerpendicular;
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
public class TestRectangleMath {

    public TestRectangleMath() {
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
    public void testGetPointPerpendicular() {
        final Coordinate[] l = new Coordinate[2];
        l[0] = new Coordinate(0, 0);
        l[1] = new Coordinate(1.5, 1.5);

        final Coordinate pointPerpendicular 
                = getPointPerpendicular(l, getPointFromStartByFraction(l, 0.5), 1);

        assertEquals(0.04289321881345243d, pointPerpendicular.x, 0);
        assertEquals(1.4571067811865475d, pointPerpendicular.y, 0);
        assertEquals(Double.NaN, pointPerpendicular.z, 0);
    }
}
