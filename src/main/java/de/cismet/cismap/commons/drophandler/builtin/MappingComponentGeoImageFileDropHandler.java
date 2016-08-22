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

import org.openide.util.lookup.ServiceProvider;

import java.io.File;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandler;
import de.cismet.cismap.commons.drophandler.filematcher.builtin.MappingComponentDropHandlerFileTypeMatcher;
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
        for (final File file : files) {
            final GpsData gpsData = gpsDataMap.get(file);
            final Point point = gpsData.getPoint();
            final Double direction = gpsData.getDirection();
            final MappingComponentGeoImageFileFeatureRenderer featureRenderer =
                new MappingComponentGeoImageFileFeatureRenderer(file, point, direction);
            featureRenderer.setGeometry(point);
            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(featureRenderer);
        }
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
