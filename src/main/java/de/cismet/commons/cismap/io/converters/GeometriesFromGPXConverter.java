/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.gpx.helper.GpxReader;

import de.cismet.commons.converter.ConversionException;

/**
 * Creates one or more point geometries from the provided coordinates. At least one coordinate is expected.
 *
 * @author   thorsten.herter@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class GeometriesFromGPXConverter extends AbstractGeometryFromTextConverter
        implements MultiGeometriesProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(GeometriesFromGPXConverter.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry createGeometry(final Coordinate[] coordinates, final GeometryFactory geomFactory)
            throws ConversionException {
        // this will never be used in this converter
        if (coordinates.length < 1) {
            throw new ConversionException("cannot create point from empty coordinate array"); // NOI18N
        }

        final List<Geometry> geomList = new ArrayList<Geometry>();

        for (final Coordinate coord : coordinates) {
            geomList.add(geomFactory.createPoint(coord));
        }

        final Geometry[] geomArray = geomList.toArray(new Geometry[geomList.size()]);
        return new GeometryCollection(geomArray, geomFactory);
    }

    @Override
    public Geometry convertForward(final String from, final String... params) throws ConversionException {
        try {
            final GpxReader reader = new GpxReader();
            final Geometry[] geometries = reader.read(from);
            final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    4326);
            Geometry geometry;

            if (geometries.length == 1) {
                geometry = geometries[0];
            } else {
                geometry = geometryFactory.createGeometryCollection(geometries);
            }

            final Geometry geomInCurrentCrs = CrsTransformer.transformToGivenCrs(geometry, params[0]);
            return geomInCurrentCrs;
        } catch (Exception e) {
            LOG.error("Cannot import gpx file", e);
        }

        return null;
    }

    @Override
    public String getFormatName() {
        return "GeomsFromGPXConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return NbBundle.getMessage(
                PointFromTextConverter.class,
                "GeometriesFromGPXConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        final String desc = NbBundle.getMessage(
                GeometriesFromGPXConverter.class,
                "GeometriesFromGPXConverter.getFormatDescription().returnValue"); // NOI18N
        final String superDesc = super.getFormatDescription();

        return desc + "\n" + superDesc;
    }

    @Override
    public String getFormatHtmlDescription() {
        return NbBundle.getMessage(
                GeometriesFromGPXConverter.class,
                "GeometriesFromGPXConverter.getFormatHtmlDescription().returnValue");
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(
                GeometriesFromGPXConverter.class,
                "GeometriesFromGPXConverter.getFormatExample().returnValue",
                getDecimalSeparator()); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   from    DOCUMENT ME!
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int rate(final String from, final String... params) {
        if (from == null) {
            return 0;
        }

        final GpxReader reader = new GpxReader();

        if (reader.isValid(from)) {
            return 100;
        } else {
            return 0;
        }
    }
}
