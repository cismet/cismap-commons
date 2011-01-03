/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleSelectionEventHandler.java
 *
 * Created on 7. M\u00E4rz 2005, 18:00
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import java.awt.Color;
import java.awt.Paint;

import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class SimpleSelectionEventHandler extends PSelectionEventHandler {

    //~ Instance fields --------------------------------------------------------

    Paint notSelectedStrokePaint = null;
    Paint selectedStrokePaint = Color.red;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleSelectionEventHandler object.
     *
     * @param  marqueeParent     DOCUMENT ME!
     * @param  selectableParent  DOCUMENT ME!
     */
    public SimpleSelectionEventHandler(final PNode marqueeParent, final PNode selectableParent) {
        super(marqueeParent, selectableParent);
    }

    /**
     * Creates a new SimpleSelectionEventHandler object.
     *
     * @param  marqueeParent      DOCUMENT ME!
     * @param  selectableParents  DOCUMENT ME!
     */
    public SimpleSelectionEventHandler(final PNode marqueeParent, final List selectableParents) {
        super(marqueeParent, selectableParents);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void decorateSelectedNode(final PNode node) {
        node.moveToFront();
        if (node instanceof PPath) {
            notSelectedStrokePaint = ((PPath)node).getStrokePaint();
            ((PPath)node).setStrokePaint(selectedStrokePaint);
        }
    }

    @Override
    public void undecorateSelectedNode(final PNode node) {
        if (node instanceof PPath) {
            ((PPath)node).setStrokePaint(notSelectedStrokePaint);
        }
    }
}
