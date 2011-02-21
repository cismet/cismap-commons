/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * PFeature.java
 *
 * Created on 12. April 2005, 10:52
 */
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

import pswing.PSwing;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.AnnotatedFeature;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.Highlightable;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.RasterDocumentFeature;
import de.cismet.cismap.commons.features.Selectable;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.DrawSelectionFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.collections.MultiMap;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class PFeature extends PPath implements Highlightable, Selectable, Refreshable {

    //~ Static fields/initializers ---------------------------------------------

    private static final String DIALOG_TEXT = org.openide.util.NbBundle.getMessage(
            PFeature.class,
            "PFeature.DIALOG_TEXT");  // NOI18N
    private static final String DIALOG_TITLE = org.openide.util.NbBundle.getMessage(
            PFeature.class,
            "PFeature.DIALOG_TITLE"); // NOI18N
    private static final Color TRANSPARENT = new Color(255, 255, 255, 0);
    private static final Stroke FIXED_WIDTH_STROKE = new FixedWidthStroke();

    //~ Instance fields --------------------------------------------------------

    ArrayList splitHandlesBetween = new ArrayList();
    PHandle splitPolygonFromHandle = null;
    PHandle splitPolygonToHandle = null;
    PHandle ellipseHandle = null;
    PFeature selectedOriginal = null;
    PPath splitPolygonLine;
    List<Point2D> splitPoints = new ArrayList<Point2D>();

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Feature feature;
    private WorldToScreenTransform wtst;
    private double x_offset = 0.0d;
    private double y_offset = 0.0d;
    private float[] xp;
    private float[] yp;
    private PNode stickyChild = null;
    private PNode secondStickyChild = null;
    private PNode infoNode = null;
    private Point2D mid = null;
    private PHandle pivotHandle = null;
    private boolean selected = false;
    private Paint nonSelectedPaint = null;
    private boolean highlighted = false;
    private Paint nonHighlightingPaint = null;
    private Coordinate[] coordArr;
    private MappingComponent viewer;
    private String geometryType = "unknown";                               // NOI18N
    private Stroke stroke = null;
    private Paint strokePaint = null;
//    private ColorTintFilter tinter;
    private ImageIcon pushpinIco = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/pushpin.png"));         // NOI18N
    private ImageIcon pushpinSelectedIco = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/pushpinSelected.png")); // NOI18N
    private boolean ignoreStickyFeature = false;
    private InfoPanel infoPanel;
    private JComponent infoComponent;
    private PSwing pswingComp;
    private PText primaryAnnotation = null;
    private FeatureAnnotationSymbol pi = null;
    private double sweetPureX = 0;
    private double sweetPureY = 0;
    private double sweetSelX = 0;
    private double sweetSelY = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PFeature.
     *
     * @param  feature  the underlying Feature
     * @param  viewer   MappingComponent
     */
    public PFeature(final Feature feature, final MappingComponent viewer) {
        this(feature, viewer.getWtst(), 0, 0, viewer);
    }

    /**
     * Creates a new PFeature object.
     *
     * @param  feature   DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     * @param  x_offset  DOCUMENT ME!
     * @param  y_offset  DOCUMENT ME!
     * @param  viewer    DOCUMENT ME!
     */
    public PFeature(final Feature feature,
            final WorldToScreenTransform wtst,
            final double x_offset,
            final double y_offset,
            final MappingComponent viewer) {
        this(feature, wtst, x_offset, y_offset, viewer, false);
    }

    /**
     * Creates a new PFeature object.
     *
     * @param  canvasPoints  DOCUMENT ME!
     * @param  wtst          DOCUMENT ME!
     * @param  x_offset      DOCUMENT ME!
     * @param  y_offset      DOCUMENT ME!
     * @param  viewer        DOCUMENT ME!
     */
    @Deprecated
    public PFeature(final Point2D[] canvasPoints,
            final WorldToScreenTransform wtst,
            final double x_offset,
            final double y_offset,
            final MappingComponent viewer) {
        this(new PureNewFeature(canvasPoints, wtst), wtst, 0, 0, viewer);
    }

    /**
     * Creates a new PFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     * @param  x_offset  DOCUMENT ME!
     * @param  y_offset  DOCUMENT ME!
     * @param  viewer    DOCUMENT ME!
     */
    @Deprecated
    public PFeature(final Coordinate[] coordArr,
            final WorldToScreenTransform wtst,
            final double x_offset,
            final double y_offset,
            final MappingComponent viewer) {
        this(new PureNewFeature(coordArr, wtst), wtst, 0, 0, viewer);
    }

    /**
     * Creates a new PFeature object.
     *
     * @param  feature              DOCUMENT ME!
     * @param  wtst                 DOCUMENT ME!
     * @param  x_offset             DOCUMENT ME!
     * @param  y_offset             DOCUMENT ME!
     * @param  viewer               DOCUMENT ME!
     * @param  ignoreStickyfeature  DOCUMENT ME!
     */
    @Deprecated
    public PFeature(final Feature feature,
            final WorldToScreenTransform wtst,
            final double x_offset,
            final double y_offset,
            final MappingComponent viewer,
            final boolean ignoreStickyfeature) {
        try {
            setFeature(feature);
            this.ignoreStickyFeature = ignoreStickyfeature;
            this.wtst = wtst;
//            this.x_offset=x_offset;
//            this.y_offset=y_offset;
            this.x_offset = 0;
            this.y_offset = 0;
            this.viewer = viewer;

            visualize();
            addInfoNode();
            refreshDesign();

            stroke = getStroke();
            strokePaint = getStrokePaint();
//            tinter = new ColorTintFilter(Color.BLUE, 0.5f);
        } catch (Throwable t) {
            log.error("Error in constructor of PFeature", t); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PNode getPrimaryAnnotationNode() {
        return primaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public PBounds boundsFromRectPolygonGeom(final Geometry g) {
        if (g instanceof Polygon) {
            final Polygon poly = (Polygon)g;
            if (poly.isRectangle()) {
                final Coordinate[] coords = poly.getCoordinates();
                final Coordinate first = coords[0];
                final PBounds b = new PBounds();
                // init
                double x1 = first.x;
                double x2 = first.x;
                double y1 = first.y;
                double y2 = first.y;
                for (int i = 0; i < coords.length; ++i) {
                    final Coordinate c = coords[i];
                    if (c.x < x1) {
                        x1 = c.x;
                    }
                    if (c.x > x2) {
                        x2 = c.x;
                    }
                    if (c.y < y1) {
                        y1 = c.y;
                    }
                    if (c.y > y1) {
                        y2 = c.y;
                    }
                }
                return new PBounds(wtst.getScreenX(x1), wtst.getScreenY(y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            }
        }
        throw new IllegalArgumentException("Geometry is not a rectangle polygon!"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     */
    public void visualize() {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("visualize()", new CurrentStackTrace()); // NOI18N
            }
        }

        final Geometry geom = CrsTransformer.transformToCurrentCrs(feature.getGeometry());
        if (feature instanceof RasterDocumentFeature) {
            final RasterDocumentFeature rdf = (RasterDocumentFeature)feature;
            final PImage pImage = new PImage(rdf.getRasterDocument());
            final PBounds bounds = boundsFromRectPolygonGeom(geom);
            // x,y,with,heigth
            pImage.setBounds(bounds);
            addChild(pImage);
            doGeometry(geom);
        } else {
            if ((geom instanceof Polygon) || (geom instanceof LineString) || (geom instanceof MultiLineString)) {
                doGeometry(geom);
            } else if (geom instanceof MultiPolygon) {
//                MultiPolygon mp = (MultiPolygon) geom;
                doGeometry(geom);
            } else if ((geom instanceof Point) || (geom instanceof MultiPoint)) {
                doGeometry(geom);
                if (feature instanceof StyledFeature) {
                    if ((pi == null)
                                || ((pi != null) && pi.equals(((StyledFeature)feature).getPointAnnotationSymbol()))) {
                        try {
                            // log.debug("Sweetspot updated");
                            pi = new FeatureAnnotationSymbol(((StyledFeature)getFeature()).getPointAnnotationSymbol()
                                            .getImage());
                            if (log.isDebugEnabled()) {
                                log.debug("newSweetSpotx: "
                                            + ((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX()); // NOI18N
                            }
                            pi.setSweetSpotX(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX());
                            pi.setSweetSpotY(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotY());
                        } catch (Throwable ex) {
                            log.warn("No PointAnnotationSymbol found", ex);                                              // NOI18N
                            pi = new FeatureAnnotationSymbol(pushpinIco.getImage());
                            pi.setSweetSpotX(0.46d);
                            pi.setSweetSpotY(0.9d);
                        }
                    } else if ((pi != null) && (getFeature() != null) && (getFeature() instanceof StyledFeature)
                                && (((StyledFeature)getFeature()).getPointAnnotationSymbol() != null)) {
//                        log.fatal("Sweetspot updated");                                                                  // NOI18N
                        if (log.isDebugEnabled()) {
                            log.debug("newSweetSpotx: "
                                        + ((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX());     // NOI18N
                        }
                        pi.setSweetSpotX(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotX());
                        pi.setSweetSpotY(((StyledFeature)getFeature()).getPointAnnotationSymbol().getSweetSpotY());
                    }
                }
                if (!ignoreStickyFeature) {
                    viewer.addStickyNode(pi);
                }

                // Hier soll getestet werden ob bei einem Punkt der pushpin schon hinzugef\u00FCgt wurde. Wegen
                // reconsider Feature
                if (stickyChild == null) {
                    stickyChild = pi;
                } else {
                    if (stickyChild instanceof StickyPText) {
                        secondStickyChild = pi;
                    }
                }
                addChild(pi);
                pi.setOffset(wtst.getScreenX(coordArr[0].x), wtst.getScreenY(coordArr[0].y));
            }
            if (pi != null) {
                sweetPureX = pi.getSweetSpotX();
                sweetPureY = pi.getSweetSpotY();
                sweetSelX = -1.0d;
                sweetSelY = -1.0d;
            }
            setSelected(isSelected());
        }
    }

    /**
     * Liefert eine exakte Kopie dieses PFeatures. Es besitzt denselben Inhalt, jedoch einen anderen Hashwert als das
     * Original.
     *
     * @return  Kopie dieses PFeatures
     */
    @Override
    public Object clone() {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("clone()", new CurrentStackTrace()); // NOI18N
            }
        }
        final PFeature p = new PFeature(feature, wtst, this.x_offset, y_offset, viewer);
        p.splitPolygonFromHandle = splitPolygonFromHandle;
        p.splitPolygonToHandle = splitPolygonToHandle;
        return p;
    }

    /**
     * Gleicht die Geometrie an das PFeature an. Erstellt die jeweilige Geometrie (Punkt, Linie, Polygon) und f\u00FCgt
     * sie dem Feature hinzu. Das CRS der Feature-Geometry wird dabei mit dem CRS der PFeature-Geometry ueberschrieben.
     */
    public void syncGeometry() {
        try {
            if (getFeature().isEditable()) {
                // TODO Im Moment nur f\u00FCr einfache Polygone ohne L\u00F6cher
                if (coordArr != null) {
                    final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            CismapBroker.getInstance().getDefaultCrsAlias());
                    final CrsTransformer defaultTranformer = new CrsTransformer(CismapBroker.getInstance()
                                    .getDefaultCrs());
                    final Coordinate[] defaultCoordArr = defaultTranformer.transformGeometry(
                            coordArr,
                            CismapBroker.getInstance().getSrs().getCode());
                    if (viewer.isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("syncGeometry:Point"); // NOI18N
                        }
                    }
                    if (defaultCoordArr.length == 1) {
                        // Point
                        final Point p = gf.createPoint(defaultCoordArr[0]);
                        feature.setGeometry(p);
                        if (viewer.isFeatureDebugging()) {
                            if (log.isDebugEnabled()) {
                                log.debug("syncGeometry:Point:" + p); // NOI18N
                            }
                        }
                    } else if ((defaultCoordArr.length > 3)
                                && defaultCoordArr[0].equals(defaultCoordArr[defaultCoordArr.length - 1])) {
                        // simple Polygon
                        final LinearRing shell = gf.createLinearRing(defaultCoordArr);
                        final Polygon poly = gf.createPolygon(shell, null);
                        feature.setGeometry(poly);
                        if (viewer.isFeatureDebugging()) {
                            if (log.isDebugEnabled()) {
                                log.debug("syncGeometry:Polygon:" + poly); // NOI18N
                            }
                        }
                    } else {
                        // Linestring
                        final LineString line = gf.createLineString(defaultCoordArr);
                        feature.setGeometry(line);
                        if (viewer.isFeatureDebugging()) {
                            if (log.isDebugEnabled()) {
                                log.debug("syncGeometry:Line:" + line); // NOI18N
                            }
                        }
                    }
                } else {
                    log.warn("coordArr==null");                         // NOI18N
                }
            }
        } catch (Exception e) {
            log.error("Error while synchronising PFeature with feature.");
        }
    }

    /**
     * Erzeugt Koordinaten- und Punktarrays aus einem gegebenen Geometry-Objekt.
     *
     * @param  geom  vorhandenes Geometry-Objekt
     */
    private void doGeometry(final Geometry geom) {
        getPathReference().reset();
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("Enter doGeometry()");      // NOI18N
            }
        }
        if (geom instanceof Point) {
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("Point");               // NOI18N
                }
            }
            coordArr = new Coordinate[1];
            coordArr[0] = ((Point)geom).getCoordinate();
        } else if (geom instanceof MultiPoint) {
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("MultiPoint");          // NOI18N
                }
            }
            coordArr = ((MultiPoint)geom).getCoordinates();
        } else if ((geom instanceof Polygon) || (geom instanceof LinearRing)) {
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("Polygon||LinearRing"); // NOI18N
                }
            }
            final Polygon p = (Polygon)geom;
            doPolygon(p);
        } else if (geom instanceof MultiPolygon) {
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("MultiPolygon" + geom); // NOI18N
                }
            }
            final MultiPolygon mp = (MultiPolygon)geom;
            getPathReference().reset();
            getPathReference().setWindingRule(GeneralPath.WIND_EVEN_ODD);
            for (int i = 0; i < mp.getNumGeometries(); ++i) {
                final Polygon p = (Polygon)mp.getGeometryN(i);
                doPolygon(p);
            }
        } else if (geom instanceof LineString) {
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("LineString");          // NOI18N
                }
            }
            coordArr = ((LineString)geom).getCoordinates();
        } else if (geom instanceof MultiLineString) {
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("MultiLineString");     // NOI18N
                }
            }
            final MultiLineString mls = (MultiLineString)geom;
            final Vector<Coordinate[]> coordSubArrs = new Vector<Coordinate[]>();
            int coordArrayLength = 0;
            for (int i = 0; i < mls.getNumGeometries(); ++i) {
                final LineString ls = (LineString)mls.getGeometryN(i);
                final Coordinate[] coordSubArr = ls.getCoordinates();
                coordSubArrs.add(coordSubArr);
                addLinearRing(coordSubArr);
                coordArrayLength += coordSubArr.length;
            }
            coordArr = new Coordinate[coordArrayLength];
            int arrayCopyCursor = 0;
            for (final Coordinate[] coordSubArr : coordSubArrs) {
                System.arraycopy(coordSubArr, 0, coordArr, arrayCopyCursor, coordSubArr.length);
                arrayCopyCursor += coordSubArr.length;
            }
        }
        xp = new float[coordArr.length];
        yp = new float[coordArr.length];

        final Coordinate[] points = transformCoordinateArr(coordArr);
        for (int i = 0; i < coordArr.length; ++i) {
            xp[i] = (float)(points[i].x);
            yp[i] = (float)(points[i].y);
        }

        if (geom instanceof Point) {
            setPathToPolyline(new float[] { xp[0], xp[0] }, new float[] { yp[0], yp[0] });
        } else if (geom instanceof LineString) {
            setPathToPolyline(xp, yp);
        } else if (geom instanceof MultiPoint) {
            setPathToPolyline(xp, yp);
        }

        refreshDesign();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  p  DOCUMENT ME!
     */
    private void doPolygon(final Polygon p) {
        coordArr = p.getCoordinates();
        final Coordinate[] ext = p.getExteriorRing().getCoordinates();

        addLinearRing(ext);
        getPathReference().setWindingRule(GeneralPath.WIND_EVEN_ODD);
        for (int i = 0; i < p.getNumInteriorRing(); ++i) {
            final Coordinate[] hole = p.getInteriorRingN(i).getCoordinates();
            addLinearRing(hole);
        }
    }
