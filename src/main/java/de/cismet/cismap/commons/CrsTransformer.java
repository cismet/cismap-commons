package de.cismet.cismap.commons;

import com.vividsolutions.jts.geom.LineString;
import java.security.InvalidParameterException;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.Point;

/**
 *
 * @author therter
 */
public class CrsTransformer {
    private GeoTransformer transformer;
    private CoordinateSystem crs;

    public CrsTransformer(String destCrs) throws UnknownCRSException, InvalidParameterException {
        transformer = new GeoTransformer(destCrs);
        crs = CRSFactory.create(destCrs);
    }


    public BoundingBox transformBoundingBox(BoundingBox bbox, String sourceCrs)
            throws UnknownCRSException, CRSTransformationException, IllegalArgumentException{
        CoordinateSystem coordSystem = CRSFactory.create(sourceCrs);
        Point minPoint = GeometryFactory.createPoint(bbox.getX1(), bbox.getY1(), coordSystem);
        Point maxPoint = GeometryFactory.createPoint(bbox.getX2(), bbox.getY2(), coordSystem);
        minPoint = (org.deegree.model.spatialschema.Point)transformer.transform(minPoint);
        maxPoint = (org.deegree.model.spatialschema.Point)transformer.transform(maxPoint);
        BoundingBox newBbox;

        if (bbox instanceof XBoundingBox) {
            newBbox = new XBoundingBox(minPoint.getX(), minPoint.getY(), maxPoint.getX(),
                maxPoint.getY(), crs.getIdentifier(), crs.getAxisUnits()[0].equals(Unit.METRE));
        } else {
            newBbox = new BoundingBox(minPoint.getX(), minPoint.getY(), maxPoint.getX(), maxPoint.getY());
        }

        return newBbox;
    }


    public XBoundingBox transformBoundingBox(XBoundingBox bbox)
            throws UnknownCRSException, CRSTransformationException, IllegalArgumentException {
        return (XBoundingBox)transformBoundingBox(bbox, bbox.getSrs());
    }

    public com.vividsolutions.jts.geom.Geometry transformGeometry(com.vividsolutions.jts.geom.Geometry geom, String sourceCrs)
            throws UnknownCRSException, CRSTransformationException, IllegalArgumentException, GeometryException {
        CoordinateSystem coordSystem = CRSFactory.create(sourceCrs);
        Geometry deegreeGeom = JTSAdapter.wrap(geom);
        deegreeGeom = transformer.transform(deegreeGeom, coordSystem.getCRS());

        return JTSAdapter.export(deegreeGeom);
    }


    public com.vividsolutions.jts.geom.Coordinate[] transformGeometry(com.vividsolutions.jts.geom.Coordinate[] coords, String sourceCrs)
            throws UnknownCRSException, CRSTransformationException, IllegalArgumentException, GeometryException {
        com.vividsolutions.jts.geom.GeometryFactory gfac = new com.vividsolutions.jts.geom.GeometryFactory();
        com.vividsolutions.jts.geom.Geometry geom;

        if (coords.length == 1) {
            geom = gfac.createPoint(coords[0]);
        } else {
            geom = gfac.createLineString(coords);
        }

        geom = transformGeometry(geom, sourceCrs);
        return geom.getCoordinates();
    }
}