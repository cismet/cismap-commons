/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import javax.swing.JComponent;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FeatureRenderer {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    java.awt.Stroke getLineStyle();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    java.awt.Paint getLinePaint();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    java.awt.Paint getFillingStyle();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getTransparency();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureAnnotationSymbol getPointSymbol();

    /**
     * DOCUMENT ME!
     *
     * @param   refresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    JComponent getInfoComponent(Refreshable refresh);
}
