/*
 *  Copyright (C) 2010 srichter
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import edu.umd.cs.piccolo.nodes.PImage;
import java.awt.image.BufferedImage;

/**
 *
 * @author srichter
 */
public class DefaultRasterDocumentFeature implements RasterDocumentFeature {

    private static final GeometryFactory GEOM_FACTORY = new GeometryFactory();
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultRasterDocumentFeature.class);

    public DefaultRasterDocumentFeature(BufferedImage rasterDocument, double x, double y) {
        this(rasterDocument, getGeomFromRasterImage(rasterDocument, x, y));
    }

    public DefaultRasterDocumentFeature(BufferedImage rasterDocument, Geometry geom) {
        this(rasterDocument, geom, true, true, false);
    }

    public DefaultRasterDocumentFeature(BufferedImage rasterDocument, Geometry geometry, boolean canBeSelected, boolean editable, boolean hidden) {
        this.rasterDocument = rasterDocument;
        this.geometry = geometry;
        this.canBeSelected = canBeSelected;
        this.editable = editable;
    }
    private final BufferedImage rasterDocument;
    private PImage pImage;
    private Geometry geometry;
    private boolean canBeSelected;
    private boolean editable;
    private boolean hidden;

    @Override
    public BufferedImage getRasterDocument() {
        return rasterDocument;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(Geometry geom) {
        this.geometry = geom;
    }

    @Override
    public boolean canBeSelected() {
        return canBeSelected;
    }

    @Override
    public void setCanBeSelected(boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public void hide(boolean hiding) {
        this.hidden = hiding;
    }

    private static Geometry getGeomFromRasterImage(BufferedImage bi, double x, double y) {
        final int width = bi.getWidth();
        final int height = bi.getHeight();
        Coordinate ursprung0 = new Coordinate(x, y);
        Coordinate x1 = new Coordinate(width + x, y);
        Coordinate xy2 = new Coordinate(width + x, height + y);
        Coordinate y3 = new Coordinate(x, height + y);
        Coordinate ursprung4 = new Coordinate(x, y);
        Coordinate[] coords = new Coordinate[]{ursprung0, x1, xy2, y3, ursprung4};
        LinearRing linearRing = GEOM_FACTORY.createLinearRing(coords);
        Geometry result = GEOM_FACTORY.createPolygon(linearRing, null);
        log.info("Created Geometry: " + result);
        return result;
    }
//    private static Geometry getGeomFromRasterImage(BufferedImage bi) {
//        final int width = bi.getWidth();
//        final int height = bi.getHeight();
//        Coordinate ursprung0 = new Coordinate(0, 0);
//        Coordinate x1 = new Coordinate(width, 0);
//        Coordinate xy2 = new Coordinate(width, height);
//        Coordinate y3 = new Coordinate(0, height);
//        Coordinate ursprung4 = new Coordinate(0, 0);
//        Coordinate[] coords = new Coordinate[]{ursprung0, x1, xy2, y3, ursprung4};
//        LinearRing linearRing = GEOM_FACTORY.createLinearRing(coords);
//        Geometry result = GEOM_FACTORY.createPolygon(linearRing, null);
//        log.info("Created Geometry: " + result);
//        return result;
//    }

    @Override
    public String toString() {
        return "Dokument";
    }
}
