/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import javax.swing.JComponent;

import de.cismet.cismap.commons.gui.ToolbarComponentsProvider.ToolbarPositionHint;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class ToolbarComponentDescription {

    //~ Instance fields --------------------------------------------------------

    private final String toolbarID;
    private final JComponent component;
    private final ToolbarPositionHint positionHint;
    private final String anchorComponentName;

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
        this.toolbarID = toolbarID;
        this.component = component;
        this.positionHint = positionHint;
        this.anchorComponentName = anchorComponent;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the toolbarID
     */
    public String getToolbarID() {
        return toolbarID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the component
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the positionHint
     */
    public ToolbarPositionHint getPositionHint() {
        return positionHint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the anchorComponentName
     */
    public String getAnchorComponentName() {
        return anchorComponentName;
    }
}
