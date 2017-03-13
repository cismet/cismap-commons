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

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingInputListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String NAME = "RasterGeoRefInputListener";

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final PInputEvent pie) {
        super.mouseClicked(pie);

        //RasterGeoReferencingWizard.getInstance().getHandler().
    }
}
