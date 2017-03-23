/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.awt.Image;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DerivedFixedPImage extends FixedPImage implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    PFeature parent;
    DeriveRule rule;
    final MappingComponent mappingComponent;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DerivedFixedPImage object.
     *
     * @param  image   DOCUMENT ME!
     * @param  parent  DOCUMENT ME!
     * @param  rule    DOCUMENT ME!
     */
    public DerivedFixedPImage(final Image image, final PFeature parent, final DeriveRule rule) {
        super(image);
        this.parent = parent;
        this.rule = rule;
        parent.addPropertyChangeListener(this);
        mappingComponent = parent.getMappingComponent();
        mappingComponent.addStickyNode(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final Geometry g = parent.getFeature().getGeometry();
        final Geometry derivedG = rule.derive(g);
        // should be a point. To be sure user
        final Point p = derivedG.getCentroid();
        final double x = mappingComponent.getWtst().getScreenX(p.getCoordinate().x);
        final double y = mappingComponent.getWtst().getScreenY(p.getCoordinate().y);
        super.setOffset(x, y);
        final double s = mappingComponent.getCamera().getViewScale();
        final double targetScale = (mappingComponent.getStickyFeatureCorrectionFactor() / s);

        // prevent endless loop of property change events with if statement. prpably should insert that in
        // mappingComponent.rescaleStickyNodes()
        if (Math.round(getScale() * 100000) != Math.round(targetScale * 100000)) {
            mappingComponent.rescaleStickyNodes();
        }
    }
}
