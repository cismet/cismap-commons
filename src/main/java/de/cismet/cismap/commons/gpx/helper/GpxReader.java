/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gpx.helper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import de.cismet.cismap.commons.gpx.GpxType;
import de.cismet.cismap.commons.gpx.RteType;
import de.cismet.cismap.commons.gpx.TrkType;
import de.cismet.cismap.commons.gpx.TrksegType;
import de.cismet.cismap.commons.gpx.WptType;
import de.cismet.cismap.commons.jtsgeometryfactories.CoordinateM;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GpxReader {

    //~ Instance fields --------------------------------------------------------

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
            4326);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GpxReader object.
     */
    public GpxReader() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gpx  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isValid(final String gpx) {
        try {
            final JAXBContext context = JAXBContext.newInstance("de.cismet.cismap.commons.gpx");
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final Object o = unmarshaller.unmarshal(new StringReader(gpx));

            return (o instanceof JAXBElement) && GpxType.class.isAssignableFrom(((JAXBElement)o).getDeclaredType());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gpx  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Geometry[] read(final String gpx) throws Exception {
        final List<Geometry> geometries = new ArrayList<>();
        final JAXBContext context = JAXBContext.newInstance("de.cismet.cismap.commons.gpx");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final Object o = unmarshaller.unmarshal(new StringReader(gpx));

        if ((o instanceof JAXBElement) && GpxType.class.isAssignableFrom(((JAXBElement)o).getDeclaredType())) {
            final GpxType gpxType = (GpxType)((JAXBElement)o).getValue();

            if (gpxType.getRte() != null) {
                for (final RteType rte : gpxType.getRte()) {
                    final List<Coordinate> coordinates = new ArrayList<>();

                    for (final WptType wayPoint : rte.getRtept()) {
                        coordinates.add(getCoordinateFromWaypoint(wayPoint));
                    }

                    geometries.add(geometryFactory.createLineString(
                            coordinates.toArray(new Coordinate[coordinates.size()])));
                }
            }

            if (gpxType.getTrk() != null) {
                for (final TrkType trk : gpxType.getTrk()) {
                    for (final TrksegType seg : trk.getTrkseg()) {
                        final List<Coordinate> coordinates = new ArrayList<>();

                        for (final WptType wayPoint : seg.getTrkpt()) {
                            coordinates.add(getCoordinateFromWaypoint(wayPoint));
                        }

                        geometries.add(geometryFactory.createLineString(
                                coordinates.toArray(new Coordinate[coordinates.size()])));
                    }
                }
            }

            if (gpxType.getWpt() != null) {
                for (final WptType wpt : gpxType.getWpt()) {
                    geometries.add(getWaypoint(wpt));
                }
            }
        }

        return geometries.toArray(new Geometry[geometries.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wayPoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point getWaypoint(final WptType wayPoint) {
        return geometryFactory.createPoint(getCoordinateFromWaypoint(wayPoint));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wayPoint  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate getCoordinateFromWaypoint(final WptType wayPoint) {
        final XMLGregorianCalendar calendar = wayPoint.getTime();
        final double timeInMillis = (calendar != null) ? calendar.toGregorianCalendar().getTimeInMillis() : 0.0;
        final double height = (wayPoint.getEle() != null) ? wayPoint.getEle().doubleValue() : 0.0;

        final CoordinateM coord = new CoordinateM(wayPoint.getLon().doubleValue(),
                wayPoint.getLat().doubleValue(),
                height,
                timeInMillis);

        return coord;
    }
}
