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
package de.cismet.cismap.commons.drophandler.builtin;

import com.vividsolutions.jts.geom.Point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.jackrabbit.commons.json.JsonParser;
import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandler;
import de.cismet.cismap.commons.drophandler.filematcher.builtin.MappingComponentDropHandlerFileTypeMatcher;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.options.GPSDirectionOptions;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.ExifReader;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = MappingComponentDropHandler.class)
public class MappingComponentGeoImageFileDropHandler implements MappingComponentDropHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MappingComponentGeoImageFileDropHandler.class);

    //~ Instance fields --------------------------------------------------------

    @Getter private final ExifGeoImageFileMatcher fileMatcher = new ExifGeoImageFileMatcher();

    private Map<File, GpsData> gpsDataMap = new HashMap<>();

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getPriority() {
        return MappingComponentDropHandlerBuiltinPriorityConstants.GEOIMAGE;
    }

    @Override
    public void dropFiles(final Collection<File> files) {
        final Collection<Feature> featuresToAdd = new ArrayList<>();
        for (final File file : files) {
            final GpsData gpsData = gpsDataMap.get(file);
            final Point point = gpsData.getPoint();
            final Double direction = gpsData.getDirection();
            final MappingComponentGeoImageFileFeatureRenderer featureRenderer =
                new MappingComponentGeoImageFileFeatureRenderer(file, point, direction);
            featureRenderer.setGeometry(point);
            featuresToAdd.add(featureRenderer);
        }
        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().substituteFeatures(featuresToAdd);
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final MappingComponent mappingComponent = CismapBroker.getInstance().getMappingComponent();
                    if (!mappingComponent.isFixedMapExtent()) {
                        mappingComponent.zoomToAFeatureCollection(
                            featuresToAdd,
                            true,
                            mappingComponent.isFixedMapScale());
                    }
                }
            });
    }

    /**
     * Adjust the direction according to the GPSDirection.properties file.
     *
     * @param   direction  the direction
     * @param   model      the model from the exif data
     *
     * @return  DOCUMENT ME!
     */
    private double adjustDirection(double direction, final String model) {
        final GPSDirectionOptions.GPSDirection angleDirection = CismapBroker.getInstance().getGpsAngleDirection();

        if (angleDirection.equals(GPSDirectionOptions.GPSDirection.AUTO)) {
            final String clockwisePattern = getRegEx(true);

            if ((model != null) && (clockwisePattern != null) && Pattern.matches(clockwisePattern, model)) {
                final String counterClockwisePattern = getRegEx(false);

                if ((counterClockwisePattern == null) || !Pattern.matches(counterClockwisePattern, model)) {
                    direction *= -1;
                }
            }
        } else if (angleDirection.equals(GPSDirectionOptions.GPSDirection.CW)) {
            direction *= -1;
        }

        return direction;
    }

    /**
     * Get the clockwise property or the counter clockwise property from the GPSDirection.properties file.
     *
     * @param   clockwise  true, iff the clockwise property should be read
     *
     * @return  the specified property
     */
    private String getRegEx(final boolean clockwise) {
        final Properties prop = new Properties();

        try {
            prop.load(MappingComponentGeoImageFileDropHandler.class.getResourceAsStream("/GPSDirection.properties"));

            if (clockwise) {
                return prop.getProperty("clockwise-filter");
            } else {
                return prop.getProperty("counter-clockwise-filter");
            }
        } catch (Exception e) {
            LOG.error("Cannot read GPSDirection.properties", e);
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class ExifGeoImageFileMatcher extends MappingComponentDropHandlerFileTypeMatcher {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ExifGeoImageFileMatcher object.
         */
        ExifGeoImageFileMatcher() {
            super(MappingComponentDropHandlerFileTypeMatcher.FileType.IMAGE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isMatching(final File file) {
            final boolean superIsMatching = super.isMatching(file);
            if (!superIsMatching) {
                return false;
            } else {
                try {
                    final ExifReader reader = new ExifReader(file);
                    final Point point = reader.getGpsCoords();
                    if (point == null) {
                        return false;
                    }
                    Double direction;
                    try {
                        direction = reader.getGpsDirection();
                        direction = adjustDirection(direction, reader.getModel());
                    } catch (final Exception ex) {
                        direction = null;
                    }
                    gpsDataMap.put(file, new GpsData(point, direction));
                    return true;
                } catch (final Exception ex) {
                    return false;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    @Getter
    @Setter
    @AllArgsConstructor
    class GpsData {

        //~ Instance fields ----------------------------------------------------

        private Point point;
        private Double direction;
    }
}
