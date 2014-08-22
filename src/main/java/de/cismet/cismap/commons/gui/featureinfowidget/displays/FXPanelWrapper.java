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
package de.cismet.cismap.commons.gui.featureinfowidget.displays;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import de.cismet.tools.gui.FXWebViewPanel;

/**
 * DOCUMENT ME!
 *
 * @author   daniel
 * @version  $Revision$, $Date$
 */
public class FXPanelWrapper extends JPanel {

    //~ Instance fields --------------------------------------------------------

    private FXWebViewPanel jfxPanel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FXPanelWrapper object.
     */
    public FXPanelWrapper() {
        jfxPanel = new FXWebViewPanel();
        this.setLayout(new BorderLayout());
        this.add(jfxPanel, BorderLayout.CENTER);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FXWebViewPanel getJfxPanel() {
        return jfxPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  jfxPanel  DOCUMENT ME!
     */
    public void setJfxPanel(final FXWebViewPanel jfxPanel) {
        this.jfxPanel = jfxPanel;
    }
}
