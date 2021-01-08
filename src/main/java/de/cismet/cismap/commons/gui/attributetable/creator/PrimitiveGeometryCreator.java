/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.attributetable.creator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Cursor;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import static de.cismet.cismap.commons.gui.attributetable.FeatureCreator.SIMPLE_GEOMETRY_LISTENER_KEY;
import static de.cismet.cismap.commons.gui.attributetable.creator.AbstractFeatureCreator.fillFeatureWithDefaultValues;

/**
 * Creates new features, which use primitive geometry types.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PrimitiveGeometryCreator extends AbstractFeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PrimitiveGeometryCreator.class);

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureCreatedListener> listener = new ArrayList<FeatureCreatedListener>();

    private final String mode;
    private boolean multi;
    private CreaterGeometryListener geometryListener = null;
    private AbstractFeatureService service = null;
    private PNode tmpFeature;
    private MappingComponent mc = null;
    private boolean activateResume = false;
    private double minArea;
    private double minLength;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrimitiveGeometryCreator object.
     *
     * @param  mode  DOCUMENT ME!
     */
    public PrimitiveGeometryCreator(final String mode) {
        this(mode, null);
    }

    /**
     * Creates a new PrimitiveGeometryCreator object.
     *
     * @param  mode        DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     */
    public PrimitiveGeometryCreator(final String mode, final Map<String, Object> properties) {
        this(mode, properties, false);
    }

    /**
     * Creates a new PrimitiveGeometryCreator object.
     *
     * @param  mode   DOCUMENT ME!
     * @param  multi  DOCUMENT ME!
     */
    public PrimitiveGeometryCreator(final String mode, final boolean multi) {
        this(mode, null, multi);
    }

    /**
     * Creates a new PrimitiveGeometryCreator object.
     *
     * @param  mode        DOCUMENT ME!
     * @param  properties  DOCUMENT ME!
     * @param  multi       DOCUMENT ME!
     */
    public PrimitiveGeometryCreator(final String mode, final Map<String, Object> properties, final boolean multi) {
        this.mode = mode;
        this.properties = properties;
        this.multi = multi;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the minArea
     */
    public double getMinArea() {
        return minArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  minArea  the minArea to set
     */
    public void setMinArea(final double minArea) {
        this.minArea = minArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the minLength
     */
    public double getMinLength() {
        return minLength;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  minLength  the minLength to set
     */
    public void setMinLength(final double minLength) {
        this.minLength = minLength;
    }

    @Override
    public void createFeature(final MappingComponent mc,
            final FeatureServiceFeature feature) {
        this.mc = mc;
        if ((feature != null) && (feature.getLayerProperties() != null)) {
            service = feature.getLayerProperties().getFeatureService();
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final String oldInteractionMode = mc.getInteractionMode();

                    final CreaterGeometryListener listener = new CreaterGeometryListener(
                            mc,
                            new GeometryFinishedListener() {

                                @Override
                                public void geometryFinished(final Geometry g) {
                                    final WaitingDialogThread wdt = new WaitingDialogThread(
                                            StaticSwingTools.getParentFrame(mc),
                                            true,
                                            "Erstelle Objekt",
                                            null,
                                            1000) {

                                            @Override
                                            protected Object doInBackground() throws Exception {
                                                Geometry geom = g;

                                                if (mode.equals(CreateGeometryListenerInterface.LINESTRING)
                                                            && geom.getGeometryType().equals("Point")) {
                                                    geom = g.getFactory()
                                                                    .createLineString(
                                                                            new Coordinate[] {
                                                                                g.getCoordinate(),
                                                                                new Coordinate(
                                                                                    g.getCoordinate().x
                                                                                    + 1,
                                                                                    g.getCoordinate().y
                                                                                    + 1)
                                                                            });
                                                }

                                                if (multi) {
                                                    geom = StaticGeometryFunctions.toMultiGeometry(geom);
                                                } else {
                                                    geom = StaticGeometryFunctions.toSimpleGeometry(geom);
                                                }

                                                if ((minArea != 0.0)
                                                            && mode.equals(CreateGeometryListenerInterface.POLYGON)
                                                            && (geom.getArea() < minArea)) {
                                                    JOptionPane.showMessageDialog(
                                                        CismapBroker.getInstance().getMappingComponent(),
                                                        NbBundle.getMessage(
                                                            PrimitiveGeometryCreator.class,
                                                            "PrimitiveGeometryCreator.createFeature().tooSmall.message",
                                                            new Object[] { minArea }),
                                                        NbBundle.getMessage(
                                                            PrimitiveGeometryCreator.class,
                                                            "PrimitiveGeometryCreator.createFeature().tooSmall.title"),
                                                        JOptionPane.WARNING_MESSAGE);
                                                    return null;
                                                }
                                                if ((minLength != 0.0)
                                                            && mode.equals(CreateGeometryListenerInterface.LINESTRING)
                                                            && (geom.getLength() < minLength)) {
                                                    JOptionPane.showMessageDialog(
                                                        CismapBroker.getInstance().getMappingComponent(),
                                                        NbBundle.getMessage(
                                                            PrimitiveGeometryCreator.class,
                                                            "PrimitiveGeometryCreator.createFeature().tooShort.message",
                                                            new Object[] { minLength }),
                                                        NbBundle.getMessage(
                                                            PrimitiveGeometryCreator.class,
                                                            "PrimitiveGeometryCreator.createFeature().tooShort.title"),
                                                        JOptionPane.WARNING_MESSAGE);
                                                    return null;
                                                }
                                                feature.setGeometry(geom);
                                                // mc.setInteractionMode(oldInteractionMode);

                                                if (feature instanceof DefaultFeatureServiceFeature) {
                                                    try {
                                                        fillFeatureWithDefaultValues(
                                                            (DefaultFeatureServiceFeature)feature,
                                                            properties);
                                                        ((DefaultFeatureServiceFeature)feature).saveChanges();
                                                        fillFeatureWithDefaultValuesAfterSave(
                                                            (DefaultFeatureServiceFeature)feature,
                                                            properties);

                                                        for (final FeatureCreatedListener featureCreatedListener
                                                                    : PrimitiveGeometryCreator.this.listener) {
                                                            featureCreatedListener.featureCreated(
                                                                new FeatureCreatedEvent(
                                                                    PrimitiveGeometryCreator.this,
                                                                    feature));
                                                        }
                                                    } catch (Exception e) {
                                                        LOG.error("Cannot save new feature", e);
                                                    }
                                                }

                                                return null;
                                            }
                                        };

                                    wdt.start();
                                }
                            });

                    if ((CismapBroker.getInstance() != null)
                                && (CismapBroker.getInstance().getMappingComponent() != null)) {
                        final CreateNewGeometryListener result = (CreateNewGeometryListener)CismapBroker
                                    .getInstance().getMappingComponent().getInputListener(MappingComponent.NEW_POLYGON);
                        listener.setShowCurrentLength(result.isShowCurrentLength());
                    }
                    geometryListener = listener;
                    mc.addInputListener(SIMPLE_GEOMETRY_LISTENER_KEY, listener);
                    mc.putCursor(SIMPLE_GEOMETRY_LISTENER_KEY, new Cursor(Cursor.CROSSHAIR_CURSOR));
                    listener.setMode(mode);
                    mc.setInteractionMode(SIMPLE_GEOMETRY_LISTENER_KEY);
                }
            });
    }

    @Override
    public void addFeatureCreatedListener(final FeatureCreatedListener listener) {
        this.listener.add(listener);
    }

    @Override
    public String getTypeName() {
        if (mode.equals(CreateGeometryListenerInterface.LINESTRING)) {
            return NbBundle.getMessage(
                    PrimitiveGeometryCreator.class,
                    "PrimitiveGeometryCreator.getTypeName().linestring");
        } else if (mode.equals(CreateGeometryListenerInterface.POLYGON)) {
            return NbBundle.getMessage(
                    PrimitiveGeometryCreator.class,
                    "PrimitiveGeometryCreator.getTypeName().polygon");
        } else if (mode.equals(CreateGeometryListenerInterface.POINT)) {
            return NbBundle.getMessage(PrimitiveGeometryCreator.class, "PrimitiveGeometryCreator.getTypeName().point");
        } else if (mode.equals(CreateGeometryListenerInterface.RECTANGLE)) {
            return NbBundle.getMessage(
                    PrimitiveGeometryCreator.class,
                    "PrimitiveGeometryCreator.getTypeName().rectangle");
        } else if (mode.equals(CreateGeometryListenerInterface.ELLIPSE)) {
            return NbBundle.getMessage(
                    PrimitiveGeometryCreator.class,
                    "PrimitiveGeometryCreator.getTypeName().ellipse");
        } else {
            return NbBundle.getMessage(PrimitiveGeometryCreator.class, "PrimitiveGeometryCreator.getTypeName().other");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the multi
     */
    public boolean isMulti() {
        return multi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  multi  the multi to set
     */
    public void setMulti(final boolean multi) {
        this.multi = multi;
    }

    @Override
    public void cancel() {
//        if ((mc != null) && !activateResume) {
//            if (mc.getTmpFeatureLayer().getChildrenCount() == 1) {
//                tmpFeature = mc.getTmpFeatureLayer().getChild(0);
//            }
//            mc.getTmpFeatureLayer().removeAllChildren();
//        }
    }

    @Override
    public void resume() {
        CismapBroker.getInstance().getMappingComponent().setInteractionMode(SIMPLE_GEOMETRY_LISTENER_KEY);
        if (mc != null) {
            final CreateNewGeometryListener result = (CreateNewGeometryListener)mc.getInputListener(
                    MappingComponent.NEW_POLYGON);

            if (geometryListener != null) {
                geometryListener.setShowCurrentLength(result.isShowCurrentLength());
            }
        }
    }

    @Override
    public AbstractFeatureService getService() {
        return service;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMode() {
        return mode;
    }
}
