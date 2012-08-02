package de.cismet.cismap.commons.features;

/**
 *
 * @author srichter
 */
public abstract class FeatureCollectionAdapter implements FeatureCollectionListener {

    @Override
    public void featuresAdded(FeatureCollectionEvent fce) {
    }

    @Override
    public void allFeaturesRemoved(FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresRemoved(FeatureCollectionEvent fce) {
    }

    @Override
    public void featuresChanged(FeatureCollectionEvent fce) {
    }

    @Override
    public void featureSelectionChanged(FeatureCollectionEvent fce) {
    }

    @Override
    public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }
}
