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

import lombok.Getter;
import lombok.Setter;

import org.openide.util.lookup.ServiceProvider;

import java.io.File;

import java.util.Collection;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandler;
import de.cismet.cismap.commons.drophandler.MappingComponentDropHandlerFileMatcher;
import de.cismet.cismap.commons.gui.layerwidget.LayerDropUtils;
import de.cismet.cismap.commons.gui.layerwidget.LayerWidget;
import de.cismet.cismap.commons.gui.layerwidget.LayerWidgetProvider;
import de.cismet.cismap.commons.rasterservice.ImageFileUtils;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = MappingComponentDropHandler.class)
public class MappingComponentGeoReferencedImageFileDropHandler implements MappingComponentDropHandler,
    LayerWidgetProvider {

    //~ Instance fields --------------------------------------------------------

    @Getter private final MappingComponentDropHandlerFileMatcher fileMatcher = new ImageFileMatcher();

    @Getter @Setter private LayerWidget layerWidget;

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getPriority() {
        return MappingComponentDropHandlerBuiltinPriorityConstants.IMAGE;
    }

    @Override
    public void dropFiles(final Collection<File> files) {
        for (final File file : files) {
            LayerDropUtils.handleImageFile(
                file,
                layerWidget.getMappingModel(),
                -1,
                layerWidget,
                ImageFileUtils.Mode.GEO_REFERENCED);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class ImageFileMatcher implements MappingComponentDropHandlerFileMatcher {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isMatching(final File file) {
            final File worldFile = ImageFileUtils.getWorldFile(file);
            return ImageFileUtils.isImageFileEnding(file.getName())
                        && ((worldFile == null) || ImageFileUtils.checkIfRasterGeoRef(worldFile));
        }
    }
}
