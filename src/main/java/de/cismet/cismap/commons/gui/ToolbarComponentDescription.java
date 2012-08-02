package de.cismet.cismap.commons.gui;

import de.cismet.cismap.commons.gui.ToolbarComponentsProvider.ToolbarPositionHint;
import javax.swing.JComponent;

/**
 *
 * @author srichter
 */
public final class ToolbarComponentDescription {

    public ToolbarComponentDescription(String toolbarID, JComponent component, ToolbarPositionHint positionHint, String anchorComponent) {
        this.toolbarID = toolbarID;
        this.component = component;
        this.positionHint = positionHint;
        this.anchorComponentName = anchorComponent;
    }
    private final String toolbarID;
    private final JComponent component;
    private final ToolbarPositionHint positionHint;
    private final String anchorComponentName;

    /**
     * @return the toolbarID
     */
    public String getToolbarID() {
        return toolbarID;
    }

    /**
     * @return the component
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * @return the positionHint
     */
    public ToolbarPositionHint getPositionHint() {
        return positionHint;
    }

    /**
     * @return the anchorComponentName
     */
    public String getAnchorComponentName() {
        return anchorComponentName;
    }
}
