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
 *
 * @author hell
 */
public class SimpleSelectionEventHandler extends PSelectionEventHandler {
    Paint notSelectedStrokePaint = null;
    Paint selectedStrokePaint = Color.red;

    public SimpleSelectionEventHandler(PNode marqueeParent, PNode selectableParent) {
        super(marqueeParent, selectableParent);
    }

    public SimpleSelectionEventHandler(PNode marqueeParent, List selectableParents) {
        super(marqueeParent, selectableParents);
    }

    @Override
    public void decorateSelectedNode(PNode node) {
        node.moveToFront();
        if (node instanceof PPath) {
            notSelectedStrokePaint = ((PPath) node).getStrokePaint();
            ((PPath) node).setStrokePaint(selectedStrokePaint);
        }
    }

    @Override
    public void undecorateSelectedNode(PNode node) {
        if (node instanceof PPath) {
            ((PPath) node).setStrokePaint(notSelectedStrokePaint);
        }
    }
}
