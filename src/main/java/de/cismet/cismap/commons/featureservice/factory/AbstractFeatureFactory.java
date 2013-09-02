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
package de.cismet.cismap.commons.featureservice.factory;

import com.vividsolutions.jts.geom.Geometry;

import groovy.lang.GroovyShell;

import org.apache.log4j.Logger;

import org.deegree.style.se.unevaluated.Style;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.*;

/**
 * Abstract impelementation of a FeatureFactory. Supports re-evaluation of id and annotation expressions.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public abstract class AbstractFeatureFactory<FT extends FeatureServiceFeature, QT> implements FeatureFactory<FT, QT> {

    //~ Static fields/initializers ---------------------------------------------

    public static final boolean DEBUG = Debug.DEBUG;

    //~ Instance fields --------------------------------------------------------

    public String layerName = null;

    // -1 = not id available
    protected int ID = -1;
    protected Logger logger = Logger.getLogger(this.getClass());
    protected LayerProperties layerProperties;
    protected int maxFeatureCount = -1;
    protected GroovyShell groovyShell = null;
    // protected boolean idExpressionChanged = true;
    // protected boolean primaryAnnotationExpressionChanged = true;
    // protected boolean secondaryAnnotationExpressionChanged = true;
    protected Vector<FT> lastCreatedfeatureVector = new Vector();
    private volatile boolean isInterruptedAllowed = true;
    private Geometry lastGeom = null;
    private QT lastQuery;
    // private BoundingBox lastBB = null;
    // private BoundingBox diff = null;
    // private final WKTReader reader;
    protected Map<String, LinkedList<Style>> styles;
    private volatile boolean isInterruptedAllowed = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractFeatureFactory object.
     */
    protected AbstractFeatureFactory() {
    }

    /**
     * Creates a new AbstractFeatureFactory object.
     *
     * @param  aff  DOCUMENT ME!
     */
    protected AbstractFeatureFactory(final AbstractFeatureFactory aff) {
        this.ID = aff.ID;
        this.layerProperties = aff.layerProperties.clone();
        this.maxFeatureCount = aff.maxFeatureCount;
        this.lastCreatedfeatureVector = new Vector(aff.lastCreatedfeatureVector.size());
        this.lastCreatedfeatureVector.addAll(lastCreatedfeatureVector);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public synchronized void waitUntilInterruptedIsAllowed() {
        try {
            if (!isInterruptedAllowed) {
                wait();
            }
        } catch (InterruptedException e) {
            logger.error("should never happen");
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected synchronized void setInterruptedAllowed() {
        isInterruptedAllowed = true;
        notifyAll();
    }

    /**
     * DOCUMENT ME!
     */
    protected synchronized void setInterruptedNotAllowed() {
        isInterruptedAllowed = false;
    }

    
    @Override
    public void setLayerName(final String layerName) {
        this.layerName = layerName;
    }

    @Override
    public void setSLDStyle(final Map<String, LinkedList<Style>> styles) {
        this.styles = styles;
        for (final FT feature : lastCreatedfeatureVector) {
            feature.setSLDStyles(getStyle(layerName));
        }
    }

    @Override
    public void setLayerName(final String layerName) {
        this.layerName = layerName;
    }

    @Override
    public void setSLDStyle(final Map<String, LinkedList<Style>> styles) {
        this.styles = styles;
        for (final FT feature : lastCreatedfeatureVector) {
            feature.setSLDStyles(getStyle(layerName));
        }
    }

    @Override
    public void setSLDStyle(final Map<String, LinkedList<Style>> styles) {
        this.styles = styles;
        for(FT feature : lastCreatedfeatureVector) {
            feature.setSLDStyle(getStyle());
        }
    }

    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        final LayerProperties oldLayerProperties = this.layerProperties;
        this.layerProperties = layerProperties;

        if (this.isGenerateIds() && this.layerProperties.isIdExpressionEnabled()) {
            logger.warn(
                "factory supports automatic id generation, disabling id expression support in layer properties");
            this.layerProperties.setIdExpressionEnabled(false);
        }

        if (this.lastCreatedfeatureVector.size() > 0) {
            final long start = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug(this.lastCreatedfeatureVector.size()
                            + " last created features found, applying updated expressions if applicable");
            }
            // check if at least one expression changed
            if (((oldLayerProperties.getIdExpression() == null)
                            || oldLayerProperties.getIdExpression().equals(this.layerProperties.getIdExpression()))
                        && ((oldLayerProperties.getPrimaryAnnotationExpression() == null)
                            || oldLayerProperties.getPrimaryAnnotationExpression().equals(
                                this.layerProperties.getPrimaryAnnotationExpression()))
                        && ((oldLayerProperties.getSecondaryAnnotationExpression() == null)
                            || oldLayerProperties.getSecondaryAnnotationExpression().equals(
                                this.layerProperties.getSecondaryAnnotationExpression()))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("expressions did not change, re-elevation not neccessary");
                }
                for (final FT feature : this.lastCreatedfeatureVector) {
                    feature.setLayerProperties(this.layerProperties);
                }
            } else if ((this.layerProperties.getIdExpressionType() == LayerProperties.EXPRESSIONTYPE_UNDEFINED)
                        && (this.layerProperties.getPrimaryAnnotationExpressionType()
                            == LayerProperties.EXPRESSIONTYPE_UNDEFINED)
                        && (this.layerProperties.getSecondaryAnnotationExpressionType()
                            == LayerProperties.EXPRESSIONTYPE_UNDEFINED)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("re-evaluation not necessary, no supported expressions");
                }
                for (final FT feature : this.lastCreatedfeatureVector) {
                    feature.setLayerProperties(this.layerProperties);
                }
            } else {
                this.reEvaluteExpressions(this.lastCreatedfeatureVector, null);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("updating layer properties of " + this.lastCreatedfeatureVector.size() + " features took "
                            + (System.currentTimeMillis() - start) + " ms");
            }
        } else {
            logger.warn("no last created features that could be refreshed found");
        }
    }

    @Override
    public LayerProperties getLayerProperties() {
        return this.layerProperties;
    }

    @Override
    public int getMaxFeatureCount() {
        return this.maxFeatureCount;
    }

    @Override
    public void setMaxFeatureCount(final int maxFeatureCount) {
        this.maxFeatureCount = maxFeatureCount;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureList  DOCUMENT ME!
     * @param  attributes   DOCUMENT ME!
     */
    protected void sortFeatureList(final List<? extends FeatureServiceFeature> featureList,
            final FeatureServiceAttribute[] attributes) {
        Collections.sort(featureList, new Comparator<FeatureServiceFeature>() {

                @Override
                public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
                    for (final FeatureServiceAttribute attribute : attributes) {
                        final Object att1 = o1.getProperty(attribute.getName());
                        final Object att2 = o2.getProperty(attribute.getName());

                        if ((att1 instanceof Comparable) && (att2 instanceof Comparable)) {
                            final Comparable c1 = (Comparable)att1;
                            final Comparable c2 = (Comparable)att2;

                            final int result = c1.compareTo(c2);

                            if (result != 0) {
                                return result;
                            }
                        }
                    }

                    return 0;
                }
            });
    }

    /**
     * Re-evaluates the expressions of all features in the list. This operation may be called, if new layer properties
     * are applied on cached features.
     *
     * @param  features      list of caches features
     * @param  workerThread  DOCUMENT ME!
     */
    protected void reEvaluteExpressions(final List<FT> features, final SwingWorker workerThread) {
        if (logger.isDebugEnabled()) {
            logger.debug("SW[" + workerThread + "]: performing re-evaluation of the expressions of " + features.size()
                        + " selected features");
        }
        final long start = System.currentTimeMillis();
        int i = 0;
        for (final FT feature : features) {
            // check if thread is canceled .........................................
            if (this.checkCancelled(workerThread, " evaluating expression")) {
                return;
            }
            // check if thread is canceled .........................................

            feature.setLayerProperties(this.layerProperties);
            this.evaluateExpressions(feature, i);
            i++;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("SW[" + workerThread + "]: re-evaluation of " + features.size() + " features took "
                        + (System.currentTimeMillis() - start) + " ms");
        }
    }

    /**
     * Evaluates id an annotation expressions of the current layer properties and applies it to the feature.
     *
     * @param  feature  to on that the expressions are applied
     * @param  index    is used as if the evaluation of the id expression fails
     */
    protected void evaluateExpressions(final FT feature, final int index) {
        Object property = null;
        String id = null;

        // ID Expression ...........................................................
        if (!this.isGenerateIds()) {
            if (DEBUG) {
                if (logger.isDebugEnabled()) {
                    logger.debug("evaluating idExpression '" + this.layerProperties.getIdExpression() + "' of type "
                                + this.layerProperties.getIdExpressionType());
                }
            }
            switch (this.layerProperties.getIdExpressionType()) {
                case LayerProperties.EXPRESSIONTYPE_PROPERTYNAME: {
                    if (DEBUG) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("evaluating idExpression: EXPRESSIONTYPE_PROPERTYNAME "
                                        + LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                        }
                    }
                    property = feature.getProperty(this.layerProperties.getIdExpression());
                    try {
                        if (property != null) {
                            if (DEBUG) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("evaluating idExpression: property '" + property + "'");
                                }
                            }
                            feature.setId(Integer.parseInt(property.toString()));
                        } else {
                            feature.setId(ID);
                            if (DEBUG) {
                                logger.warn("evaluating idExpression: property '"
                                            + this.layerProperties.getIdExpression() + "' not found, setting id to "
                                            + ID);
                            }
                        }
                    } catch (NumberFormatException nfe) {
                        feature.setId(ID);
                        if (DEBUG) {
                            logger.warn("evaluating idExpression: property '" + property.toString()
                                        + "' could not be converted to int, setting id to " + ID);
                        }
                    }
                    break;
                }

                case LayerProperties.EXPRESSIONTYPE_BEANSHELL: {
                    if (DEBUG) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("evaluating idExpression: EXPRESSIONTYPE_BEANSHELL "
                                        + LayerProperties.EXPRESSIONTYPE_BEANSHELL);
                        }
                    }
                    id = this.evaluateBeanShellExpression(feature, this.layerProperties.getIdExpression());
                    try {
                        if (id != null) {
                            feature.setId(Integer.parseInt(id.toString()));
                        } else {
                            feature.setId(ID);
                        }
                    } catch (NumberFormatException nfe) {
                        feature.setId(ID);
                    }
                    break;
                }

                case LayerProperties.EXPRESSIONTYPE_GROOVY: {
                    if (DEBUG) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("evaluating idExpression: EXPRESSIONTYPE_GROOVY "
                                        + LayerProperties.EXPRESSIONTYPE_GROOVY);
                        }
                    }
                    id = this.evaluateGroovyExpressions(feature, this.layerProperties.getIdExpression());
                    try {
                        if (id != null) {
                            feature.setId(Integer.parseInt(id.toString()));
                        } else {
                            feature.setId(ID);
                        }
                    } catch (NumberFormatException nfe) {
                        feature.setId(ID);
                    }
                    break;
                }

                default: {
                    feature.setId(ID);
                    break;
                }
            }
        }

        // PrimaryAnnotationExpression .............................................
        if (DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug("evaluating PrimaryAnnotationExpression '"
                            + this.layerProperties.getPrimaryAnnotationExpression() + "' of type "
                            + this.layerProperties.getPrimaryAnnotationExpressionType());
            }
        }
        switch (this.layerProperties.getPrimaryAnnotationExpressionType()) {
            case LayerProperties.EXPRESSIONTYPE_STATIC: {
                feature.setPrimaryAnnotation(this.layerProperties.getPrimaryAnnotationExpression());
                break;
            }

            case LayerProperties.EXPRESSIONTYPE_PROPERTYNAME: {
                property = feature.getProperty(this.layerProperties.getPrimaryAnnotationExpression());
                if (DEBUG) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("evaluating PrimaryAnnotationExpression: setting PrimaryAnnotationExpression '"
                                    + property + "'");
                    }
                }
                if (property != null) {
                    feature.setPrimaryAnnotation(property.toString());
                }
                break;
            }

            case LayerProperties.EXPRESSIONTYPE_BEANSHELL: {
                feature.setPrimaryAnnotation(this.evaluateBeanShellExpression(
                        feature,
                        this.layerProperties.getPrimaryAnnotationExpression()));
                break;
            }

            case LayerProperties.EXPRESSIONTYPE_GROOVY: {
                feature.setPrimaryAnnotation(this.evaluateGroovyExpressions(
                        feature,
                        this.layerProperties.getPrimaryAnnotationExpression()));
                break;
            }
        }

        // SecondaryAnnotationExpression ...........................................
        if (DEBUG) {
            if (logger.isDebugEnabled()) {
                logger.debug("evaluating SecondaryAnnotationExpression '"
                            + this.layerProperties.getSecondaryAnnotationExpression() + "' of type "
                            + this.layerProperties.getSecondaryAnnotationExpressionType());
            }
        }
        switch (this.layerProperties.getSecondaryAnnotationExpressionType()) {
            case LayerProperties.EXPRESSIONTYPE_STATIC: {
                feature.setSecondaryAnnotation(this.layerProperties.getSecondaryAnnotationExpression());
                break;
            }

            case LayerProperties.EXPRESSIONTYPE_PROPERTYNAME: {
                property = feature.getProperty(this.layerProperties.getSecondaryAnnotationExpression());
                if (DEBUG) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("evaluating PrimaryAnnotationExpression: setting SecondaryAnnotationExpression '"
                                    + property + "'");
                    }
                }
                if (property != null) {
                    feature.setSecondaryAnnotation(property.toString());
                }
                break;
            }

            case LayerProperties.EXPRESSIONTYPE_BEANSHELL: {
                feature.setSecondaryAnnotation(this.evaluateBeanShellExpression(
                        feature,
                        this.layerProperties.getSecondaryAnnotationExpression()));
                break;
            }

            case LayerProperties.EXPRESSIONTYPE_GROOVY: {
                feature.setSecondaryAnnotation(this.evaluateGroovyExpressions(
                        feature,
                        this.layerProperties.getSecondaryAnnotationExpression()));
                break;
            }
        }
    }

    /**
     * Evaluates a groovy expression.
     *
     * @param   feature     DOCUMENT ME!
     * @param   expression  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String evaluateGroovyExpressions(final FT feature, String expression) {
        if (this.groovyShell == null) {
            if (DEBUG) {
                if (logger.isDebugEnabled()) {
                    logger.debug("performing lazy first time initialisation of GroovyShell");
                }
            }
            this.groovyShell = new GroovyShell();
        }

        try {
            groovyShell.getContext().getVariables().clear();
            /** Groovy keeps references to former scripts. This circumstance leads to
             * a steady growing of permGen space and in the worst case to
             * OutOfMemoryException. The method resetLoadedClasses() removes this
             * references.
             */
            groovyShell.resetLoadedClasses();
            for (final Object key : feature.getProperties().keySet()) {
                final Object property = feature.getProperty(key.toString());
                groovyShell.setVariable(key.toString().replaceAll(":", "_"), property);
            }

            expression = expression.replaceAll(":", "_");
            return groovyShell.evaluate(expression).toString();
        } catch (Throwable t) {
            logger.error("could not evaluate groovy expression '" + expression + "'", t);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature     DOCUMENT ME!
     * @param   expression  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    protected String evaluateBeanShellExpression(final FT feature, final String expression) {
        throw new UnsupportedOperationException("BeanShell not supported");
    }

    /**
     * Checks if the worker thread was cancelled and performs cleanup.
     *
     * @param   workerThread  DOCUMENT ME!
     * @param   message       DOCUMENT ME!
     *
     * @return  true if the worker thread was cancelled
     */
    protected boolean checkCancelled(final SwingWorker workerThread, final String message) {
        if ((workerThread != null) && workerThread.isCancelled()) {
            logger.warn("FRW[" + workerThread + "]: operation is canceled after " + message);
            this.lastCreatedfeatureVector.clear();
            return true;
        }

        return false;
    }

    @Override
    public synchronized Vector<FT> getLastCreatedFeatures() {
        // return copy to prevent concurrent modification exception when thread is canceled
        return new Vector<FT>(this.lastCreatedfeatureVector);
    }

    /**
     * Determines if the service automatically generates unique IDs for all queryable features. Note that if this
     * operation returns {@code true}, the id expression is not evaluated.
     *
     * @return  {@code true} if the service generates id}
     */
    protected abstract boolean isGenerateIds();
//  protected void processFeatures() throws Exception
//  {
//    if (AbstractFeatureService.this instanceof StaticFeatureService)
//    {
//      Coordinate[] polyCords = new Coordinate[5];
//      polyCords[0] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY1());
//      polyCords[1] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY2());
//      polyCords[2] = new Coordinate(getBoundingBox().getX2(), getBoundingBox().getY2());
//      polyCords[3] = new Coordinate(getBoundingBox().getX2(), getBoundingBox().getY1());
//      polyCords[4] = new Coordinate(getBoundingBox().getX1(), getBoundingBox().getY1());
//      Polygon boundingPolygon = (new GeometryFactory()).createPolygon((new GeometryFactory()).createLinearRing(polyCords), null);
//
//      if (!(JTSAdapter.export(current.getDefaultGeometryPropertyValue())).intersects(boundingPolygon))
//      {
//        //if(DEBUG)logger.debug("Feature ist nicht in boundingbox");
//        continue;
//      }
//    }
//
//  }

    /**
     * DOCUMENT ME!
     *
     * @param  features  DOCUMENT ME!
     * @param  geom      DOCUMENT ME!
     * @param  query     DOCUMENT ME!
     */
    protected synchronized void updateLastCreatedFeatures(final Collection<FT> features,
            final Geometry geom,
            final QT query) {
        this.lastCreatedfeatureVector.clear();
        this.lastCreatedfeatureVector.ensureCapacity(features.size());
        this.lastCreatedfeatureVector.addAll(features);
        this.lastCreatedfeatureVector.trimToSize();
        this.lastGeom = geom;
        this.lastQuery = query;
    }

    @Override
    public FeatureServiceFeature createNewFeature() {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom   DOCUMENT ME!
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean featuresAlreadyInMemory(final Geometry geom, final QT query) {
        if (((lastQuery == null) && (query != null))
                    || ((lastQuery != null) && (query != null) && !lastQuery.equals(query))) {
            return false;
        } else {
            return (lastGeom != null) && (geom.getSRID() == lastGeom.getSRID()) && geom.within(lastGeom);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   query  DOCUMENT ME!
     * @param   geom   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    protected Vector<FT> createFeaturesFromMemory(final QT query,
            final Geometry geom) throws TooManyFeaturesException, Exception {
        if (!featuresAlreadyInMemory(geom, query)) {
            return null;
        }

        final Vector<FT> featureList = new Vector<FT>();

        for (final FT feature : lastCreatedfeatureVector) {
            if (geom.intersects(feature.getGeometry())) {
                featureList.add(feature);
            }
        }

        return featureList;
    }

    @Override
    public abstract AbstractFeatureFactory clone();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<Style> getStyle() {
        if (styles != null) {
            return styles.get("default");
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Style> getStyle(final String layerName) {
        if (layerName == null) {
            return getStyle();
        } else if ((styles != null) && styles.containsKey(layerName)) {
            return styles.get(layerName);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<Style> getStyle(final String layerName) {
        if (layerName == null) {
            return getStyle();
        } else if ((styles != null) && styles.containsKey(layerName)) {
            return styles.get(layerName);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<Style> getStyle(final String layerName) {
        if (layerName == null) {
            return getStyle();
        } else if ((styles != null) && styles.containsKey(layerName)) {
            return styles.get(layerName);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layerName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<Style> getStyle(final String layerName) {
        if (layerName == null) {
            return getStyle();
        } else if ((styles != null) && styles.containsKey(layerName)) {
            return styles.get(layerName);
        } else {
            return null;
        }
    }
}
