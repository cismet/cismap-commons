/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class RaisePolygonListener extends PBasicInputEventHandler {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private MappingComponent mc = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RaisePolygonListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public RaisePolygonListener(final MappingComponent mc) {
        this.mc = mc;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (log.isDebugEnabled()) {
            log.debug("RaiseTry1"); // NOI18N
        }
        final PFeature o = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class });
        // if (o!=null&&o.getFeature() instanceof DefaultFeatureServiceFeature&& o.getVisible()==true &&
        // o.getParent()!=null && o.getParent().getVisible()==true) {
        if ((o != null) && (o.getFeature() != null) && (o.getVisible() == true) && (o.getParent() != null)
                    && (o.getParent().getVisible() == true)) {
            if (log.isDebugEnabled()) {
                log.debug("RaiseTry2"); // NOI18N
            }
            final PureNewFeature pnf = new PureNewFeature((Geometry)(o.getFeature().getGeometry().clone()));
            pnf.setEditable(true);
            mc.getFeatureCollection().addFeature(pnf);
        }
    }
}
