/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.capabilitywidget;

import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SelectionAndCapabilities {

    //~ Instance fields --------------------------------------------------------

    private TreePath[] selection;
    private WMSCapabilities capabilities;
    private String url;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionAndCapabilities object.
     *
     * @param  s    DOCUMENT ME!
     * @param  c    DOCUMENT ME!
     * @param  url  DOCUMENT ME!
     */
    public SelectionAndCapabilities(final TreePath[] s, final WMSCapabilities c, final String url) {
        selection = s;
        capabilities = c;
        this.url = url;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreePath[] getSelection() {
        return selection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selection  DOCUMENT ME!
     */
    public void setSelection(final TreePath[] selection) {
        this.selection = selection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WMSCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilities  DOCUMENT ME!
     */
    public void setCapabilities(final WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUrl() {
        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    public void setUrl(final String url) {
        this.url = url;
    }
}
