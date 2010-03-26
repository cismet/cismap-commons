package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.tools.collections.TypeSafeCollections;
import java.awt.Paint;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author srichter
 */
public class PureFeatureGroup implements FeatureGroup, StyledFeature {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String myAttributeStringInParentFeature = null;
    private FeatureGroup parentFeature = null;

    public PureFeatureGroup(Feature feature) {
        this();
        groupFeatures.add(feature);
    }

    public PureFeatureGroup(Collection<? extends Feature> features) {
        if (features == null || features.size() <= 0) {
            this.groupFeatures = TypeSafeCollections.newHashSet();
        } else {
            this.groupFeatures = TypeSafeCollections.newHashSet(features);
        }
        this.readOnlyGroupFeatures = Collections.unmodifiableCollection(groupFeatures);
    }

    public PureFeatureGroup() {
        this(Collections.EMPTY_LIST);
    }
    private final Set<Feature> groupFeatures;
    private final Collection<Feature> readOnlyGroupFeatures;
    private Geometry enclosingGeometry;
//    private boolean canBeSelected;

    @Override
    public boolean addFeature(Feature toAdd) {
        final boolean changeHappened = groupFeatures.add(toAdd);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public boolean addFeatures(Collection<? extends Feature> toAdd) {
        final boolean changeHappened = groupFeatures.addAll(toAdd);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public boolean removeFeature(Feature toRemove) {
        final boolean changeHappened = groupFeatures.remove(toRemove);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public boolean removeFeatures(Collection<? extends Feature> toRemove) {
        final boolean changeHappened = groupFeatures.removeAll(toRemove);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    @Override
    public Geometry getGeometry() {
        if (enclosingGeometry == null) {
            //lazy refresh, bei parallelem zugriff duch mehrere threads muesste
            //hier auf enclosingGeometry syncronisiert werden!
            refreshEnclosingGeometry();
        }
        return enclosingGeometry;
    }

    @Override
    public void setGeometry(Geometry geom) {
        log.warn("Call for setGeometry(...) on FeatureGroup has no effects");//NOI18N
    }

    @Override
    public boolean canBeSelected() {
        return false;
    }

    @Override
    public void setCanBeSelected(boolean canBeSelected) {
        
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        log.warn("Call for setEditable(...) on FeatureGroup has no effects");//NOI18N
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void hide(boolean hiding) {
    }

    @Override
    public Iterator<Feature> iterator() {
        return new IteratorWrapper(groupFeatures.iterator());
    }

    public Feature[] toArray() {
        return groupFeatures.toArray(new Feature[groupFeatures.size()]);
    }

    public int getSize() {
        return groupFeatures.size();
    }

    private void refreshEnclosingGeometry() {
        this.enclosingGeometry = FeatureGroups.getEnclosingGeometry(groupFeatures);
    }

    /**
     *
     * @return read-only view of all contained features
     */
    @Override
    public Collection<? extends Feature> getFeatures() {
        return readOnlyGroupFeatures;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (parentFeature instanceof XStyledFeature) {
            sb.append(((XStyledFeature) parentFeature).getName());
        } else {
            sb.append(parentFeature);
        }
        sb.append(".");//NOI18N
        sb.append(myAttributeStringInParentFeature);
        return sb.toString();
    }

    @Override
    public Paint getLinePaint() {
        return null;
    }

    @Override
    public void setLinePaint(Paint linePaint) {
    }

    @Override
    public int getLineWidth() {
        return 0;
    }

    @Override
    public void setLineWidth(int width) {
    }

    @Override
    public Paint getFillingPaint() {
        return null;
    }

    @Override
    public void setFillingPaint(Paint fillingStyle) {
    }

    @Override
    public float getTransparency() {
        return 0f;
    }

    @Override
    public void setTransparency(float transparrency) {
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return null;
    }

    @Override
    public void setPointAnnotationSymbol(FeatureAnnotationSymbol featureAnnotationSymbol) {
    }

    @Override
    public boolean isHighlightingEnabled() {
        return false;
    }

    @Override
    public void setHighlightingEnabled(boolean enabled) {
    }

//    private static final String TO_STRING_HEAD = "Gruppe [";
//    private static final String TO_STRING_SEPARATOR = ", ";
//    private static final String TO_STRING_END = "]";
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder(TO_STRING_HEAD);
//        if (groupFeatures.size() > 0) {
//            for (Feature f : groupFeatures) {
//                if (f instanceof XStyledFeature) {
//                    sb.append(((XStyledFeature) f).getName());
//                } else {
//                    sb.append(f.getGeometry());
//                }
//                sb.append(TO_STRING_SEPARATOR);
//            }
//            sb.delete(sb.length() - TO_STRING_SEPARATOR.length(), sb.length());
//        }
//        sb.append(TO_STRING_END);
//        return sb.toString();
//    }
    final class IteratorWrapper implements Iterator<Feature> {

        public IteratorWrapper(Iterator<Feature> delegate) {
            this.delegate = delegate;
        }
        private final Iterator<Feature> delegate;

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
    public void setMyAttributeStringInParentFeature(String myAttributeStringInParentFeature) {
        this.myAttributeStringInParentFeature = myAttributeStringInParentFeature;
    }

    @Override
    public FeatureGroup getParentFeature() {
        return parentFeature;
    }

    @Override
    public void setParentFeature(FeatureGroup parentFeature) {
        this.parentFeature = parentFeature;
    }
}
