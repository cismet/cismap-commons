/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DeleteFeatureListener.java
 *
 * Created on 20. April 2005, 11:22
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureCreateAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class DeleteFeatureListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String FEATURE_DELETE_REQUEST_NOTIFICATION = "FEATURE_DELETE_REQUEST_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature featureRequestedForDeletion = null;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();
            final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
            if ((o instanceof PFeature) && (o != null) && ((PFeature)o).getFeature().isEditable()
                        && ((PFeature)o).getFeature().canBeSelected()) {
                super.mouseClicked(pInputEvent);
                final PFeature pf = (PFeature)o;
                featureRequestedForDeletion = (PFeature)pf.clone();
                mc.getFeatureCollection().removeFeature(pf.getFeature());
                mc.getMemUndo().addAction(new FeatureCreateAction(mc, pf.getFeature()));
                mc.getMemRedo().clear();
                postFeatureDeleteRequest();
            } else {
                featureRequestedForDeletion = null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postFeatureDeleteRequest() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(FEATURE_DELETE_REQUEST_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureRequestedForDeletion() {
        return featureRequestedForDeletion;
    }
}
