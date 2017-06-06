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

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.ChildNodesProvider;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.PreventNamingDuplicates;
import de.cismet.cismap.commons.features.RequestForNonreflectingFeature;
import de.cismet.cismap.commons.features.RequestForRotatingPivotLock;
import de.cismet.cismap.commons.features.RequestForUnaddableHandles;
import de.cismet.cismap.commons.features.RequestForUnmoveableHandles;
import de.cismet.cismap.commons.features.RequestForUnremovableHandles;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.PrintingSettingsWidget;
import de.cismet.cismap.commons.gui.printing.Resolution;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.printing.Template;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class PrintTemplateFeature extends DefaultStyledFeature implements XStyledFeature,
    ChildNodesProvider,
    PreventNamingDuplicates,
    RequestForRotatingPivotLock,
    RequestForUnaddableHandles,
    RequestForUnmoveableHandles,
    RequestForUnremovableHandles,
    RequestForNonreflectingFeature {

    //~ Static fields/initializers ---------------------------------------------

    public static final double DEFAULT_JAVA_RESOLUTION_IN_DPI = 72d;
    public static final double MILLIMETER_OF_AN_INCH = 25.4d;
    public static final double INCH_OF_A_MILLIMETER = 0.039d;
    public static final double MILLIMETER_OF_A_METER = 1000d;
    static final Color TEXTCOLOR = new Color(11, 72, 107);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Side {

        //~ Enum constants -----------------------------------------------------

        SOUTH, NORTH, WEST, EAST
    }

    //~ Instance fields --------------------------------------------------------

    Template template;
    Resolution resolution;
    Scale scale;
    String name;
    int number = 0;

    ArrayList<PNode> children = null;
    private MappingComponent mappingComponent;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Future<Image> futureMapImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrintTemplateFeature object.
     *
     * @param  ptfTemplate  DOCUMENT ME!
     * @param  side         DOCUMENT ME!
     */
    public PrintTemplateFeature(final PrintTemplateFeature ptfTemplate, final Side side) {
        this(ptfTemplate.template, ptfTemplate.resolution, ptfTemplate.scale, ptfTemplate.mappingComponent, true);
        final Coordinate[] translationSide = ptfTemplate.getSideLineCoords(getTranslationSide(side));
        final AffineTransformation translationAT = AffineTransformation.translationInstance(translationSide[1].x
                        - translationSide[0].x,
                translationSide[1].y
                        - translationSide[0].y);
        setGeometry(translationAT.transform(ptfTemplate.getGeometry()));
        // setGeometry(ptfTemplate.getGeometry().buffer(0));
    }

    /**
     * Creates a new PrintTemplateFeature object.
     *
     * @param  template          DOCUMENT ME!
     * @param  resolution        DOCUMENT ME!
     * @param  scale             DOCUMENT ME
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PrintTemplateFeature(final Template template,
            final Resolution resolution,
            final Scale scale,
            final MappingComponent mappingComponent) {
        this(template, resolution, scale, mappingComponent, false);
    }

    /**
     * Creates a new PrintTemplateFeature object.
     *
     * @param  template          DOCUMENT ME!
     * @param  resolution        DOCUMENT ME!
     * @param  scale             DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     * @param  cloning           DOCUMENT ME!
     */
    private PrintTemplateFeature(final Template template,
            final Resolution resolution,
            final Scale scale,
            final MappingComponent mappingComponent,
            final boolean cloning) {
        this.mappingComponent = mappingComponent;
        this.template = template;
        this.resolution = resolution;
        this.scale = scale;
        final BoundingBox boundingBoxToCalculateTheLocationOfTHeTemplate = CismapBroker.getInstance()
                    .getMappingComponent()
                    .getCurrentBoundingBoxFromCamera();
        final double dimensionWidth = mappingComponent.getCamera().getViewBounds().getWidth();
        final double dimensionHeight = mappingComponent.getCamera().getViewBounds().getHeight();
        init(boundingBoxToCalculateTheLocationOfTHeTemplate, dimensionWidth, dimensionHeight, cloning);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  boundingBoxToCalculateTheLocationOfTHeTemplate  DOCUMENT ME!
     */
    public final void init(final BoundingBox boundingBoxToCalculateTheLocationOfTHeTemplate) {
        init(boundingBoxToCalculateTheLocationOfTHeTemplate, null, null, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  boundingBoxToCalculateTheLocationOfTHeTemplate  DOCUMENT ME!
     * @param  dimensionWidth                                  DOCUMENT ME!
     * @param  dimensionHeight                                 DOCUMENT ME!
     * @param  cloning                                         DOCUMENT ME!
     */
    public final void init(final BoundingBox boundingBoxToCalculateTheLocationOfTHeTemplate,
            Double dimensionWidth,
            Double dimensionHeight,
            final boolean cloning) {
        if ((dimensionHeight == null) && (template != null)) {
            dimensionHeight = (double)template.getMapHeight();
        }
        if ((dimensionWidth == null) && (template != null)) {
            dimensionWidth = (double)template.getMapWidth();
        }

        children = new ArrayList<>();
        final int placeholderWidth = template.getMapWidth();
        final int placeholderHeight = template.getMapHeight();
        int scaleDenominator = scale.getDenominator();
        final double widthToHeightRatio = (double)placeholderWidth / (double)placeholderHeight;

        double realWorldHeight = 0d;
        double realWorldWidth = 0d;

        // calculate realworldsize
        if ((scaleDenominator == -1) && !cloning) {
            final String s = JOptionPane.showInputDialog(
                    StaticSwingTools.getParentFrame(mappingComponent),
                    org.openide.util.NbBundle.getMessage(
                        PrintTemplateFeature.class,
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
            if ((widthToHeightRatio / (dimensionWidth / dimensionHeight)) < 1) {
                // height is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldHeight = dimensionHeight * 0.75;
                realWorldWidth = realWorldHeight * widthToHeightRatio;
            } else {
                // width is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldWidth = dimensionWidth * 0.75;
                realWorldHeight = (double)realWorldWidth / (double)widthToHeightRatio;
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
        final double centerX = (boundingBoxToCalculateTheLocationOfTHeTemplate.getX1()
                        + boundingBoxToCalculateTheLocationOfTHeTemplate.getX2()) / 2;
        final double centerY = (boundingBoxToCalculateTheLocationOfTHeTemplate.getY1()
                        + boundingBoxToCalculateTheLocationOfTHeTemplate.getY2()) / 2;
        final double halfRealWorldWidth = realWorldWidth / 2d;
        final double halfRealWorldHeigth = realWorldHeight / 2d;
        // build geometry for sheet with center in origin
        final Coordinate[] outerCoords = new Coordinate[5];
        outerCoords[0] = new Coordinate(-halfRealWorldWidth, -halfRealWorldHeigth);
        outerCoords[1] = new Coordinate(+halfRealWorldWidth, -halfRealWorldHeigth);
        outerCoords[2] = new Coordinate(+halfRealWorldWidth, +halfRealWorldHeigth);
        outerCoords[3] = new Coordinate(-halfRealWorldWidth, +halfRealWorldHeigth);
        outerCoords[4] = new Coordinate(-halfRealWorldWidth, -halfRealWorldHeigth);

        // create the geometry from coordinates
        LinearRing outerRing = getGF().createLinearRing(outerCoords);
        final LinearRing[] innerRings = null;

        // translate to target landparcel position
        final AffineTransformation translateToDestination = AffineTransformation.translationInstance(centerX, centerY);
        outerRing = (LinearRing)translateToDestination.transform(outerRing);
        this.setGeometry(getGF().createPolygon(outerRing, innerRings));
        setCanBeSelected(true);
        setEditable(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   side  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Side getTranslationSide(final Side side) {
        switch (side) {
            case NORTH: {
                return Side.EAST;
            }
            case SOUTH: {
                return Side.WEST;
            }
            case WEST: {
                return Side.NORTH;
            }
            case EAST:
            default: {
                return Side.SOUTH;
            }
        }
    }

    @Override
    public String toString() {
        return "Druckbereich ";
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private GeometryFactory getGF() {
        return new GeometryFactory(new PrecisionModel(
                    PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Point getTemplateCenter() {
        return getGeometry().getCentroid();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  template  DOCUMENT ME!
     */
    public void setTemplate(final Template template) {
        this.template = template;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  resolution  DOCUMENT ME!
     */
    public void setResolution(final Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Scale getScale() {
        return scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getRealScaleDenominator() {
        final Coordinate[] corrds = getGeometry().getCoordinates();
        // take former points (0,0) and (X,0) from template rectangle
        final Coordinate p0 = corrds[0];
        final Coordinate p1 = corrds[1];

        final double realwidth = Math.sqrt(((p0.x - p1.x) * (p0.x - p1.x)) + ((p0.y - p1.y) * (p0.y - p1.y)));

        final double paperwidth = template.getMapWidth() / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH
                    / MILLIMETER_OF_A_METER;

        final double denom = realwidth / paperwidth;

        return Math.round(denom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scale  DOCUMENT ME!
     */
    public void setScale(final Scale scale) {
        this.scale = scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getRotationAngle() {
        if (getGeometry() != null) {
            final Coordinate[] corrds = getGeometry().getCoordinates();
            // take former points (0,0) and (X,0) from template rectangle
            final Coordinate point00 = corrds[0];
            final Coordinate pointX0 = corrds[1];
            // determine tangens
            final double oppositeLeg = pointX0.y - point00.y;
            final double adjacentLeg = pointX0.x - point00.x;
            final double tangens = oppositeLeg / adjacentLeg;
            // handle quadrant detection, map to range [-180, 180] degree
            double result = (adjacentLeg > 0) ? 0d : 180d;
            result = (oppositeLeg > 0) ? -result : result;
            // calculate rotation angle in degree
            result -= Math.toDegrees(Math.atan(tangens));
////        round to next full degree
//        return Math.round(result);
            return result;
        } else {
            return -1;
        }
    }

    @Override
    public ImageIcon getIconImage() {
        if (template != null) {
            return template.getIcon();
        }
        return null;
    }

    @Override
    public String getType() {
        return ((template != null) ? template.getShortname() : "");
    }

    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        return null;
    }

    @Override
    public Stroke getLineStyle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Future<Image> getFutureMapImage() {
        return futureMapImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  futureMapImage  DOCUMENT ME!
     */
    public void setFutureMapImage(final Future<Image> futureMapImage) {
        this.futureMapImage = futureMapImage;
    }

    @Override
    public Paint getFillingPaint() {
        return mappingComponent.getPrintingSettingsDialog().getFeatureFillingPaint();
    }

    @Override
    public float getTransparency() {
        return 0.75f;
    }

    @Override
    public String getName() {
        if (mappingComponent.getSpecialFeatureCollection(PrintTemplateFeature.class).size() == 1) {
            return getOriginalName();
        } else {
            return getOriginalName() + " - " + number;
        }
    }

    @Override
    public String getOriginalName() {
        return "Druckbereich ";
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(final int n) {
        number = n;
    }

    /**
     * DOCUMENT ME!
     *
     * @param     parent  DOCUMENT ME!
     *
     * @return    DOCUMENT ME!
     *
     * @Override  DOCUMENT ME!
     */
    @Override
    public Collection<PNode> provideChildren(final PFeature parent) {
        if (children.size() == 0) {
            initPNodeChildren(parent);
        }
        return children;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parent  DOCUMENT ME!
     */
    private void initPNodeChildren(final PFeature parent) {
        final PTFDerivedCommandArea commands = new PTFDerivedCommandArea(parent);

//        final DerivedSubFeature centerArea = new DerivedSubFeature(parent, new DeriveRule() {
//
//            @Override
//            public Geometry derive(final Geometry in) {
//                return getGeometry().getCentroid().buffer(getShortSideLength() * 0.2).getEnvelope();
//            }
//        });
//
//        final DerivedSubFeature sw = new DerivedSubFeature(parent, new DeriveRule() {
//
//                    @Override
//                    public Geometry derive(final Geometry in) {
//                        return getGF().createPoint(getGeometry().buffer(-1 * 0.1 * getShortSideLength()).getCoordinates()[0]).buffer(10);
//                    }
//                });
//        final DerivedSubFeature so = new DerivedSubFeature(parent, new DeriveRule() {
//
//                    @Override
//                    public Geometry derive(final Geometry in) {
//                        return getGF().createPoint(getGeometry().buffer(-1 * 0.1 * getShortSideLength()).getCoordinates()[1]).buffer(10);
//                    }
//                });
//        final DerivedSubFeature no = new DerivedSubFeature(parent, new DeriveRule() {
//
//                    @Override
//                    public Geometry derive(final Geometry in) {
//                        return getGF().createPoint(getGeometry().buffer(-1 * 0.1 * getShortSideLength()).getCoordinates()[2]).buffer(10);
//                    }
//                });
//        final DerivedSubFeature nw = new DerivedSubFeature(parent, new DeriveRule() {
//
//                    @Override
//                    public Geometry derive(final Geometry in) {
//                        return getGF().createPoint(getGeometry().buffer(-1 * 0.1 * getShortSideLength()).getCoordinates()[3]).buffer(10);
//                    }
//                });
//
//        children.add(centerArea);
//        children.add(sw);
//        children.add(so);
//        children.add(no);
//        children.add(nw);
        children.add(new SubPText(commands));

        children.add(new DerivedCloneArea(parent, Side.WEST));
        children.add(new DerivedCloneArea(parent, Side.EAST));
        children.add(new DerivedCloneArea(parent, Side.NORTH));
        children.add(new DerivedCloneArea(parent, Side.SOUTH));
        children.add(commands);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   indexA  DOCUMENT ME!
     * @param   indexB  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry getLineFromCoordsAt(final int indexA, final int indexB) {
        return getGF().createLineString(getCoordsArrayFromGeometryCoordsAt(indexA, indexB));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   indexA  DOCUMENT ME!
     * @param   indexB  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate[] getCoordsArrayFromGeometryCoordsAt(final int indexA, final int indexB) {
        final Coordinate[] cs = getGeometry().getCoordinates();
        final Coordinate[] lineCoords = new Coordinate[2];

        lineCoords[0] = cs[indexA];
        lineCoords[1] = cs[indexB];
        return lineCoords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   side  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    private Coordinate[] getSideLineCoords(final Side side) {
        switch (side) {
            case NORTH: {
                return getCoordsArrayFromGeometryCoordsAt(2, 3);
            }
            case SOUTH: {
                return getCoordsArrayFromGeometryCoordsAt(0, 1);
            }
            case WEST: {
                return getCoordsArrayFromGeometryCoordsAt(3, 0);
            }
            case EAST: {
                return getCoordsArrayFromGeometryCoordsAt(1, 2);
            }
            default: {
                throw new RuntimeException("Error in Universe");
            }
        }
    }

    /**
     * The Rectangle of the PrinTemplateFeature is build as a Coordinate Array with these indices.
     *
     * @param   side  The Side of the Rectangle
     *
     * @return  THe Linestring of the side
     */
    protected Geometry getSideLine(final Side side) {
        return getGF().createLineString(getSideLineCoords(side));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected double getShortSideLength() {
        final Geometry southLine = getSideLine(Side.SOUTH);
        final Geometry westLine = getSideLine(Side.WEST);
        if (southLine.getLength() < westLine.getLength()) {
            return southLine.getLength();
        } else {
            return westLine.getLength();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected double getLongSideLength() {
        final Geometry southLine = getSideLine(Side.SOUTH);
        final Geometry westLine = getSideLine(Side.WEST);
        if (southLine.getLength() > westLine.getLength()) {
            return southLine.getLength();
        } else {
            return westLine.getLength();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class PTFDerivedCommandArea extends DerivedCommandArea {

        //~ Instance fields ----------------------------------------------------

        private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DerivedCommandArea object.
         *
         * @param  parent  DOCUMENT ME!
         */
        public PTFDerivedCommandArea(final PFeature parent) {
            super(parent, new DeriveRule() {

                    @Override
                    public Geometry derive(final Geometry in) {
                        return getGeometry().buffer(-1 * 0.1 * getShortSideLength());
                    }
                });
            setPaint(Color.white);
            setStroke(null);
            setTransparency(0.3f);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void mouseClicked(final PInputEvent event) {
            super.mouseClicked(event);
            if ((event.getClickCount() > 1) && event.isLeftMouseButton()) {
                mappingComponent.showPrintingDialog();
            } else if ((event.getClickCount() == 1) && event.isRightMouseButton()) {
                final JPopupMenu changeMenu = new JPopupMenu();
                final PrintingSettingsWidget settings = mappingComponent.getPrintingSettingsDialog();

                final JMenu menTemplates = new JMenu(settings.getTemplateString());
                for (final Template t : settings.getTemplates()) {
                    menTemplates.add(new JMenuItem(new AbstractAction(t.getTitle()) {

                                @Override
                                public void actionPerformed(final ActionEvent e) {
                                    mappingComponent.getFeatureCollection().removeFeature(PrintTemplateFeature.this);
                                    setTemplate(t);
                                    addAndRefresh();
                                }
                            }));
                }
                changeMenu.add(menTemplates);

                final JMenu menScales = new JMenu(settings.getScaleString());
                for (final Scale s : settings.getScales()) {
                    menScales.add(new JMenuItem(new AbstractAction(s.getText()) {

                                @Override
                                public void actionPerformed(final ActionEvent e) {
                                    mappingComponent.getFeatureCollection().removeFeature(PrintTemplateFeature.this);
                                    PrintTemplateFeature.this.setScale(s);
                                    addAndRefresh();
                                }
                            }));
                }
                changeMenu.add(menScales);

                final JMenu menResolution = new JMenu(settings.getResolutionString());
                for (final Resolution r : settings.getResolutions()) {
                    menResolution.add(new JMenuItem(new AbstractAction(r.getText()) {

                                @Override
                                public void actionPerformed(final ActionEvent e) {
                                    mappingComponent.getFeatureCollection().removeFeature(PrintTemplateFeature.this);
                                    setResolution(r);
                                    addAndRefresh();
                                }
                            }));
                }
                changeMenu.add(menResolution);

                changeMenu.add(new JMenuItem(
                        new AbstractAction(
                            org.openide.util.NbBundle.getMessage(
                                PrintingSettingsWidget.class,
                                "PrintingTemplateFeature.removeAction")) {

                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                mappingComponent.getFeatureCollection().removeFeature(PrintTemplateFeature.this);
                            }
                        }));

                changeMenu.show(
                    mappingComponent,
                    (int)event.getCanvasPosition().getX(),
                    (int)event.getCanvasPosition().getY());
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void addAndRefresh() {
            final DefaultFeatureCollection mapFeatureCol = (DefaultFeatureCollection)
                mappingComponent.getFeatureCollection();

            init(new XBoundingBox(getGeometry()));
            // ---
            mapFeatureCol.holdFeature(PrintTemplateFeature.this);
            mapFeatureCol.addFeature(PrintTemplateFeature.this);
            mappingComponent.adjustMapForSpecialFeatureClasses(PrintTemplateFeature.class);
            mapFeatureCol.select(PrintTemplateFeature.this);
            // mappingComponent.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
            mappingComponent.showHandles(false);

//                              Warum geht das nicht ????
//                            mappingComponent.reconsiderFeature(PrintTemplateFeature.this);
//                            final PFeature printPFeature = mappingComponent.getPFeatureHM().get(PrintTemplateFeature.this);
//                            printPFeature.visualize();
//                            mappingComponent.showHandles(true);
        }

        // Moving
        @Override
        public void mousePressed(final PInputEvent event) {
            super.mousePressed(event);
            ((PBasicInputEventHandler)mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON)).mousePressed(
                event);
        }

        @Override
        public void mouseDragged(final PInputEvent event) {
            super.mouseDragged(event);
            ((PBasicInputEventHandler)mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON)).mouseDragged(
                event);
        }

        @Override
        public void mouseReleased(final PInputEvent event) {
            super.mouseReleased(event);
            ((PBasicInputEventHandler)mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON)).mouseReleased(
                event);
            mappingComponent.ensureVisibilityOfSpecialFeatures(PrintTemplateFeature.class);
        }

        @Override
        public void mouseMoved(final PInputEvent event) {
            super.mouseMoved(event);
//                mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            ((PBasicInputEventHandler)mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON)).mouseMoved(
                event);
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
                    mappingComponent.adjustMapForSpecialFeatureClasses(PrintTemplateFeature.class);
                } else {
                    zoom(1.1d, ptf);
                    mappingComponent.adjustMapForSpecialFeatureClasses(PrintTemplateFeature.class);
                }
            }
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
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class DerivedCloneArea extends DerivedCommandArea {

        //~ Instance fields ----------------------------------------------------

        private Side side;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DerivedCloneArea object.
         *
         * @param  parent  DOCUMENT ME!
         * @param  side    DOCUMENT ME!
         */
        public DerivedCloneArea(final PFeature parent, final Side side) {
            super(parent, new DeriveRule() {

                    @Override
                    public Geometry derive(final Geometry in) {
                        final Coordinate[] line = new Coordinate[2];

                        line[0] = RectangleMath.getPointPerpendicular(
                                getSideLineCoords(side),
                                RectangleMath.getPointFromStartByFraction(getSideLineCoords(side), 0.25),
                                0.04
                                        * getShortSideLength());
                        line[1] = RectangleMath.getPointPerpendicular(
                                getSideLineCoords(side),
                                RectangleMath.getPointFromStartByFraction(getSideLineCoords(side), 0.75),
                                0.04
                                        * getShortSideLength());

                        return getGF().createLineString(line).buffer(0.02 * getShortSideLength());
                    }
                });
            this.side = side;
            setPaint(Color.white);
            setStroke(null);
            setTransparency(0.3f);
        }

        //~ Methods ------------------------------------------------------------

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
                    mappingComponent.adjustMapForSpecialFeatureClasses(PrintTemplateFeature.class);
                }
            }
        }

        @Override
        public void mouseMoved(final PInputEvent event) {
//            if (event.getPickedNode() instanceof PrintTemplateFeature.DerivedCloneArea) {
//                mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//            } else {
//                mappingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                ((PBasicInputEventHandler) mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON)).mouseDragged(event);
//            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Side getSide() {
            return side;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class SubPText extends PText implements PropertyChangeListener {

        //~ Instance fields ----------------------------------------------------

        PNode parentNode;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SubPNode object.
         *
         * @param  parent  DOCUMENT ME!
         */
        public SubPText(final PNode parent) {
            // super("/Users/thorsten/tmp/printer-empty.png");
            super();
            this.parentNode = parent;
            setText(getPTFString());
            this.setTextPaint(PrintTemplateFeature.TEXTCOLOR);
            this.setJustification(Component.CENTER_ALIGNMENT);
            parent.addPropertyChangeListener(this);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            setText(getPTFString());
            this.centerFullBoundsOnPoint(parentNode.getGlobalBounds().getCenterX(),
                parentNode.getGlobalBounds().getCenterY());

            setScale(0.9 / 1000 * PrintTemplateFeature.this.getRealScaleDenominator()); // Heuristic / use
                                                                                        // realsScaleDenominator
                                                                                        // because of the "free"
                                                                                        // Option
            setRotation(Math.toRadians(PrintTemplateFeature.this.getRotationAngle()));
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getPTFString() {
            final String s = PrintTemplateFeature.this.template.getShortname() + "\n\n\n"
                        + "#" + PrintTemplateFeature.this.getNumber() + "\n\n\n"
                        + "1:" + PrintTemplateFeature.this.getRealScaleDenominator() + "\n\n\n"
                        + "Auflösung:" + PrintTemplateFeature.this.resolution.getResolution() + " dpi";
            return s;
        }
    }
}
