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
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import edu.umd.cs.piccolo.event.PInputEvent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.PopupMenu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.PrintingToolTip;
import de.cismet.cismap.commons.gui.printing.Resolution;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.printing.Template;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.CismetThreadPool;

import static java.lang.Thread.sleep;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class PrintingTemplatePreviewListener extends FeatureMoveListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            PrintingTemplatePreviewListener.class);
    //

    public static final String WIDTH = "WIDTH";
    public static final String HEIGHT = "HEIGHT";
    public static final Color BORDER_COLOR = new Color(0, 0, 255, 75);

    //~ Instance fields --------------------------------------------------------

    Thread zoomThread;
    long zoomTime;
    private final PropertyChangeListener mapInteractionModeListener;
    private final MappingComponent mappingComponent;
    // private final FeatureMoveListener featureMoveListenerDelegate;
    private final List<? extends Feature> backupFeature;
    private final List<? extends Feature> backupHoldFeature;
    private final PrintingToolTip PRINTING_TOOLTIP = new PrintingToolTip(new Color(255, 255, 222, 200));
    private boolean cleared;
    private String oldInteractionMode;
    private PrintTemplateFeature printTemplateStyledFeature;
    private boolean oldOverlappingCheck = true;
//    private String bestimmerDimension = WIDTH;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrintingTemplatePreviewListener object.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PrintingTemplatePreviewListener(final MappingComponent mappingComponent) {
        super(mappingComponent);
        this.cleared = true;
        this.mappingComponent = mappingComponent;
