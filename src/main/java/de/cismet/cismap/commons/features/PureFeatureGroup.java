/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Paint;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.tools.collections.TypeSafeCollections;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public class PureFeatureGroup implements FeatureGroup, StyledFeature {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String myAttributeStringInParentFeature = null;
    private FeatureGroup parentFeature = null;
    private final Set<Feature> groupFeatures;
    private final Collection<Feature> readOnlyGroupFeatures;
    private Geometry enclosingGeometry;
//    private boolean canBeSelected;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PureFeatureGroup object.
     */
    public PureFeatureGroup() {
        this(Collections.EMPTY_LIST);
    }

    /**
     * Creates a new PureFeatureGroup object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public PureFeatureGroup(final Feature feature) {
        this();
        groupFeatures.add(feature);
    }

    /**
     * Creates a new PureFeatureGroup object.
     *
     * @param  features  DOCUMENT ME!
     */
    public PureFeatureGroup(final Collection<? extends Feature> features) {
        if ((features == null) || (features.size() <= 0)) {
            this.groupFeatures = TypeSafeCollections.newHashSet();
        } else {
            this.groupFeatures = (Set<Feature>)TypeSafeCollections.newHashSet(features);
        }
        this.readOnlyGroupFeatures = Collections.unmodifiableCollection(groupFeatures);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean addFeature(final Feature toAdd) {
        final boolean changeHappened = groupFeatures.add(toAdd);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public boolean addFeatures(final Collection<? extends Feature> toAdd) {
        final boolean changeHappened = groupFeatures.addAll(toAdd);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public boolean removeFeature(final Feature toRemove) {
        final boolean changeHappened = groupFeatures.remove(toRemove);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public boolean removeFeatures(final Collection<? extends Feature> toRemove) {
        final boolean changeHappened = groupFeatures.removeAll(toRemove);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public Geometry getGeometry() {
        if (enclosingGeometry == null) {
            // lazy refresh, bei parallelem zugriff duch mehrere threads muesste
            // hier auf enclosingGeometry syncronisiert werden!
            refreshEnclosingGeometry();
        }
        return enclosingGeometry;
    }

    @Override
    public void setGeometry(final Geometry geom) {
        log.warn("Call for setGeometry(...) on FeatureGroup has no effects");
    }

    @Override
    public boolean canBeSelected() {
        return false;
    }

    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(final boolean editable) {
        log.warn("Call for setEditable(...) on FeatureGroup has no effects");
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void hide(final boolean hiding) {
    }

    @Override
    public Iterator<Feature> iterator() {
        return new IteratorWrapper(groupFeatures.iterator());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Feature[] toArray() {
        return groupFeatures.toArray(new Feature[groupFeatures.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getSize() {
        return groupFeatures.size();
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshEnclosingGeometry() {
        this.enclosingGeometry = FeatureGroups.getEnclosingGeometry(groupFeatures);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  read-only view of all contained features
     */
    @Override
    public Collection<? extends Feature> getFeatures() {
        return readOnlyGroupFeatures;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (parentFeature instanceof XStyledFeature) {
            sb.append(((XStyledFeature)parentFeature).getName());
        } else {
            sb.append(parentFeature);
        }
        sb.append(".");
        sb.append(myAttributeStringInParentFeature);
        return sb.toString();
    }

    @Override
    public Paint getLinePaint() {
        return null;
    }

    @Override
    public void setLinePaint(final Paint linePaint) {
    }

    @Override
    public int getLineWidth() {
        return 0;
    }

    @Override
    public void setLineWidth(final int width) {
    }

    @Override
    public Paint getFillingPaint() {
        return null;
    }

    @Override
    public void setFillingPaint(final Paint fillingStyle) {
    }

    @Override
    public float getTransparency() {
        return 0f;
    }

    @Override
    public void setTransparency(final float transparrency) {
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return null;
    }

    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
    }

    @Override
    public boolean isHighlightingEnabled() {
        return false;
    }

    @Override
    public void setHighlightingEnabled(final boolean enabled) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getEnclosingGeometry() {
        return enclosingGeometry;
    }

//    public void setEnclosingGeometry(Geometry enclosingGeometry) {
//        this.enclosingGeometry = enclosingGeometry;
//    }
    @Override
    public String getMyAttributeStringInParentFeature() {
        return myAttributeStringInParentFeature;
    }

    @Override
    public void setMyAttributeStringInParentFeature(final String myAttributeStringInParentFeature) {
        this.myAttributeStringInParentFeature = myAttributeStringInParentFeature;
    }

    @Override
    public FeatureGroup getParentFeature() {
        return parentFeature;
    }

    @Override
    public void setParentFeature(final FeatureGroup parentFeature) {
        this.parentFeature = parentFeature;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * private static final String TO_STRING_HEAD = "Gruppe ["; private static final String TO_STRING_SEPARATOR = ", ";
     * private static final String TO_STRING_END = "]"; @Override public String toString() { StringBuilder sb = new
     * StringBuilder(TO_STRING_HEAD); if (groupFeatures.size() > 0) { for (Feature f : groupFeatures) { if (f instanceof
     * XStyledFeature) { sb.append(((XStyledFeature) f).getName()); } else { sb.append(f.getGeometry()); }
     * sb.append(TO_STRING_SEPARATOR); } sb.delete(sb.length() - TO_STRING_SEPARATOR.length(), sb.length()); }
     * sb.append(TO_STRING_END); return sb.toString(); }
     *
     * @version  $Revision$, $Date$
     */
    final class IteratorWrapper implements Iterator<Feature> {

        //~ Instance fields ----------------------------------------------------

        private final Iterator<Feature> delegate;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new IteratorWrapper object.
         *
         * @param  delegate  DOCUMENT ME!
         */
        public IteratorWrapper(final Iterator<Feature> delegate) {
            this.delegate = delegate;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Feature next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            enclosingGeometry = null;
            delegate.remove();
        }
    }
}
