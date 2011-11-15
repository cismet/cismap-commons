/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import java.awt.Paint;

import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * Wrapper implementation of {@link StyledFeatureGroupMember}.
 *
 * @author   Benjamin Friedrich (benjamin.friedrich@cismet.de)
 * @version  1.0, 15.11.2011
 */
public class StyledFeatureGroupWrapper extends FeatureGroupWrapper implements StyledFeatureGroupMember {

    //~ Instance fields --------------------------------------------------------

    private final StyledFeature feature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureGroupWrapper object.
     *
     * @param  feature    underlying feature to be wrapped
     * @param  groupId    group id
     * @param  groupName  group display name
     */
    public StyledFeatureGroupWrapper(final StyledFeature feature, final String groupId, final String groupName) {
        super(feature, groupId, groupName);

        this.feature = feature;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Paint getLinePaint() {
        return this.feature.getLinePaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLinePaint(final Paint linePaint) {
        this.feature.setLinePaint(linePaint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineWidth() {
        return this.feature.getLineWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLineWidth(final int width) {
        this.feature.setLineWidth(width);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Paint getFillingPaint() {
        return this.feature.getFillingPaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        this.feature.setFillingPaint(fillingStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTransparency() {
        return this.feature.getTransparency();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransparency(final float transparrency) {
        this.feature.setTransparency(transparrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return this.feature.getPointAnnotationSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.feature.setPointAnnotationSymbol(featureAnnotationSymbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHighlightingEnabled() {
        return this.feature.isHighlightingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHighlightingEnabled(final boolean enabled) {
        this.feature.setHighlightingEnabled(enabled);
    }
}
