/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cismap.commons.features.Feature;

/**
 * Wrapper implementation of {@link FeatureGroupMember}.
 *
 * @author   Benjamin Friedrich (benjamin.friedrich@cismet.de)
 * @version  1.0, 15.11.2011
 */
public class FeatureGroupWrapper implements FeatureGroupMember {

    //~ Instance fields --------------------------------------------------------

    private final String groupName;
    private final String groupId;
    private final Feature feature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureGroupWrapper object.
     *
     * @param   feature    underlying feature to be wrapped
     * @param   groupId    group id
     * @param   groupName  group display name
     *
     * @throws  NullPointerException  DOCUMENT ME!
     */
    public FeatureGroupWrapper(final Feature feature, final String groupId, final String groupName) {
        if (feature == null) {
            throw new NullPointerException("Given feature must not be null");
        }

        if (groupId == null) {
            throw new NullPointerException("Given groupId must not be null");
        }

        if (groupName == null) {
            throw new NullPointerException("Given groupName must not be null");
        }

        this.feature = feature;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Geometry getGeometry() {
        return this.feature.getGeometry();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGeometry(final Geometry geom) {
        this.feature.setGeometry(geom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeSelected() {
        return this.feature.canBeSelected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        this.feature.setCanBeSelected(canBeSelected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditable() {
        return this.feature.isEditable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEditable(final boolean editable) {
        this.feature.setEditable(editable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden() {
        return this.feature.isHidden();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hide(final boolean hiding) {
        this.feature.hide(hiding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Feature getFeature() {
        return this.feature;
    }
}
