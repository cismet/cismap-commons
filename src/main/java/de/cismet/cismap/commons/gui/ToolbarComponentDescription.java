/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.JComponent;

import de.cismet.cismap.commons.gui.ToolbarComponentsProvider.ToolbarPositionHint;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
@Getter
@AllArgsConstructor
public final class ToolbarComponentDescription {

    //~ Instance fields --------------------------------------------------------

    private final String toolbarID;
    private final JComponent component;
    private final ToolbarPositionHint positionHint;
    private final String anchorComponentName;
    private final boolean interactionMode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ToolbarComponentDescription object.
     *
     * @param  toolbarID        DOCUMENT ME!
     * @param  component        DOCUMENT ME!
     * @param  positionHint     DOCUMENT ME!
     * @param  anchorComponent  DOCUMENT ME!
     */
    public ToolbarComponentDescription(final String toolbarID,
            final JComponent component,
            final ToolbarPositionHint positionHint,
            final String anchorComponent) {
        this(toolbarID, component, positionHint, anchorComponent, false);
    }
}