//    private void doLineString(LineString ls)  {
//        Coordinate[] coords=ls.getCoordinates();
//        Coordinate[] points = transformCoordinateArr(coords);
//        GeneralPath gp = new GeneralPath();
//        gp.reset();
//        gp.moveTo((float) points[0].x, (float) points[0].y);
//    }

    /**
     * F\u00FCgt dem PFeature ein weiteres Coordinate-Array hinzu. Dadurch entstehen Multipolygone und Polygone mit
     * L\u00F6chern, je nachdem, ob der neue LinearRing ausserhalb oder innerhalb des PFeatures liegt.
     *
     * @param  coordinateArr  die Koordinaten des hinzuzuf\u00FCgenden Rings als Coordinate-Array
     */
    private void addLinearRing(final Coordinate[] coordinateArr) {
        final Coordinate[] points = transformCoordinateArr(coordinateArr);
        final GeneralPath gp = new GeneralPath();
        gp.reset();
        gp.moveTo((float)points[0].x, (float)points[0].y);
        for (int i = 1; i < points.length; i++) {
            gp.lineTo((float)points[i].x, (float)points[i].y);
        }
        append(gp, false);
        // setPathToPolyline(xp, yp);
    }

    /**
     * Erzeugt PCanvas-Koordinaten-Punktarrays aus Realworldkoordinaten.
     *
     * @param   coordinateArr  Array mit Realworld-Koordinaten
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate[] transformCoordinateArr(final Coordinate[] coordinateArr) {
        final Coordinate[] points = new Coordinate[coordinateArr.length];
        for (int i = 0; i < coordinateArr.length; ++i) {
            points[i] = new Coordinate();
            if (wtst == null) {
                points[i].x = (float)(coordinateArr[i].x + x_offset);
                points[i].y = (float)(coordinateArr[i].y + y_offset);
            } else {
                points[i].x = (float)(wtst.getDestX(coordinateArr[i].x) + x_offset);
                points[i].y = (float)(wtst.getDestY(coordinateArr[i].y) + y_offset);
            }
        }

        return points;
    }

    /**
     * Setzt die Zeichenobjekte des Features (z.B. unselektiert=rot) und st\u00F6\u00DFt ein Neuzeichnen an.
     */
    public void refreshDesign() {
        if (primaryAnnotation != null) {
            removeChild(primaryAnnotation);
            viewer.removeStickyNode(primaryAnnotation);
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("refreshDesign()", new CurrentStackTrace()); // NOI18N
            }
        }
        if (getFeature().isHidden() && !getFeature().isEditable()) {
            setStroke(null);
            setPaint(null);
        } else {
            // hier muss die Anpassung bei den WFS Features hin.
            Stroke overridingstroke = null;
            if (getFeature() instanceof XStyledFeature) {
                final XStyledFeature xsf = (XStyledFeature)getFeature();
                overridingstroke = xsf.getLineStyle();
            }

            if (getFeature() instanceof RasterDocumentFeature) {
                overridingstroke = FIXED_WIDTH_STROKE;
            }

            if ((getFeature() instanceof StyledFeature) && (overridingstroke == null)) {
                final StyledFeature sf = (StyledFeature)getFeature();
                if (sf.getLineWidth() <= 1) {
                    setStroke(FIXED_WIDTH_STROKE);
                } else {
                    final CustomFixedWidthStroke old = new CustomFixedWidthStroke(sf.getLineWidth());
                    setStroke(old);
                }
                // Falls absichtlich keine Linie gesetzt worden ist (z.B. im StyleDialog)
                if (sf.getLinePaint() == null) {
                    setStroke(null);
                }
            }

            if (overridingstroke != null) {
                setStroke(overridingstroke);
            }
            if ((getFeature().getGeometry() instanceof LineString)
                        || (getFeature().getGeometry() instanceof MultiLineString)) {
                if ((feature instanceof StyledFeature)) {
                    final java.awt.Paint linePaint = ((StyledFeature)feature).getLinePaint();
                    if (linePaint != null) {
                        setStrokePaint(linePaint);
                    }
                }
            } else {
                if ((feature instanceof StyledFeature)) {
                    final java.awt.Paint paint = ((StyledFeature)feature).getFillingPaint();
                    final java.awt.Paint linePaint = ((StyledFeature)feature).getLinePaint();
                    if (paint != null) {
                        setPaint(paint);
                        nonHighlightingPaint = paint;
                    }
                    if (linePaint != null) {
                        setStrokePaint(linePaint);
                    }
                }
            }
            stroke = getStroke();
            strokePaint = getStrokePaint();
            setSelected(this.isSelected());

            // TODO:Wenn feature=labeledFeature jetzt noch Anpassungen machen
            if (((feature instanceof AnnotatedFeature) && ((AnnotatedFeature)feature).isPrimaryAnnotationVisible()
                            && (((AnnotatedFeature)feature).getPrimaryAnnotation() != null))) {
                final AnnotatedFeature af = (AnnotatedFeature)feature;
                primaryAnnotation = new StickyPText(af.getPrimaryAnnotation());
                primaryAnnotation.setJustification(af.getPrimaryAnnotationJustification());
                if (af.isAutoscale()) {
                    stickyChild = primaryAnnotation;
                }
                viewer.getCamera()
                        .addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {

                                @Override
                                public void propertyChange(final PropertyChangeEvent evt) {
                                    setVisibility(primaryAnnotation, af);
                                }
                            });
                // if (true || af.getMaxScaleDenominator() == null || af.getMinScaleDenominator() == null ||
                // af.getMaxScaleDenominator() > denom && af.getMinScaleDenominator() < denom) {

                if (af.getPrimaryAnnotationPaint() != null) {
                    primaryAnnotation.setTextPaint(af.getPrimaryAnnotationPaint());
                } else {
                    primaryAnnotation.setTextPaint(Color.BLACK);
                }
                if (af.getPrimaryAnnotationScaling() > 0) {
                    primaryAnnotation.setScale(af.getPrimaryAnnotationScaling());
                }
                if (af.getPrimaryAnnotationFont() != null) {
                    primaryAnnotation.setFont(af.getPrimaryAnnotationFont());
                }
                final boolean vis = primaryAnnotation.getVisible();

                final Point intPoint = CrsTransformer.transformToCurrentCrs(feature.getGeometry()).getInteriorPoint();

                primaryAnnotation.setOffset(wtst.getScreenX(intPoint.getX()), wtst.getScreenY(intPoint.getY()));

                addChild(primaryAnnotation);

                if (!ignoreStickyFeature && af.isAutoscale()) {
                    viewer.addStickyNode(primaryAnnotation);
                    viewer.rescaleStickyNode(primaryAnnotation);
                }
                setVisibility(primaryAnnotation, af);
                // }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ptext  DOCUMENT ME!
     * @param  af     DOCUMENT ME!
     */
    private void setVisibility(final PText ptext, final AnnotatedFeature af) {
        final double denom = viewer.getScaleDenominator();
        if ((af.getMaxScaleDenominator() == null) || (af.getMinScaleDenominator() == null)
                    || ((af.getMaxScaleDenominator() > denom) && (af.getMinScaleDenominator() < denom))) {
            ptext.setVisible(true);
        } else {
            ptext.setVisible(false);
        }
    }

    /**
     * Entfernt eine Koordinate aus der Geometrie, z.B. beim L\u00F6schen eines Handles.
     *
     * @param   position  Position des zu l\u00F6schenden Punkes im Koordinatenarray
     * @param   original  Koordinatenarray der Geometrie
     *
     * @return  Koordinatenarray ohne die gew\u00FCnschte Koordinate
     */
    private Coordinate[] removeCoordinate(final int position, final Coordinate[] original) {
        if (((getFeature().getGeometry() instanceof Polygon) && (original != null)
                        && ((original.length - 1) > position))
                    || ((getFeature().getGeometry() instanceof LineString) && (original != null)
                        && (original.length > position)
                        && (original.length > 2))) {
            final Coordinate[] newCoordinates = new Coordinate[original.length - 1];
            // vorher
            for (int i = 0; i < position; ++i) {
                newCoordinates[i] = original[i];
            }
            // zu entferndes Element \u00FCberspringen

            // nachher
            for (int i = position; i < newCoordinates.length; ++i) {
                newCoordinates[i] = original[i + 1];
            }

            // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist (nur beim Polygon)
            if ((position == 0) && (getFeature().getGeometry() instanceof Polygon)) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            }
            if (log.isInfoEnabled()) {
                log.info("Original: " + original);  // NOI18N
                log.info("New: " + newCoordinates); // NOI18N
            }
            return newCoordinates;
        } else {
            if (original != null) {
                return original;
            } else {
                return null;
            }
        }
    }

    /**
     * Entfernt einen Punkt aus der Geometrie, z.B. beim L\u00F6schen eines Handles.
     *
     * @param   position  Position des zu l\u00F6schenden Punkes im PCanvas-Punktarray
     * @param   original  PCanvas-Punktarray der Geometrie
     *
     * @return  PCanvas-Punktarray, ohne den gew\u00FCnschten Punkt
     */
    private float[] removeCoordinate(final int position, final float[] original) {
        if (((getFeature().getGeometry() instanceof Polygon) && (original != null)
                        && ((original.length - 1) > position))
                    || ((getFeature().getGeometry() instanceof LineString) && (original != null)
                        && (original.length > position)
                        && (original.length > 2))) {
            final float[] newCoordinates = new float[original.length - 1];
            // vorher
            for (int i = 0; i < position; ++i) {
                newCoordinates[i] = original[i];
            }
            // zu entferndes Element \u00FCberspringen

            // nachher
            for (int i = position; i < newCoordinates.length; ++i) {
                newCoordinates[i] = original[i + 1];
            }
            // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist (nur beim Polygon)
            if ((position == 0) && (getFeature().getGeometry() instanceof Polygon)) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            }
            return newCoordinates;
        } else {
            if (original != null) {
                return original;
            } else {
                return null;
            }
        }
    }

    /**
     * F\u00FCgt eine neue \u00FCbergebene Koordinate in das Koordinatenarray ein, statt nur einen Punkt zu duplizieren.
     *
     * @param   position  die Position des neuen Punktes im Array
     * @param   original  das Original-Array
     * @param   newValue  der einzuf\u00FCgende Wert
     *
     * @return  Coordinate-Array mit der neue Koordinate
     */
    public Coordinate[] insertCoordinate(final int position, final Coordinate[] original, final Coordinate newValue) {
        if (((getFeature().getGeometry() instanceof Polygon) && (original != null)
                        && ((original.length - 1) >= position))
                    || ((getFeature().getGeometry() instanceof LineString) && (original != null)
                        && (original.length > position)
                        && (original.length > 2))) {
            final Coordinate[] newCoordinates = new Coordinate[original.length + 1];
            // vorher
            for (int i = 0; i < position; ++i) {
                newCoordinates[i] = original[i];
            }

            newCoordinates[position] = newValue;

            // nachher
            for (int i = position; i < original.length; ++i) {
                newCoordinates[i + 1] = original[i];
            }

            // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist
            if ((position == 0) && (getFeature().getGeometry() instanceof Polygon)) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            } else if ((position == (original.length - 1)) && (getFeature().getGeometry() instanceof Polygon)) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            }
            return newCoordinates;
        } else {
            if (original != null) {
                return original;
            } else {
                return null;
            }
        }
    }

    /**
     * F\u00FCgt eine neue \u00FCbergebene Koordinate in das Koordinatenarray ein, statt nur einen Punkt zu duplizieren.
     *
     * @param   position  die Position des neuen Punktes im Array
     * @param   original  das Original-Array
     * @param   newValue  der einzuf\u00FCgende Wert
     *
     * @return  float-Array mit der neu eingef\u00FCgten Koordinate
     */
    public float[] insertCoordinate(final int position, final float[] original, final float newValue) {
        if (((getFeature().getGeometry() instanceof Polygon) && (original != null)
                        && ((original.length - 1) >= position))
                    || ((getFeature().getGeometry() instanceof LineString) && (original != null)
                        && (original.length > position)
                        && (original.length > 2))) {
            final float[] newCoordinates = new float[original.length + 1];
            // vorher
            for (int i = 0; i < position; ++i) {
                newCoordinates[i] = original[i];
            }

            newCoordinates[position] = newValue;

            // nachher
            for (int i = position; i < original.length; ++i) {
                newCoordinates[i + 1] = original[i];
            }

            // Sicherstellen dass der neue Anfangspunkt auch der Endpukt ist
            if ((position == 0) && (getFeature().getGeometry() instanceof Polygon)) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            } else if ((position == (original.length - 1)) && (getFeature().getGeometry() instanceof Polygon)) {
                newCoordinates[newCoordinates.length - 1] = newCoordinates[0];
            }
            return newCoordinates;
        } else {
            if (original != null) {
                return original;
            } else {
                return null;
            }
        }
    }

    /**
     * Erm\u00F6glicht es die Moethde removeCoordinate() von ausserhalb aufzurufen.
     *
     * @param   position  Position des neuen Punkes im PCanvas-Punktarray
     * @param   original  PCanvas-Punktarray der Geometrie
     *
     * @return  PCanvas-Punktarray, mit dem neuen Punkt
     */
    public float[] removeCoordinateFromOutside(final int position, final float[] original) {
        return removeCoordinate(position, original);
    }

    /**
     * Erm\u00F6glicht es die Moethde removeCoordinate() von ausserhalb aufzurufen.
     *
     * @param   position  Position des neuen Punkes im Coordinate-Array
     * @param   original  Coordinate-Array der Geometrie
     *
     * @return  Coordinate-Array, mit der neuen Koordinate
     */
    public Coordinate[] removeCoordinateFromOutside(final int position, final Coordinate[] original) {
        return removeCoordinate(position, original);
    }

    /**
     * Erzeugt alle Handles f\u00FCr dieses PFeature auf dem \u00FCbergebenen HandleLayer.
     *
     * @param  handleLayer  PLayer der die Handles aufnimmt
     */
    public void addHandles(final PNode handleLayer) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addHandles(): Hinzuf\u00FCgen von " + xp.length + " Handles"); // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addHandles(): PFeature:" + this);                              // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addHandles(): " + xp[0] + "--" + xp[xp.length - 1]);           // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addHandles(): " + yp[0] + "--" + yp[yp.length - 1]);           // NOI18N
            }
        }
        if (getFeature() instanceof LinearReferencedPointFeature) {
            addLinearReferencedPointPHandle(handleLayer);
        } else if ((getFeature() instanceof PureNewFeature)
                    && (((PureNewFeature)getFeature()).getGeometryType() == PureNewFeature.geomTypes.ELLIPSE)) {
            addEllipseHandle(handleLayer);
        } else {
            int length = xp.length;
            if (getFeature().getGeometry() instanceof Polygon) {
                length--;                                                                 // xp.length-1 weil der erste
                                                                                          // und letzte Punkt identisch
                                                                                          // sind
            }
            for (int i = 0; i < length; ++i) {
                addHandle(handleLayer, i);
            }
        }
    }

    /**
     * Erzeugt ein PHandle an den Koordinaten eines bestimmten Punktes des Koordinatenarrays und f\u00FCgt es dem
     * HandleLayer hinzu.
     *
     * @param  handleLayer  PLayer dem das Handle als Kind hinzugef\u00FCgt wird
     * @param  position     Position des Punktes im Koordinatenarray
     */
    public void addHandle(final PNode handleLayer, final int position) {
        final int positionInArray = position;

        final PHandle h = new TransformationPHandle(this, positionInArray);

//        EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
        handleLayer.addChild(h);
        h.addClientProperty("coordinate", getCoordArr()[position]);               // NOI18N
        h.addClientProperty("coordinate_position_in_arr", new Integer(position)); // NOI18N
//            }
//        });
    }

    /**
     * Pr\u00FCft alle Features, ob sie zu das gegebene PFeature \u00FCberschneiden und ein Handle besitzen das weniger
     * als 1cm vom angeklickten Handle entfernt ist. Falls beides zutrifft, wird eine MultiMap mit diesen Features
     * gef\u00FCllt und zur\u00FCckgegeben.
     *
     * @param   posInArray  Postion des geklickten Handles im Koordinatenarray um Koordinaten herauszufinden
     *
     * @return  MultiMap mit Features, die die Bedingungen erf\u00FCllen
     */
    public de.cismet.tools.collections.MultiMap checkforGlueCoords(final int posInArray) {
        final GeometryFactory gf = new GeometryFactory();
        final MultiMap glueCoords = new MultiMap();

        // Alle vorhandenen Features holen und pr\u00FCfen
        final Vector<Feature> allFeatures = getViewer().getFeatureCollection().getAllFeatures();
        for (final Feature f : allFeatures) {
            // \u00DCberschneiden sich die Features? if (!f.equals(PFeature.this.getFeature()) &&
            // f.getGeometry().intersects(PFeature.this.getFeature().getGeometry()) ){
            if (!f.equals(PFeature.this.getFeature())) {
                final Geometry fgeo = CrsTransformer.transformToCurrentCrs(f.getGeometry());
                final Geometry thisGeo = CrsTransformer.transformToCurrentCrs(PFeature.this.getFeature().getGeometry());
                if (fgeo.buffer(0.01).intersects(thisGeo.buffer(0.01))) {
                    final Point p = gf.createPoint(coordArr[posInArray]);
                    // Erzeuge Array mit allen Eckpunkten
                    final Coordinate[] ca = fgeo.getCoordinates();

                    // Prüfe für alle Punkte ob der Abstand < 1cm ist
                    for (int i = 0; i < ca.length; ++i) {
                        final Point p2 = gf.createPoint(ca[i]);
                        final double abstand = p.distance(p2);
                        if (abstand < 0.01) {
                            glueCoords.put(getViewer().getPFeatureHM().get(f), i);
                            if (viewer.isFeatureDebugging()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("checkforGlueCoords() Abstand kleiner als 1cm: " + abstand + " :: " + f); // NOI18N
                                }
                            }
                        } else {
                            if (viewer.isFeatureDebugging()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("checkforGlueCoords() Abstand: " + abstand);                              // NOI18N
                                }
                            }
                        }
                    }
                }
            }
        }
        return glueCoords;
    }

    /**
     * Erzeugt alle RotaionHandles f\u00FCr dieses PFeature auf dem \u00FCbergebenen HandleLayer.
     *
     * @param  handleLayer  PLayer der die RotationHandles aufnimmt
     */
    public void addRotationHandles(final PNode handleLayer) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addRotationHandles(): PFeature:" + this); // NOI18N
            }
        }
        int length = xp.length;
        if (getFeature().getGeometry() instanceof Polygon) {
            length--;                                                // xp.length-1 weil der erste und letzte Punkt
                                                                     // identisch sind
        }
        // SchwerpunktHandle erzeugen
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("PivotHandle==" + pivotHandle); // NOI18N
            }
        }
        if (pivotHandle == null) {
            addPivotHandle(handleLayer);
        } else {
            boolean contains = false;
            for (final Object o : handleLayer.getChildrenReference()) {
                if (o == pivotHandle) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
//                EventQueue.invokeLater(new Runnable() {
//
//                    public void run() {
                handleLayer.addChild(pivotHandle);
//                    }
//                });
            }
        }
        // Handles einfügen
        for (int i = 0; i < length; ++i) {
            addRotationHandle(handleLayer, i);
        }
    }

    /**
     * F\u00FCgt dem PFeature spezielle Handles zum Rotieren des PFeatures an den Eckpunkten hinzu. Zus\u00E4tzlich ein
     * Handle am Rotationsmittelpunkt, um diesen manuell \u00E4nder nzu k\u00F6nnen.
     *
     * @param  handleLayer  HandleLayer der MappingComponent
     * @param  position     DOCUMENT ME!
     */
    public void addRotationHandle(final PNode handleLayer, final int position) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addRotationHandles():add from " + position + ". RotationHandle"); // NOI18N
            }
        }

        final PHandle rotHandle = new RotationPHandle(this, mid, pivotHandle, position);

        rotHandle.setPaint(new Color(1f, 1f, 0f, 0.7f));
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
        handleLayer.addChild(rotHandle);
        rotHandle.addClientProperty("coordinate", getCoordArr()[position]);               // NOI18N
        rotHandle.addClientProperty("coordinate_position_in_arr", new Integer(position)); // NOI18N
