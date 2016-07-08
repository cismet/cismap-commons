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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import edu.umd.cs.piccolo.event.PInputEvent;

import java.awt.Color;
import java.awt.Cursor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.PrintingToolTip;
import de.cismet.cismap.commons.gui.printing.Resolution;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.printing.Template;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.collections.TypeSafeCollections;

import de.cismet.tools.gui.StaticSwingTools;

import static java.lang.Thread.sleep;

import static de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener.DEFAULT_JAVA_RESOLUTION_IN_DPI;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener.MILLIMETER_OF_AN_INCH;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener.MILLIMETER_OF_A_METER;

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

// int placeholderHeight = 0;
// double realWorldWidth = 0;
// double realWorldHeight = 0;
// double widthToHeightRatio = 1.0d;
// int scaleDenominator = 0;
// int placeholderWidth = 0;
    Thread zoomThread;
    long zoomTime;
    private final PropertyChangeListener mapInteractionModeListener;
    private final MappingComponent mappingComponent;
    private final Collection<PrintTemplateFeature> printFeatureCollection;
    // private final FeatureMoveListener featureMoveListenerDelegate;
    private final List<Feature> backupFeature;
    private final List<Feature> backupHoldFeature;
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
        this.printFeatureCollection = TypeSafeCollections.newArrayList(1);
        this.mappingComponent = mappingComponent;
