/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io.converters;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import de.cismet.cismap.commons.CrsTransformer;

import de.cismet.commons.converter.ConversionException;

/**
 * Creates a geometry from (E)WKT. However, the conversion back from a geometry produces WKT only. If the input is EWKT
 * a possibly provided EPSG is ignored.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
@ServiceProvider(service = TextToGeometryConverter.class)
public final class GeomFromWktConverter extends AbstractRatingConverter<String, Geometry>
        implements TextToGeometryConverter {

    //~ Methods ----------------------------------------------------------------

    // this is because of jalopy as for some reason it generates a javadoc template for this method although overridden
    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry convertForward(final String from, final String... params) throws ConversionException {
        if ((from == null) || from.isEmpty()) {
            throw new IllegalArgumentException("'from' must not be null or empty"); // NOI18N
        }

        if ((params == null) || (params.length < 1)) {
            throw new IllegalArgumentException("no parameters provided, epsgcode is required parameter"); // NOI18N
        }

        final EWKT ewkt;
        try {
            ewkt = getEWKT(from);
        } catch (final RuntimeException e) {
            throw new ConversionException("illegal (e)wkt format: " + from, e); // NOI18N
        }

        final int srid;
        if (ewkt.srid < 0) {
            try {
                srid = CrsTransformer.extractSridFromCrs(params[0]);
            } catch (final Exception e) {
                throw new ConversionException("unsupported epsg parameter: " + params[0], e); // NOI18N
            }
        } else {
            srid = ewkt.srid;
        }

        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        final WKTReader wktReader = new WKTReader(geomFactory);
        try {
            return wktReader.read(ewkt.wkt);
        } catch (final ParseException ex) {
            throw new ConversionException("cannot create geometry from WKT: " + ewkt.wkt, ex); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   candidate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private EWKT getEWKT(final String candidate) {
        final EWKT ewkt = new EWKT();

        final int skIndex = candidate.indexOf(';');
        if (skIndex > 0) {
            final String sridKV = candidate.substring(0, skIndex);
            final int eqIndex = sridKV.indexOf('=');

            if (eqIndex > 0) {
                ewkt.srid = Integer.parseInt(sridKV.substring(eqIndex + 1));
                ewkt.wkt = candidate.substring(skIndex + 1);
            } else {
                ewkt.wkt = candidate;
            }
        } else {
            ewkt.wkt = candidate;
        }

        return ewkt;
    }

    @Override
    public String convertBackward(final Geometry to, final String... params) throws ConversionException {
        if (to == null) {
            throw new IllegalArgumentException("'to' must not be null"); // NOI18N
        }

        final WKTWriter wktWriter = new WKTWriter();

        return wktWriter.write(to);
    }

    @Override
    public String getFormatName() {
        return "GeomFromWktConverter"; // NOI18N
    }

    @Override
    public String getFormatDisplayName() {
        return NbBundle.getMessage(
                GeomFromWktConverter.class,
                "GeomFromWktConverter.getFormatDisplayName().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlName() {
        return null;
    }

    @Override
    public String getFormatDescription() {
        return NbBundle.getMessage(
                GeomFromWktConverter.class,
                "GeomFromWktConverter.getFormatDescription().returnValue"); // NOI18N
    }

    @Override
    public String getFormatHtmlDescription() {
        return NbBundle.getMessage(
                GeomFromWktConverter.class,
                "GeomFromWktConverter.getFormatHtmlDescription().returnValue"); // NOI18N
    }

    @Override
    public Object getFormatExample() {
        return NbBundle.getMessage(GeomFromWktConverter.class, "GeomFromWktConverter.getFormatExample().returnValue"); // NOI18N
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class EWKT {

        //~ Instance fields ----------------------------------------------------

        private transient int srid = -1;
        private transient String wkt = null;
    }
}