//            }
//        });
    }

    /**
     * Erzeugt den Rotations-Angelpunkt. Der Benutzer kann den Punkt verschieben, um die Drehung um einen anderen Punkt
     * als den Mittel-/Schwerpunkt auszuf\u00FChren.
     *
     * @param  handleLayer  PLayer der das PivotHandle aufnimmt
     */
    public void addPivotHandle(final PNode handleLayer) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addPivotHandle()"); // NOI18N
            }
        }
        PBounds allBounds = null;
        if (getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
            final Collection selectedFeatures = getViewer().getFeatureCollection().getSelectedFeatures();
            Rectangle2D tmpBounds = getBounds().getBounds2D();
            for (final Object o : selectedFeatures) {
                final PFeature pf = (PFeature)getViewer().getPFeatureHM().get(o);
                if (!(selectedFeatures.contains(pf))) {
                    tmpBounds = pf.getBounds().getBounds2D().createUnion(tmpBounds);
                }
            }
            allBounds = new PBounds(tmpBounds);
        }
        final Collection selArr = getViewer().getFeatureCollection().getSelectedFeatures();
        for (final Object o : selArr) {
            final PFeature pf = (PFeature)(getViewer().getPFeatureHM().get(o));
            pf.setPivotPoint(allBounds.getCenter2D());
            mid = allBounds.getCenter2D();
        }

        pivotHandle = new PivotPHandle(this, mid);
        pivotHandle.setPaint(new Color(0f, 0f, 0f, 0.6f));
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
        handleLayer.addChild(pivotHandle);
