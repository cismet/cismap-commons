/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Point;

import javax.swing.Action;
import javax.swing.JMenu;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface MapPopupAction extends Action, Comparable<MapPopupAction> {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the {@link Point} where the action was triggered.
     *
     * @return  the <code>point</code> where the action was triggered
     */
    Point getPoint();

    /**
     * Setter for the {@link Point} where the action will be triggered.
     *
     * @param  point  the <code>point</code> where the action will be triggered
     */
    void setPoint(Point point);

    /**
     * Returns the desired position in the popup menu.
     *
     * @return  an integer
     */
    int getPosition();

    /**
     * The action may decide whether it shall be shown in the upcoming popup menu or not.
     *
     * @param   featuresSubjacent  <code>true</code>, if the action popup menu was triggered over a feature, <code>
     *                             false</code> otherwise
     *
     * @return  <code>true</code>, if the action shall be displayed, <code>false</code> otherwise
     */
    boolean isActive(boolean featuresSubjacent);

    /**
     * In some cases it's preferred to show a whole menu in the upcoming popup menu and not only one single menu item.
     * If this method returns a JMenu object, it will be added to the popup menu instead of the MapPopupAction instance.
     *
     * @return  The menu to be shown in the upcoming popup menu.
     */
    JMenu getSubmenu();
}
