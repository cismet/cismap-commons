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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Cursor;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;

import static de.cismet.cismap.commons.gui.attributetable.FeatureCreator.SIMPLE_GEOMETRY_LISTENER_KEY;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PrimitiveGeometryCreator implements FeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PrimitiveGeometryCreator.class);

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureCreatedListener> listener = new ArrayList<FeatureCreatedListener>();

    private final String mode;
    private Map<String, Object> properties;

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
        this.mode = mode;
        this.properties = properties;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void createFeature(final MappingComponent mc,
            final FeatureServiceFeature feature) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final String oldInteractionMode = mc.getInteractionMode();

                    final CreateNewGeometryListener listener = new CreaterGeometryListener(
                            mc,
                            new GeometryFinishedListener() {

                                @Override
                                public void geometryFinished(final Geometry g) {
                                    feature.setGeometry(g);
                                    mc.setInteractionMode(oldInteractionMode);

                                    if (feature instanceof DefaultFeatureServiceFeature) {
                                        try {
                                            if (properties != null) {
                                                for (final String propName : properties.keySet()) {
                                                    final Object o = properties.get(propName);

                                                    if ((o instanceof String) && ((String)o).startsWith("@")) {
                                                        final String referencedProperty = ((String)o).substring(1);
                                                        final Object value = ((DefaultFeatureServiceFeature)feature)
                                                                        .getProperty(referencedProperty);
                                                        ((DefaultFeatureServiceFeature)feature).setProperty(
                                                            propName,
                                                            value);
                                                    } else {
                                                        ((DefaultFeatureServiceFeature)feature).setProperty(
                                                            propName,
                                                            o);
                                                    }
                                                }
                                            }
                                            ((DefaultFeatureServiceFeature)feature).saveChanges();

                                            for (final FeatureCreatedListener featureCreatedListener
                                                        : PrimitiveGeometryCreator.this.listener) {
                                                featureCreatedListener.featureCreated(
                                                    new FeatureCreatedEvent(PrimitiveGeometryCreator.this, feature));
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Cannot save new feature", e);
                                        }
                                    }
                                }
                            });
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
}