//            }
//        });
        for (final Object o : selArr) {
            final PFeature pf = (PFeature)(getViewer().getPFeatureHM().get(o));
            pf.pivotHandle = this.pivotHandle;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  handleLayer  DOCUMENT ME!
     */
    public void addLinearReferencedPointPHandle(final PNode handleLayer) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addLinearReferencingHandle()"); // NOI18N
            }
        }

        final PHandle h = new LinearReferencedPointFeaturePHandle(this);

//        EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
        handleLayer.addChild(h);
//            }
//        });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  handleLayer  DOCUMENT ME!
     */
    public void addEllipseHandle(final PNode handleLayer) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addEllipseHandle()"); // NOI18N
            }
        }

        ellipseHandle = new EllipsePHandle(this);

        ellipseHandle.setPaint(new Color(0f, 0f, 0f, 0.6f));
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
        handleLayer.addChild(ellipseHandle);
//            }
//        });
    }

    /**
     * Sets a new pivotpoint for the roation.
     *
     * @param  newPivot  new Point2D
     */
    public void setPivotPoint(final Point2D newPivot) {
        this.mid = newPivot;
    }

    /**
     * Berechnet anhand einer Rotationsmatrix die neuen Punkte des Features, diese werden dann mittels
     * moveCoordinateToNewPiccoloPosition() auch auf die zugeh\u00F6rige Geometrie \u00FCbertragen.
     *
     * @param  rad      Winkel der Rotation im Bogenma\u00DF
     * @param  tempMid  Mittelpunkt der Rotation
     */
    public void rotateAllPoints(double rad, Point2D tempMid) {
        final double[][] matrix = new double[2][2];
        double cos;
        double sin;
        if (rad > 0.0d) { // Clockwise
            cos = Math.cos(rad);
            sin = Math.sin(rad);
            matrix[0][0] = cos;
            matrix[0][1] = sin * (-1);
            matrix[1][0] = sin;
            matrix[1][1] = cos;
        } else {          // Counterclockwise
            rad *= -1;
            cos = Math.cos(rad);
            sin = Math.sin(rad);
            matrix[0][0] = cos;
            matrix[0][1] = sin;
            matrix[1][0] = sin * (-1);
            matrix[1][1] = cos;
        }
        if (tempMid == null) {
            tempMid = mid;
        }
        for (int i = getXp().length - 1; i >= 0; i--) {
            final double dx = getXp()[i] - tempMid.getX();
            final double dy = getYp()[i] - tempMid.getY();

            // Clockwise
            final float resultX = new Double(tempMid.getX() + ((dx * matrix[0][0]) + (dy * matrix[0][1]))).floatValue();
            final float resultY = new Double(tempMid.getY() + ((dx * matrix[1][0]) + (dy * matrix[1][1]))).floatValue();

            moveCoordinateToNewPiccoloPosition(i, resultX, resultY);
        }
    }

    /**
     * Bildet aus Mausposition, Mittelpunkt und Handleposition ein Dreieck und berechnet daraus, den bei der Bewegung
     * zur\u00FCckgelegten Winkel und dessen Richtung.
     *
     * @param   event  PInputEvent der Mausbewegung
     * @param   x      X-Koordinate des Handles
     * @param   y      Y-Koordinate des Handles
     *
     * @return  \u00FCberstrichener Winkel der Bewegung im Bogenma\u00DF
     */
    public double calculateDrag(final PInputEvent event, final float x, final float y) {
        final Point2D mousePos = event.getPosition();

        // create vectors
        final double[] mv = { (mousePos.getX() - mid.getX()), (mousePos.getY() - mid.getY()) };
        final double[] hv = { (x - mid.getX()), (y - mid.getY()) };

        final double cosm = ((mv[0]) / Math.hypot(mv[0], mv[1]));
        final double cosh = ((hv[0]) / Math.hypot(hv[0], hv[1]));
        final double resH = Math.acos(cosh);
        final double resM = Math.acos(cosm);
        double res = 0;

        if (((mousePos.getY() - mid.getY()) > 0) && ((y - mid.getY()) > 0)) {
            res = resM - resH;
        } else if (((mousePos.getY() - mid.getY()) > 0) && ((y - mid.getY()) < 0)) {
            res = resM - (resH * -1);
        } else if ((y - mid.getY()) < 0) {
            res = resH - resM;
        } else if (((mousePos.getY() - mid.getY()) < 0) && ((y - mid.getY()) > 0)) {
            res = (resH * -1) - resM;
        }
        return res;
    }

    /**
     * Ver\u00E4ndert die PCanvas-Koordinaten eines Punkts des PFeatures.
     *
     * @param  positionInArray  Position des Punkts im Koordinatenarray
     * @param  newX             neue X-Koordinate
     * @param  newY             neue Y-Koordinate
     */
    public void moveCoordinateToNewPiccoloPosition(final int positionInArray, final float newX, final float newY) {
        getXp()[positionInArray] = newX;
        getYp()[positionInArray] = newY;

        // Originalgeometrie ver\u00E4ndern
        // hin :wtst.getDestX(coordArr[i].x)+x_offset)
        PFeature.this.getCoordArr()[positionInArray].x = wtst.getSourceX(getXp()[positionInArray] - x_offset);
        PFeature.this.getCoordArr()[positionInArray].y = wtst.getSourceY(getYp()[positionInArray] - y_offset);
        if ((positionInArray == 0) && (getFeature().getGeometry() instanceof Polygon)) {
            getXp()[getXp().length - 1] = getXp()[0];
            getYp()[getYp().length - 1] = getYp()[0];
            // Originalgeometrie ver\u00E4ndern
            // hin :wtst.getDestX(coordArr[i].x)+x_offset)
            PFeature.this.getCoordArr()[getXp().length - 1].x = wtst.getSourceX(getXp()[positionInArray] - x_offset);
            PFeature.this.getCoordArr()[getXp().length - 1].y = wtst.getSourceY(getYp()[positionInArray] - y_offset);
        }
        PFeature.this.setPathToPolyline(getXp(), getYp());
    }

    /**
     * Removes the current splitline and creates a new one from the startingpoint.
     */
    private void resetSplitLine() {
        removeAllChildren();
        splitPolygonLine = new PPath();
        splitPoints = new Vector();
        splitPoints.add(getFirstSplitHandle());
        splitPolygonLine.setStroke(FIXED_WIDTH_STROKE);
        // splitPolygonLine.setPaint(new Color(1f,0f,0f,0.5f));
        addChild(splitPolygonLine);
    }

    /**
     * Fügt dem PFeature ein Handle hinzu mit dem man das PFeature in zwei zerlegen kann.
     *
     * @param  p  das SplitHandle
     */
    public void addSplitHandle(final PHandle p) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("addSplitHandle()");                                                           // NOI18N
            }
        }
        if (splitPolygonFromHandle == p) {
            splitPolygonFromHandle = null;
            p.setSelected(false);
        } else if (splitPolygonToHandle == p) {
            splitPolygonToHandle = null;
            p.setSelected(false);
        } else if (splitPolygonFromHandle == null) {
            splitPolygonFromHandle = p;
            p.setSelected(true);
            resetSplitLine();
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("after addSplitHandle: splitPolygonFromHandle=" + splitPolygonFromHandle); // NOI18N
                }
            }
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("in addSplitHandle this=" + this);                                         // NOI18N
                }
            }
        } else if (splitPolygonToHandle == null) {
            splitPolygonToHandle = p;
            p.setSelected(true);
            splitPoints.add(new Point2D.Double(
                    splitPolygonToHandle.getLocator().locateX(),
                    splitPolygonToHandle.getLocator().locateY()));
        } else {
            p.setSelected(false);
        }
