/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import java.awt.Stroke;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface XStyledFeature extends StyledFeature {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ImageIcon getIconImage();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getName();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getType();
    /**
     * DOCUMENT ME!
     *
     * @param   refresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    JComponent getInfoComponent(Refreshable refresh);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Stroke getLineStyle();
}