//        this.featureMoveListenerDelegate = new FeatureMoveListener(mappingComponent);
        this.backupFeature = TypeSafeCollections.newArrayList();
        this.backupHoldFeature = TypeSafeCollections.newArrayList();
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
    private GeometryFactory getGF() {
        return new GeometryFactory(new PrecisionModel(
                    PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getName()));
    }

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
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getDiagonal(final Geometry g) {
        final XBoundingBox geomBB = new XBoundingBox(g);
        return Math.sqrt((geomBB.getWidth() * geomBB.getWidth())
                        + (geomBB.getHeight() * geomBB.getHeight()));
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

        final int placeholderWidth = selectedTemplate.getMapWidth();
        final int placeholderHeight = selectedTemplate.getMapHeight();
        int scaleDenominator = selectedScale.getDenominator();
        final double widthToHeightRatio = (double)placeholderWidth / (double)placeholderHeight;
        final double mapWidth = mappingComponent.getCamera().getViewBounds().getWidth();
        final double mapHeight = mappingComponent.getCamera().getViewBounds().getHeight();
        double realWorldHeight = 0d;
        double realWorldWidth = 0d;
        String bestimmerDimension = null;
        // calculate realworldsize
        if (scaleDenominator == -1) {
            final String s = JOptionPane.showInputDialog(
                    StaticSwingTools.getParentFrame(mappingComponent),
                    org.openide.util.NbBundle.getMessage(
                        PrintingFrameListener.class,
                        "PrintingFrameListener.init(double,int,int,String).message"),
                    ""); // NOI18N
            try {
                scaleDenominator = Integer.parseInt(s);
            } catch (Exception skip) {
                log.warn(
                    "Could not determine the given scale denominator. It will be set to '0.0' to enable free scaling.",
                    skip);
                scaleDenominator = 0;
            }
        }

        if (scaleDenominator == 0) {
            // no fixed scale
            if ((widthToHeightRatio / (mapWidth / mapHeight)) < 1) {
                // height is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldHeight = mapHeight * 0.75;
                realWorldWidth = realWorldHeight * widthToHeightRatio;
                bestimmerDimension = PrintingTemplatePreviewListener.HEIGHT;
            } else {
                // width is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldWidth = mapWidth * 0.75;
                realWorldHeight = (double)realWorldWidth / (double)widthToHeightRatio;
                bestimmerDimension = PrintingTemplatePreviewListener.WIDTH;
            }
        } else {
            realWorldWidth = placeholderWidth / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH
                        / MILLIMETER_OF_A_METER * scaleDenominator;
            realWorldHeight = placeholderHeight / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH
                        / MILLIMETER_OF_A_METER * scaleDenominator;

            if (!mappingComponent.getMappingModel().getSrs().isMetric()) {
                try {
                    final String srs = mappingComponent.getMappingModel().getSrs().getCode();
                    final BoundingBox currentBox = mappingComponent.getCurrentBoundingBox();
                    final GeometryFactory factory = new GeometryFactory(new PrecisionModel(),
                            CrsTransformer.extractSridFromCrs(srs));
                    Point point = factory.createPoint(new Coordinate(currentBox.getX1(), currentBox.getY1()));
                    point = CrsTransformer.transformToMetricCrs(point);
                    final XBoundingBox metricBbox = new XBoundingBox(point.getX(),
                            point.getY(),
                            point.getX()
                                    + 1,
                            point.getY()
                                    + 1,
                            CrsTransformer.createCrsFromSrid(point.getSRID()),
                            true);
                    final CrsTransformer geoTransformer = new CrsTransformer(srs);
                    final XBoundingBox geoBbox = geoTransformer.transformBoundingBox(metricBbox);
                    realWorldWidth = realWorldWidth * (geoBbox.getX2() - geoBbox.getX1());
                    realWorldHeight = realWorldHeight * (geoBbox.getX2() - geoBbox.getX1());
                } catch (Exception e) {
                    log.error("Error while trying to convert the boundingbox to a metric one.", e);
                }
            }
        }
        final BoundingBox c = CismapBroker.getInstance().getMappingComponent().getCurrentBoundingBoxFromCamera();
        final double centerX = (c.getX1() + c.getX2()) / 2;
        final double centerY = (c.getY1() + c.getY2()) / 2;
        final double halfRealWorldWidth = realWorldWidth / 2d;
        final double halfRealWorldHeigth = realWorldHeight / 2d;
        // build geometry for sheet with center in origin
        final Coordinate[] outerCoords = new Coordinate[5];
        outerCoords[0] = new Coordinate(-halfRealWorldWidth, -halfRealWorldHeigth);
        outerCoords[1] = new Coordinate(+halfRealWorldWidth, -halfRealWorldHeigth);
        outerCoords[2] = new Coordinate(+halfRealWorldWidth, +halfRealWorldHeigth);
        outerCoords[3] = new Coordinate(-halfRealWorldWidth, +halfRealWorldHeigth);
        outerCoords[4] = new Coordinate(-halfRealWorldWidth, -halfRealWorldHeigth);

        log.fatal(realWorldWidth);

        // create the geometry from coordinates
        LinearRing outerRing = getGF().createLinearRing(outerCoords);
        final LinearRing[] innerRings = null;

        // translate to target landparcel position
        final AffineTransformation translateToDestination = AffineTransformation.translationInstance(centerX, centerY);
        outerRing = (LinearRing)translateToDestination.transform(outerRing);

        final Geometry geom = getGF().createPolygon(outerRing, innerRings);

        final Feature oldPrintFeature = printTemplateStyledFeature;
        printTemplateStyledFeature = new PrintTemplateFeature();
        printTemplateStyledFeature.setResolution(selectedResolution);
        printTemplateStyledFeature.setTemplate(selectedTemplate);
        printTemplateStyledFeature.setScale(selectedScale);
        printTemplateStyledFeature.setFillingPaint(Color.DARK_GRAY);
        printTemplateStyledFeature.setCanBeSelected(true);
        printTemplateStyledFeature.setEditable(true);
        printTemplateStyledFeature.setGeometry(geom);
        printFeatureCollection.clear();
        printFeatureCollection.add(printTemplateStyledFeature);
        final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection)
            mappingComponent.getFeatureCollection();
        if (oldPrintFeature != null) {
            mapFeatureCol.unholdFeature(oldPrintFeature);
            mapFeatureCol.removeFeature(oldPrintFeature);
        } else {
            oldOverlappingCheck = CismapBroker.getInstance().isCheckForOverlappingGeometriesAfterFeatureRotation();
            CismapBroker.getInstance().setCheckForOverlappingGeometriesAfterFeatureRotation(false);
        }
        mapFeatureCol.holdFeature(printTemplateStyledFeature);
        mapFeatureCol.addFeature(printTemplateStyledFeature);
        final PFeature printPFeature = mappingComponent.getPFeatureHM().get(printTemplateStyledFeature);

        mappingComponent.getPrintingFrameLayer().removeAllChildren();

        gotoPrintAreaWithBuffer();
        mapFeatureCol.select(printTemplateStyledFeature);
        mappingComponent.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
        mappingComponent.showHandles(false);
    }

    /**
     * DOCUMENT ME!
     */
    private void gotoPrintAreaWithBuffer() {
        final double diag = getDiagonal(getExtentOfPrintFeatureCollection());
        final XBoundingBox gotoBB = new XBoundingBox(getExtentOfPrintFeatureCollection().buffer(diag * 0.05d));
        mappingComponent.gotoBoundingBoxWithHistory(gotoBB);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getExtentOfPrintFeatureCollection() {
        Geometry union = null;
        for (final Feature f : printFeatureCollection) {
            if (union == null) {
                union = f.getGeometry().buffer(0);
            } else {
                union.union(f.getGeometry().buffer(0));
            }
        }
        return union.getEnvelope();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<PrintTemplateFeature> getPrintFeatureCollection() {
        return printFeatureCollection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    private void ensureSelection(final PInputEvent e) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(e, new Class[] { PFeature.class });
        if (o instanceof PFeature) {
            final PFeature pFeature = (PFeature)o;
            final Feature feature = pFeature.getFeature();
            final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection)
                mappingComponent.getFeatureCollection();
            if (!mapFeatureCol.isSelected(feature)) {
                mapFeatureCol.select(feature);
            }
        }
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);
        if ((event.getClickCount() > 1) && event.isLeftMouseButton()) {
//            final double rotationAngle = calculateRotationAngle();
//            final Point templateCenter = getTemplateCenter();
//            printWidget.downloadProduct(templateCenter, rotationAngle);
//            cleanUpAndRestoreFeatures();
            mappingComponent.showPrintingDialog(getOldInteractionMode());
        }
    }

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        gotoPrintAreaWithBuffer();
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
        if (!cleared) {
            mappingComponent.removePropertyChangeListener(mapInteractionModeListener);
            if (printTemplateStyledFeature != null) {
                final FeatureCollection mapFeatureCollection = mappingComponent.getFeatureCollection();
                mapFeatureCollection.unholdFeature(printTemplateStyledFeature);
                mapFeatureCollection.removeFeature(printTemplateStyledFeature);
                printTemplateStyledFeature = null;
            }
            if (MappingComponent.PRINTING_AREA_SELECTION.equals(mappingComponent.getInteractionMode())) {
                mappingComponent.setInteractionMode(oldInteractionMode);
            }
        }
        cleared = true;
        CismapBroker.getInstance().setCheckForOverlappingGeometriesAfterFeatureRotation(oldOverlappingCheck);
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
                        gotoPrintAreaWithBuffer();
                        mappingComponent.queryServices();
                    }
                };
            zoomThread.setPriority(Thread.NORM_PRIORITY);
            CismetThreadPool.execute(zoomThread);
        }
    }
}