//LineString()
        if ((splitPolygonFromHandle != null) && (splitPolygonToHandle != null)) {
            final Coordinate[] ca = new Coordinate[splitPoints.size() + 2];
//            ca[0]=(Coordinate)splitPolygonFromHandle.getClientProperty("coordinate");
//            ca[1]=(Coordinate)splitPolygonToHandle.getClientProperty("coordinate");
//            GeometryFactory gf=new GeometryFactory();
//            LineString ls=gf.createLineString(ca);
            // Geometry geom=feature.getGeometry();
//            if ((geom.overlaps(ls))) {
//                splitPolygonLine=PPath.createLine((float)splitPolygonFromHandle.getLocator().locateX(),(float)splitPolygonFromHandle.getLocator().locateY(),
//                        (float)splitPolygonToHandle.getLocator().locateX(),(float)splitPolygonToHandle.getLocator().locateY());
//                splitPolygonLine.setStroke(new FixedWidthStroke());
//                this.addChild(splitPolygonLine);
//            }
        }
    }

    /**
     * Returns the point of the handle from which the split starts.
     *
     * @return  Point2D
     */
    public Point2D getFirstSplitHandle() {
        if ((splitPolygonFromHandle != null)
                    && (splitPolygonFromHandle.getClientProperty("coordinate") instanceof Coordinate)) { // NOI18N
            final Coordinate c = ((Coordinate)splitPolygonFromHandle.getClientProperty("coordinate"));   // NOI18N
            final Point2D ret = new Point2D.Double((double)splitPolygonFromHandle.getLocator().locateX(),
                    (double)splitPolygonFromHandle.getLocator().locateY());
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Returns if the PFeature in currently in a splitmode.
     *
     * @return  true, if splitmode is active, else false
     */
    public boolean inSplitProgress() {
        final CurrentStackTrace cst = new CurrentStackTrace();
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("splitPolygonFromHandle:" + splitPolygonFromHandle, cst);                                   // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("splitPolygonToHandle:" + splitPolygonToHandle, cst);                                       // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("inSplitProgress=" + ((splitPolygonFromHandle != null) && (splitPolygonToHandle == null))); // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("in inSplitProgress this=" + this);                                                         // NOI18N
            }
        }
        return ((splitPolygonFromHandle != null) && (splitPolygonToHandle == null));
    }

    /**
     * Zerlegt das Feature dieses PFeatures in zwei Features an Hand einer vom Benutzer gezogenen Linie zwischen 2
     * Handles.
     *
     * @return  Feature-Array mit den Teilfeatures
     */
    public Feature[] split() {
        if (isSplittable()) {
            final PureNewFeature[] ret = new PureNewFeature[2];
            int from = ((Integer)(splitPolygonFromHandle.getClientProperty("coordinate_position_in_arr"))).intValue(); // NOI18N
            int to = ((Integer)(splitPolygonToHandle.getClientProperty("coordinate_position_in_arr"))).intValue();     // NOI18N

            splitPolygonToHandle = null;
            splitPolygonFromHandle = null;

            // In splitPoints.get(0) steht immer from
            // In splitPoint.get(size-1) steht immer to
            // Werden die beiden vertauscht, so muss dies sp\u00E4ter bei der Reihenfolge ber\u00FCcksichtigt werden.
            boolean wasSwapped = false;

            if (from > to) {
                final int swap = from;
                from = to;
                to = swap;
                wasSwapped = true;
            }
            // Erstes Polygon
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("ErstesPolygon" + (to - from + splitPoints.size())); // NOI18N
                }
            }
            final Coordinate[] c1 = new Coordinate[to - from + splitPoints.size()];
            int counter = 0;
            for (int i = from; i <= to; ++i) {
                c1[counter] = (Coordinate)coordArr[i].clone();
                counter++;
            }
            if (wasSwapped) {
                if (viewer.isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("SWAPPED");                                      // NOI18N
                    }
                }
                for (int i = 1; i < (splitPoints.size() - 1); ++i) {
                    final Point2D splitPoint = (Point2D)splitPoints.get(i);
                    final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                            wtst.getSourceY(splitPoint.getY()));
                    c1[counter] = splitCoord;
                    counter++;
                }
            } else {
                if (viewer.isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("NOT_SWAPPED");                                  // NOI18N
                    }
                }
                for (int i = splitPoints.size() - 2; i > 0; --i) {
                    final Point2D splitPoint = (Point2D)splitPoints.get(i);
                    final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                            wtst.getSourceY(splitPoint.getY()));
                    c1[counter] = splitCoord;
                    counter++;
                }
            }
            c1[counter] = (Coordinate)coordArr[from].clone();
            ret[0] = new PureNewFeature(c1, wtst);
            ret[0].setEditable(true);

            // Zweites Polygon
            // Größe Array= (Anzahl vorh. Coords) - (anzahl vorh. Handles des ersten Polygons) + (SplitLinie )
            final Coordinate[] c2 = new Coordinate[(coordArr.length) - (to - from + 1) + splitPoints.size()];
            counter = 0;
            for (int i = 0; i <= from; ++i) {
                c2[counter] = (Coordinate)coordArr[i].clone();
                counter++;
            }
            if (wasSwapped) {
                if (viewer.isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("SWAPPED"); // NOI18N
                    }
                }
                for (int i = splitPoints.size() - 2; i > 0; --i) {
                    final Point2D splitPoint = (Point2D)splitPoints.get(i);
                    final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                            wtst.getSourceY(splitPoint.getY()));
                    c2[counter] = splitCoord;
                    counter++;
                }
            } else {
                if (viewer.isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("NOT_SWAPPED"); // NOI18N
                    }
                }
                for (int i = 1; i < (splitPoints.size() - 1); ++i) {
                    final Point2D splitPoint = (Point2D)splitPoints.get(i);
                    final Coordinate splitCoord = new Coordinate(wtst.getSourceX(splitPoint.getX()),
                            wtst.getSourceY(splitPoint.getY()));
                    c2[counter] = splitCoord;
                    counter++;
                }
            }

            for (int i = to; i < coordArr.length; ++i) {
                c2[counter] = (Coordinate)coordArr[i].clone();
                counter++;
            }
