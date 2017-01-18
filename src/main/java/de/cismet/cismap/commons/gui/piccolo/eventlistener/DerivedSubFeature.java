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

import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.geom.GeneralPath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DerivedSubFeature extends PPath implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    PFeature parent;
    DeriveRule rule;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DerivedSubFeature object.
     *
     * @param  parent  DOCUMENT ME!
     * @param  rule    DOCUMENT ME!
     */
    public DerivedSubFeature(final PFeature parent, final DeriveRule rule) {
        this.parent = parent;
        this.rule = rule;
        parent.addPropertyChangeListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final Geometry g = parent.getFeature().getGeometry();
        final DefaultStyledFeature dsf = new DefaultStyledFeature();
        dsf.setGeometry(rule.derive(g));
        final PFeature p = new PFeature(dsf, CismapBroker.getInstance().getMappingComponent());
        super.setPathTo(new GeneralPath(p.getPathReference()));
    }
}
