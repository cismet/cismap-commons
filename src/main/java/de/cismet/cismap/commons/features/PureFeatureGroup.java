package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.tools.collections.TypeSafeCollections;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author srichter
 */
public class PureFeatureGroup implements SubFeature, FeatureGroup {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String myAttributeStringInParentFeature = null;
    private FeatureGroup parentFeature = null;

    public PureFeatureGroup(Feature feature) {
        this();
        groupFeatures.add(feature);
    }

    public PureFeatureGroup(Collection<Feature> features) {
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
    private boolean canBeSelected;

    public boolean addFeature(Feature toAdd) {
        final boolean changeHappened = groupFeatures.add(toAdd);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    public boolean addFeatures(Collection<Feature> toAdd) {
        final boolean changeHappened = groupFeatures.addAll(toAdd);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    public boolean removeFeature(Feature toRemove) {
        final boolean changeHappened = groupFeatures.remove(toRemove);
        if (changeHappened) {
            enclosingGeometry = null;
        }
        return changeHappened;
    }

    public boolean removeFeatures(Collection<Feature> toRemove) {
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
        log.warn("Call for setGeometry(...) on FeatureGroup has no effects");
    }

    @Override
    public boolean canBeSelected() {
        return canBeSelected;
    }

    @Override
    public void setCanBeSelected(boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        log.warn("Call for setEditable(...) on FeatureGroup has no effects");
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
        Geometry calculatedEnclosingGeom = null;
        Geometry currentFeatureGeom = null;
        for (Feature f : groupFeatures) {
            currentFeatureGeom = f.getGeometry();
            if (currentFeatureGeom != null) {
                currentFeatureGeom = currentFeatureGeom.getEnvelope();
                if (calculatedEnclosingGeom == null) {
                    calculatedEnclosingGeom = f.getGeometry().getEnvelope();
                } else {
                    calculatedEnclosingGeom = calculatedEnclosingGeom.getEnvelope().union(currentFeatureGeom).getEnvelope();
                }
            }
        }
        this.enclosingGeometry = calculatedEnclosingGeom;
    }

    /**
     *
     * @return read-only view of all contained features
     */
    @Override
    public Collection<Feature> getFeatures() {
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
        sb.append(".");
        sb.append(myAttributeStringInParentFeature);
        return sb.toString();
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

    public void setEnclosingGeometry(Geometry enclosingGeometry) {
        this.enclosingGeometry = enclosingGeometry;
    }

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