//            c1[counter]=(Coordinate)coordArr[0].clone();
            for (int i = 0; i < c2.length; ++i) {
                if (viewer.isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("c2[" + i + "]=" + c2[i]); // NOI18N
                    }
                }
            }
//            ret[1]=new PFeature(c2,wtst,x_offset,y_offset,viewer);
            ret[1] = new PureNewFeature(c2, wtst);
            ret[1].setEditable(true);
//            ret[0].setViewer(viewer);
//            ret[1].setViewer(viewer);
            return ret;
//            ret[1]=new PFeature(c1,wtst,x_offset,y_offset);
//            ret[0].setViewer(viewer);
//            ret[1].setViewer(viewer);
//            return ret;
        } else {
            return null;
        }
    }

    /**
     * Moves the PFeature for a certain dimension.
     *
     * @param  dim  PDimension to move
     */
    public void moveFeature(final PDimension dim) {
        try {
            final double scale = viewer.getCamera().getViewScale();
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("Scale=" + scale);        // NOI18N
                }
            }
            for (int j = 0; j < xp.length; ++j) {
                xp[j] = xp[j] + (float)(dim.getWidth() / (float)scale);
                yp[j] = yp[j] + (float)(dim.getHeight() / (float)scale);
                coordArr[j].x = wtst.getSourceX(xp[j]); // -x_offset);
                coordArr[j].y = wtst.getSourceY(yp[j]); // -y_offset);
            }
            setPathToPolyline(xp, yp);
            syncGeometry();
            resetInfoNodePosition();
        } catch (NullPointerException npe) {
            log.warn("error at moveFeature:", npe);     // NOI18N
        }
    }

    /**
     * Sets the offset of the stickychild to the interiorpoint of this PFeature.
     */
    public void resetInfoNodePosition() {
        if (stickyChild != null) {
            final Geometry geom = CrsTransformer.transformToCurrentCrs(getFeature().getGeometry());
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("getFeature().getGeometry():" + geom);                  // NOI18N
                }
            }
            if (viewer.isFeatureDebugging()) {
                if (log.isDebugEnabled()) {
                    log.debug("getFeature().getGeometry().getInteriorPoint().getY():" // NOI18N
                                + geom.getInteriorPoint().getY());
                }
            }
            stickyChild.setOffset(wtst.getScreenX(geom.getInteriorPoint().getX()),
                wtst.getScreenY(geom.getInteriorPoint().getY()));
        }
    }

    /**
     * Renews the InfoNode by deleting the old and creating a new one.
     */
    public void refreshInfoNode() {
        if ((stickyChild == infoNode) && (infoNode != null)) {
            stickyChild = null;
            removeChild(infoNode);
        } else if ((stickyChild != null) && (infoNode != null)) {
            stickyChild.removeChild(infoNode);
        }
        addInfoNode();
    }

    /**
     * Calls refreshInfoNode() in the EDT.
     */
    @Override
    public void refresh() {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (log.isDebugEnabled()) {
                        log.debug("refreshInfoNode"); // NOI18N
                    }
                    PFeature.this.refreshInfoNode();
                }
            });
    }

    /**
     * Creates an InfoPanel which is located in a PSwingComponent. This component will be added as child of this
     * PFeature. The InfoPanel contains the featuretype as icon and the name of the PFeature.
     */
    public void addInfoNode() {
        try {
            if (getFeature() instanceof XStyledFeature) {
                final XStyledFeature xsf = (XStyledFeature)getFeature();

                if (infoComponent == null) {
                    infoComponent = xsf.getInfoComponent(this);
                }

                if (viewer.isFeatureDebugging()) {
                    if (log.isDebugEnabled()) {
                        log.debug("ADD INFONODE3"); // NOI18N
                    }
                }
                if (infoPanel != null) {
                    viewer.getSwingWrapper().remove(infoPanel);
                }

                infoPanel = new InfoPanel(infoComponent);
                infoPanel.setPfeature(this);
                infoPanel.setTitleText(xsf.getName());
                infoPanel.setTitleIcon(xsf.getIconImage());

                pswingComp = new PSwing(viewer, infoPanel);
                pswingComp.resetBounds();
                pswingComp.setOffset(0, 0);

//            PText pt=new PText(xsf.getName());
//            if (getFeature().isEditable()) {
//                pt.setTextPaint(new Color(255,0,0));
//            } else {
//                pt.setTextPaint(new Color(0,0,0));
//            }
//            int width=(int)(pt.getWidth()+pi.getWidth());
//            int height=(int)(pi.getHeight());

                // Dieser node wird gebraucht damit die Mouseover sachen funktionieren. Geht nicht mit einem PSwing.
                // Auch nicht wenn das PSwing Element ParentNodeIsAPFeature & PSticky implementieren
                final StickyPPath p = new StickyPPath(new Rectangle(0, 0, 1, 1));
                p.setStroke(null);
                p.setPaint(new Color(250, 0, 0, 0)); // letzer Wert Wert Alpha: Wenn 0 dann unsichtbar
                p.setStrokePaint(null);
                infoPanel.setPNodeParent(p);
                infoPanel.setPSwing(pswingComp);

                p.addChild(pswingComp);
                pswingComp.setOffset(0, 0);

                if (stickyChild != null) {
                    stickyChild.addChild(p);
                    p.setOffset(stickyChild.getWidth(), 0);
                } else {
                    syncGeometry();
                    final Geometry geom = CrsTransformer.transformToCurrentCrs(getFeature().getGeometry());
                    p.setOffset(wtst.getScreenX(geom.getInteriorPoint().getX()),
                        wtst.getScreenY(geom.getInteriorPoint().getY()));
                    addChild(p);
                    p.setWidth(pswingComp.getWidth());
                    p.setHeight(pswingComp.getHeight());
                    stickyChild = p;
                    if (!ignoreStickyFeature) {
                        viewer.addStickyNode(p);
                        viewer.rescaleStickyNodes();
                    }
                    if (viewer.isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("addInfoNode()"); // NOI18N
                        }
                    }
                }
                infoNode = p;
                if (viewer != null) {
                    infoNode.setVisible(viewer.isInfoNodesVisible());
                    if (viewer.isFeatureDebugging()) {
                        if (log.isDebugEnabled()) {
                            log.debug("addInfoNode()"); // NOI18N
                        }
                    }
                    viewer.rescaleStickyNodes();
                    p.setWidth(pswingComp.getWidth());
                    p.setHeight(pswingComp.getHeight());
                } else {
                    infoNode.setVisible(false);
                }
                pswingComp.addPropertyChangeListener("fullBounds", new PropertyChangeListener() { // NOI18N

                        @Override
                        public void propertyChange(final PropertyChangeEvent evt) {
                            p.setWidth(pswingComp.getWidth());
                            p.setHeight(pswingComp.getHeight());
                        }
                    });
            }
        } catch (Throwable t) {
            log.error("Error in AddInfoNode", t); // NOI18N
        }
    }

    /**
     * Deletes the InfoPanel and hides the PFeature.
     */
    public void cleanup() {
        if (infoPanel != null) {
            infoPanel.setVisible(false);
            viewer.getSwingWrapper().remove(infoPanel);
        }
        this.setVisible(false);
    }

    /**
     * DOCUMENT ME!
     */
    public void ensureFullVisibility() {
        final PBounds all = viewer.getCamera().getViewBounds();
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("getViewBounds()" + all);             // NOI18N
            }
        }
        final PBounds newBounds = new PBounds();
        newBounds.setRect(this.getFullBounds().createUnion(all.getBounds2D()));
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("getFullBounds()" + getFullBounds()); // NOI18N
            }
        }
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("newBounds" + newBounds);             // NOI18N
            }
        }
        viewer.getCamera().animateViewToCenterBounds(newBounds.getBounds2D(), true, viewer.getAnimationDuration());
        viewer.refresh();
    }

    /**
     * DOCUMENT ME!
     *
     * @return    DOCUMENT ME!
     *
     * @Override  public boolean equals(Object obj) { try { return
     *            this.getFeature().equals(((PFeature)obj).getFeature()); } catch(Throwable t) { return false; } }
     */
    public boolean isInfoNodeExpanded() {
        return (infoPanel != null) && infoPanel.isExpanded();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  expanded  DOCUMENT ME!
     */
    public void setInfoNodeExpanded(final boolean expanded) {
        if (infoPanel != null) {
            infoPanel.setExpanded(expanded, false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedOriginal  DOCUMENT ME!
     */
    public void setSelectedOriginal(final PFeature selectedOriginal) {
        this.selectedOriginal = selectedOriginal;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSelectedOriginal() {
        return selectedOriginal;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void setFeature(final Feature feature) {
        this.feature = feature;
    }

    /**
     * Erstellt einen PPath zu diesem PFeature mittels der PCanvas-Arrays Xp und Yp.
     *
     * @return  PPath
     */
    public PPath getPurePPath() {
        final PPath pp = new PPath();
        pp.setPathToPolyline(xp, yp);
        // pp.setStroke(new FixedWidthStroke());
// pp.setStroke(new BasicStroke());
        return pp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PPath getSplitLine() {
        return splitPolygonLine;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Point2D> getSplitPoints() {
        return splitPoints;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSplittable() {
        if ((splitPolygonFromHandle != null) && (splitPolygonToHandle != null)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Zeichnet das PFeature bei einem RollOver um 40% heller.
     *
     * @param  highlighting  true, wenn das PFeature hervorgehoben werden soll
     */
    @Override
    public void setHighlighting(final boolean highlighting) {
        final boolean highlightingEnabledIfStyledFeature = ((getFeature() != null)
                        && !(getFeature() instanceof StyledFeature))
                    || ((getFeature() != null) && ((StyledFeature)getFeature()).isHighlightingEnabled());
        if (!isSelected() && (getPaint() != null) && highlightingEnabledIfStyledFeature) {
            highlighted = highlighting;
            if (highlighted) {
                nonHighlightingPaint = getPaint();
                if (nonHighlightingPaint instanceof Color) {
                    final Color c = (Color)nonHighlightingPaint;
                    int red = (int)(c.getRed() + 70);
                    int green = (int)(c.getGreen() + 70);
                    int blue = (int)(c.getBlue() + 70);
                    if (red > 255) {
                        red = 255;
                    }
                    if (green > 255) {
                        green = 255;
                    }
                    if (blue > 255) {
                        blue = 255;
                    }
                    setPaint(new Color(red, green, blue, c.getAlpha()));
                } else {
                    setPaint(new Color(1f, 1f, 1f, 0.6f));
                }
            } else {
                setPaint(nonHighlightingPaint);
            }
            repaint();
        }
    }

    /**
     * Liefert ein boolean, ob das Pfeature gerade hervorgehoben wird.
     *
     * @return  true, falls hervorgehoben
     */
    @Override
    public boolean getHighlighting() {
        return highlighted;
    }

    /**
     * Selektiert das PFeature je nach \u00FCbergebenem boolean-Wert.
     *
     * @param  selected  true, markiert. false, nicht markiert
     */
    @Override
    public void setSelected(final boolean selected) {
        if (viewer.isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("setSelected(" + selected + ")"); // NOI18N
            }
        }

        this.selected = selected;

        boolean showSelected = true;
        if (getFeature() instanceof DrawSelectionFeature) {
            showSelected = (((DrawSelectionFeature)getFeature()).isDrawingSelection());
        }
        if (showSelected) {
            showSelected(selected);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selected  DOCUMENT ME!
     */
    private void showSelected(final boolean selected) {
        splitPolygonFromHandle = null;
        splitPolygonToHandle = null;
        if (this.selected && !selected) {
            pivotHandle = null;
        }
        this.selected = selected;

        // PUNKT
        if (getFeature().getGeometry() instanceof Point) {
            PImage p = null;
            for (final ListIterator lit = getChildrenIterator(); lit.hasNext();) {
                final Object elem = (Object)lit.next();
                if (elem instanceof PImage) {
                    p = (PImage)elem;
                    break;
                }
            }
            if (p != null) {
                Image iconImage = null;
                if ((feature instanceof StyledFeature)
                            && (((StyledFeature)getFeature()).getPointAnnotationSymbol() != null)) {
                    final FeatureAnnotationSymbol symbolization = ((StyledFeature)getFeature())
                                .getPointAnnotationSymbol();
                    // assign pure unselected image
                    iconImage = symbolization.getImage();
                    final Image selectedImage = symbolization.getSelectedFeatureAnnotationSymbol();
                    if (selectedImage != null) {
                        if (selected) {
                            // assign pure selected image
                            iconImage = selectedImage;
                        }
                    } else if (iconImage != null) {
                        final Image old = iconImage;
                        final int inset = 10;
                        if (selected) {
                            // assign unselected image with selection frame
                            iconImage = highlightImageAsSelected(
                                    iconImage,
                                    new Color(0.3f, 0.3f, 1.0f, 0.4f),
                                    new Color(0.2f, 0.2f, 1.0f, 0.8f),
                                    inset);
                        } else {
                            // assign unselected image with invisible offset with size of the selection frame
                            iconImage = highlightImageAsSelected(iconImage, TRANSPARENT, TRANSPARENT, inset);
                        }
                        // adjust sweetspot if necessary
                        if (sweetSelX < 0f) {
                            sweetSelX = ((sweetPureX * old.getWidth(null)) + inset) / iconImage.getWidth(null);
                            sweetSelY = ((sweetPureY * old.getHeight(null)) + inset) / iconImage.getHeight(null);
                        }
                        pi.setSweetSpotX(sweetSelX);
                        pi.setSweetSpotY(sweetSelY);
                    }
                }
                // Fallback case: Pushpin icons
                if (iconImage == null) {
                    if (selected) {
                        iconImage = pushpinSelectedIco.getImage();
                    } else {
                        iconImage = pushpinIco.getImage();
                    }
                }
                p.setImage(iconImage);
                // Necessary "evil" to refresh sweetspot
                p.setScale(p.getScale());
            }
        }                                                                                                  // LINESTRING
        else if ((feature.getGeometry() instanceof LineString) || (feature.getGeometry() instanceof MultiLineString)) {
            if (selected) {
                final CustomFixedWidthStroke fws = new CustomFixedWidthStroke(5f);
                setStroke(fws);
                setStrokePaint(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground")); // NOI18N
                setPaint(null);
            } else {
                // setStroke(new FixedWidthStroke());
                if (stroke != null) {
                    setStroke(stroke);
                } else {
                    setStroke(FIXED_WIDTH_STROKE);
                }
                if (strokePaint != null) {
                    setStrokePaint(strokePaint);
                } else {
                    setStrokePaint(Color.black);
                }
            }
        } // POLYGON
        else {
            if (stroke != null) {
                setStroke(stroke);
            } else {
                setStroke(FIXED_WIDTH_STROKE);
            }

            if (selected) {
                nonSelectedPaint = getPaint();
                if (nonSelectedPaint instanceof Color) {
                    final Color c = (Color)nonHighlightingPaint;
                    if (c != null) {
                        final int red = (int)(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground")
                                        .getRed());                           // NOI18N
                        final int green = (int)(javax.swing.UIManager.getDefaults().getColor(
                                    "Table.selectionBackground").getGreen()); // NOI18N
                        final int blue = (int)(javax.swing.UIManager.getDefaults().getColor(
                                    "Table.selectionBackground").getBlue());  // NOI18N
                        setPaint(new Color(red, green, blue, c.getAlpha() / 2));
                    }
                } else {
                    setPaint(new Color(172, 210, 248, 178));
                }
            } else {
                setPaint(nonHighlightingPaint);
            }
        }
        repaint();
    }

    @Override
    public void setStroke(final Stroke s) {
        // log.debug("setStroke: " + s, new CurrentStackTrace());
        super.setStroke(s);
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   toSelect   DOCUMENT ME!
     * @param   colFill    DOCUMENT ME!
     * @param   colEdge    DOCUMENT ME!
     * @param   insetSize  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Image highlightImageAsSelected(final Image toSelect, Color colFill, Color colEdge, final int insetSize) {
        if (colFill == null) {
            colFill = TRANSPARENT;
        }
        if (colEdge == null) {
            colEdge = TRANSPARENT;
        }
        if (toSelect != null) {
            final int doubleInset = 2 * insetSize;
            final BufferedImage tint = new BufferedImage(toSelect.getWidth(null) + doubleInset,
                    toSelect.getHeight(null)
                            + doubleInset,
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g2d = (Graphics2D)tint.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setPaint(colFill);
            g2d.fillRoundRect(
                0,
                0,
                toSelect.getWidth(null)
                        - 1
                        + doubleInset,
                toSelect.getHeight(null)
                        - 1
                        + doubleInset,
                insetSize,
                insetSize);
            g2d.setPaint(colEdge);
            g2d.drawRoundRect(
                0,
                0,
                toSelect.getWidth(null)
                        - 1
                        + doubleInset,
                toSelect.getHeight(null)
                        - 1
                        + doubleInset,
                insetSize,
                insetSize);
            g2d.drawImage(toSelect, insetSize, insetSize, null);
            return tint;
        } else {
            return toSelect;
        }
    }

    /**
     * Ver\u00E4ndert die Sichtbarkeit der InfoNode.
     *
     * @param  visible  true, wenn die InfoNode sichtbar sein soll
     */
    public void setInfoNodeVisible(final boolean visible) {
        if (infoNode != null) {
            infoNode.setVisible(visible);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getViewer() {
        return viewer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  viewer  DOCUMENT ME!
     */
    public void setViewer(final MappingComponent viewer) {
        this.viewer = viewer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return viewer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Paint getNonSelectedPaint() {
        return nonSelectedPaint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nonSelectedPaint  DOCUMENT ME!
     */
    public void setNonSelectedPaint(final Paint nonSelectedPaint) {
        this.nonSelectedPaint = nonSelectedPaint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Paint getNonHighlightingPaint() {
        return nonHighlightingPaint;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nonHighlightingPaint  DOCUMENT ME!
     */
    public void setNonHighlightingPaint(final Paint nonHighlightingPaint) {
        this.nonHighlightingPaint = nonHighlightingPaint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float[] getXp() {
        return xp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  xp  DOCUMENT ME!
     */
    public void setXp(final float[] xp) {
        this.xp = xp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float[] getYp() {
        return yp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  yp  DOCUMENT ME!
     */
    public void setYp(final float[] yp) {
        this.yp = yp;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PNode getInfoNode() {
        return infoNode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordArr  DOCUMENT ME!
     */
    public void setCoordArr(final Coordinate[] coordArr) {
        this.coordArr = coordArr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PNode getStickyChild() {
        return stickyChild;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasSecondStickyChild() {
        return (secondStickyChild != null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PNode getSecondStickyChild() {
        return secondStickyChild;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate[] getCoordArr() {
        return coordArr;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class StickyPPath extends PPath implements ParentNodeIsAPFeature, PSticky {

        //~ Instance fields ----------------------------------------------------

        int transparency = 0;
        Color c = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new StickyPPath object.
         *
         * @param  s  DOCUMENT ME!
         */
        public StickyPPath(final Shape s) {
            super(s);
        }
    }

    /**
     * StickyPText represents the annotation of a PFeature.
     *
     * @version  $Revision$, $Date$
     */
    class StickyPText extends PText implements ParentNodeIsAPFeature, PSticky {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new StickyPText object.
         */
        public StickyPText() {
            super();
        }

        /**
         * Creates a new StickyPText object.
         *
         * @param  text  DOCUMENT ME!
         */
        public StickyPText(final String text) {
            super(text);
        }
    }
}