//        this.featureMoveListenerDelegate = new FeatureMoveListener(mappingComponent);
        this.backupFeature = new ArrayList<Feature>();
        this.backupHoldFeature = new ArrayList<Feature>();
        this.oldInteractionMode = "PAN";
        // listener to remove the template feature and reset the old state if interaction mode is changed by user
        this.mapInteractionModeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    if ((evt != null) && MappingComponent.PROPERTY_MAP_INTERACTION_MODE.equals(evt.getPropertyName())) {
                        if (MappingComponent.PRINTING_AREA_SELECTION.equals(evt.getOldValue())) {
                            cleanUpAndRestoreFeatures();
                        }
                    }
                }
            };
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOldInteractionMode() {
        return oldInteractionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  oldInteractionMode  DOCUMENT ME!
     */
    public void setOldInteractionMode(final String oldInteractionMode) {
        this.oldInteractionMode = oldInteractionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scale             DOCUMENT ME!
     * @param  printingTemplate  DOCUMENT ME!
     */
    private void zoom(final double scale, final PrintTemplateFeature printingTemplate) {
        final Point centroid = printingTemplate.getGeometry().getCentroid();
        final AffineTransformation at = AffineTransformation.scaleInstance(
                scale,
                scale,
                centroid.getX(),
                centroid.getY());
        final Geometry g = at.transform(printingTemplate.getGeometry());
        printingTemplate.setGeometry(g);
        final PFeature printPFeature = mappingComponent.getPFeatureHM().get(printingTemplate);
        printPFeature.visualize();
        mappingComponent.showHandles(true);
    }

    /**
     * DOCUMENT ME!
     */
    public void init() {
        mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mappingComponent.setPointerAnnotation(PRINTING_TOOLTIP);
        mappingComponent.setPointerAnnotationVisibility(true);
        final String currentInteractionMode = mappingComponent.getInteractionMode();

        // do not add listener again if we are already in print mode
        if (!MappingComponent.PRINTING_AREA_SELECTION.equals(currentInteractionMode)) {
            this.oldInteractionMode = currentInteractionMode;
            mappingComponent.addPropertyChangeListener(mapInteractionModeListener);
        }
        mappingComponent.setInteractionMode(MappingComponent.PRINTING_AREA_SELECTION);

        cleared = false;
        mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mappingComponent.setPointerAnnotation(PRINTING_TOOLTIP);
        mappingComponent.setPointerAnnotationVisibility(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedScale       DOCUMENT ME!
     * @param  selectedResolution  DOCUMENT ME!
     * @param  selectedTemplate    DOCUMENT ME!
     * @param  oldInteractionMode  DOCUMENT ME!
     */
    public void init(final Scale selectedScale,
            final Resolution selectedResolution,
            final Template selectedTemplate,
            final String oldInteractionMode) {
        init();

        this.oldInteractionMode = oldInteractionMode;

        printTemplateStyledFeature = new PrintTemplateFeature(
                selectedTemplate,
                selectedResolution,
                selectedScale,
                mappingComponent);
        final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection)
            mappingComponent.getFeatureCollection();
        oldOverlappingCheck = CismapBroker.getInstance().isCheckForOverlappingGeometriesAfterFeatureRotation();
        CismapBroker.getInstance().setCheckForOverlappingGeometriesAfterFeatureRotation(false);
        mapFeatureCol.holdFeature(printTemplateStyledFeature);
        mapFeatureCol.addFeature(printTemplateStyledFeature);
        final PFeature printPFeature = mappingComponent.getPFeatureHM().get(printTemplateStyledFeature);
//        final String fn = "/Users/thorsten/Desktop/pdfa4h.png";///Users/thorsten/desktmp/printer-empty.png";
//        final PImage pii = new BackgroundPImage(fn);
//        printPFeature.addChild(pii);
        // mappingComponent.getPrintingFrameLayer().removeAllChildren();

        mappingComponent.zoomToAFeatureCollection(CismapBroker.getInstance().getPrintFeatureCollection(), false, false);
        mapFeatureCol.select(printTemplateStyledFeature);
        mappingComponent.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
        mappingComponent.showHandles(false);
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);
        if ((event.getClickCount() == 1) && event.isLeftMouseButton()) {
            if (event.getPickedNode() instanceof PrintTemplateFeature.DerivedCloneArea) {
                final PrintTemplateFeature.DerivedCloneArea dca = (PrintTemplateFeature.DerivedCloneArea)
                    event.getPickedNode();
                final PrintTemplateFeature ptf = (PrintTemplateFeature)dca.parent.getFeature();
                final PrintTemplateFeature newPTF = new PrintTemplateFeature(ptf, dca.getSide());
                final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection)
                    mappingComponent.getFeatureCollection();
                mapFeatureCol.holdFeature(newPTF);
                mapFeatureCol.addFeature(newPTF);
                mappingComponent.zoomToAFeatureCollection(CismapBroker.getInstance().getPrintFeatureCollection(),
                    false,
                    false);
            }
        } else if ((event.getClickCount() > 1) && event.isLeftMouseButton()) {
            mappingComponent.showPrintingDialog(getOldInteractionMode());
        } else if ((event.getClickCount() == 1) && event.isRightMouseButton()) {
//            final JPopupMenu test = new JPopupMenu("test");
//            test.add(new JMenuItem("Drucken"));
//            test.add(new JMenuItem("DPI"));
//            test.add(new JMenuItem("Template"));
//            test.add(new JMenuItem("Maßstab"));
//            test.show(
//                mappingComponent,
//                (int)event.getCanvasPosition().getX(),
//                (int)event.getCanvasPosition().getY());
//            final Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[]{PFeature.class});
//            if (!(o instanceof PFeature)) {
//                return;
//            }
//            final PFeature sel = (PFeature) o;
//
//            if (!(sel.getFeature() instanceof PrintTemplateFeature)) {
//                return;
//            }
//            final PrintTemplateFeature ptf = (PrintTemplateFeature) sel.getFeature();
//            final PrintTemplateFeature newPTF = new PrintTemplateFeature(ptf, PrintTemplateFeature.Side.SOUTH);
//            final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection) mappingComponent.getFeatureCollection();
//            mapFeatureCol.holdFeature(newPTF);
//            mapFeatureCol.addFeature(newPTF);
//            mappingComponent.zoomToAFeatureCollection(CismapBroker.getInstance().getPrintFeatureCollection(),
//                    false,
//                    false);
        }
    }

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        mappingComponent.zoomToAFeatureCollection(CismapBroker.getInstance().getPrintFeatureCollection(), false, false);
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        if (event.getPickedNode() instanceof PrintTemplateFeature.DerivedCloneArea) {
            mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void mouseWheelRotated(final PInputEvent event) {
        super.mouseWheelRotatedByBlock(event);
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(event, new Class[] { PFeature.class });
        if (!(o instanceof PFeature)) {
            return;
        }
        final PFeature sel = (PFeature)o;

        if (!(sel.getFeature() instanceof PrintTemplateFeature)) {
            return;
        }
        final PrintTemplateFeature ptf = (PrintTemplateFeature)sel.getFeature();

        if (ptf.getScale().getDenominator() == 0) {
            if (log.isDebugEnabled()) {
                log.debug((event.getWheelRotation()));
            }
            if (event.getWheelRotation() < 0) {
                zoom(0.9d, ptf);
                adjustMap();
            } else {
                zoom(1.1d, ptf);
                adjustMap();
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUpAndRestoreFeatures() {
//        if (!cleared) {
//            mappingComponent.removePropertyChangeListener(mapInteractionModeListener);
//            if (printTemplateStyledFeature != null) {
//                final FeatureCollection mapFeatureCollection = mappingComponent.getFeatureCollection();
//                mapFeatureCollection.unholdFeature(printTemplateStyledFeature);
//                mapFeatureCollection.removeFeature(printTemplateStyledFeature);
//                printTemplateStyledFeature = null;
//            }
//            if (MappingComponent.PRINTING_AREA_SELECTION.equals(mappingComponent.getInteractionMode())) {
//                mappingComponent.setInteractionMode(oldInteractionMode);
//            }
//        }
//        cleared = true;
//        CismapBroker.getInstance().setCheckForOverlappingGeometriesAfterFeatureRotation(oldOverlappingCheck);
    }

    /**
     * DOCUMENT ME!
     */
    public void adjustMap() {
        final int delayTime = 800;
        zoomTime = System.currentTimeMillis() + delayTime;
        if ((zoomThread == null) || !zoomThread.isAlive()) {
            zoomThread = new Thread("PrintFrameListener adjustMap()") {

                    @Override
                    public void run() {
                        while (System.currentTimeMillis() < zoomTime) {
                            try {
                                sleep(100);
                                // log.debug("WAIT");
                            } catch (InterruptedException iex) {
                            }
                        }
                        mappingComponent.zoomToAFeatureCollection(CismapBroker.getInstance()
                                    .getPrintFeatureCollection(),
                            false,
                            false);
                    }
                };
            zoomThread.setPriority(Thread.NORM_PRIORITY);
            CismetThreadPool.execute(zoomThread);
        }
    }
}
