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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.Color;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.ChildNodesProvider;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.LockedRotatingPivotRequest;
import de.cismet.cismap.commons.features.PreventNamingDuplicates;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.Resolution;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.printing.Template;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class PrintTemplateFeature extends DefaultStyledFeature implements XStyledFeature,
    LockedRotatingPivotRequest,
    PreventNamingDuplicates,
    ChildNodesProvider {

    //~ Static fields/initializers ---------------------------------------------

    public static final double DEFAULT_JAVA_RESOLUTION_IN_DPI = 72d;
    public static final double MILLIMETER_OF_AN_INCH = 25.4d;
    public static final double INCH_OF_A_MILLIMETER = 0.039d;
    public static final double MILLIMETER_OF_A_METER = 1000d;

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

    ArrayList<PNode> children = new ArrayList<>();
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
        this(ptfTemplate.template, ptfTemplate.resolution, ptfTemplate.scale, ptfTemplate.mappingComponent);
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
     * @param  scale             DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PrintTemplateFeature(final Template template,
            final Resolution resolution,
            final Scale scale,
            final MappingComponent mappingComponent) {
        this.template = template;
        this.resolution = resolution;
        this.scale = scale;
        this.mappingComponent = mappingComponent;
        final int placeholderWidth = template.getMapWidth();
        final int placeholderHeight = template.getMapHeight();
        int scaleDenominator = scale.getDenominator();
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

    //~ Methods ----------------------------------------------------------------

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
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getName()));
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
        return javax.swing.UIManager.getDefaults().getColor("Cismap.featureSelectionForeground");
    }

    @Override
    public float getTransparency() {
        return 0.75f;
    }

    @Override
    public String getName() {
        if (number == 0) {
            return getOriginalName();
        } else {
            return getOriginalName() + " - " + number;
        }
    }

    @Override
    public String getOriginalName() {
        return "Druckbereich " + getRotationAngle();
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
        children.add(new SubPNode(parent));
        children.add(new DerivedCloneArea(parent, Side.WEST));
        children.add(new DerivedCloneArea(parent, Side.EAST));
        children.add(new DerivedCloneArea(parent, Side.NORTH));
        children.add(new DerivedCloneArea(parent, Side.SOUTH));
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class DerivedCloneArea extends DerivedSubFeature {

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
                                10);
                        line[1] = RectangleMath.getPointPerpendicular(
                                getSideLineCoords(side),
                                RectangleMath.getPointFromStartByFraction(getSideLineCoords(side), 0.75),
                                10);

                        return getGF().createLineString(line).buffer(5);
                    }
                });
            this.side = side;
            setPaint(Color.white);
            setStroke(null);
            setTransparency(0.3f);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Side getSide() {
            return side;
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
interface DeriveRule {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   in  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Geometry derive(Geometry in);
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class DerivedSubFeature extends PPath implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    PFeature parent;
    DeriveRule rule;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DerivedSubFeature object.
     *
     * @param  parent  DOCUMENT ME!
     * @param  rule    DOCUMENT ME!
     */
    public DerivedSubFeature(final PFeature parent, final DeriveRule rule) {
        this.parent = parent;
        this.rule = rule;
        parent.addPropertyChangeListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final Geometry g = parent.getFeature().getGeometry();
        final DefaultStyledFeature dsf = new DefaultStyledFeature();
        dsf.setGeometry(rule.derive(g));
        final PFeature p = new PFeature(dsf, CismapBroker.getInstance().getMappingComponent());
        super.setPathTo(new GeneralPath(p.getPathReference()));
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class SubPNode extends PText {

    //~ Instance fields --------------------------------------------------------

    PNode parent;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SubPNode object.
     *
     * @param  parent  DOCUMENT ME!
     */
    public SubPNode(final PNode parent) {
        // super("/Users/thorsten/tmp/printer-empty.png");
        super("1");
        this.parent = parent;
        final PBounds pb = new PBounds(parent.getGlobalBounds().getCenter2D(), 0, 0);
        this.setBounds(deriveBoundsFromParent());
        this.setPaint(Color.RED);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void parentBoundsChanged() {
        super.parentBoundsChanged();
        this.setBounds(deriveBoundsFromParent());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected PBounds deriveBoundsFromParent() {
        final Point2D p = parent.getGlobalBounds().getCenter2D();
        final PBounds local = getBounds();
        final double x = p.getX(); // - (local.width / 2.0);
        final double y = p.getY(); // - (local.height / 2.0);

        final PBounds pb = new PBounds(x, y, local.width, local.height);
        return pb;
    }
}
