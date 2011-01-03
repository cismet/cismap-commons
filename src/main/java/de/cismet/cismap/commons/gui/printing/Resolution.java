/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.printing;

import org.jdom.Element;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class Resolution {

    //~ Instance fields --------------------------------------------------------

    private int resolution = 72;
    private String text;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Resolution object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Resolution(final Element e) throws Exception {
        resolution = e.getAttribute("dpi").getIntValue(); // NOI18N
        text = e.getText();
    }
    /**
     * Creates a new Resolution object.
     *
     * @param  resolution  DOCUMENT ME!
     * @param  text        DOCUMENT ME!
     */
    public Resolution(final int resolution, final String text) {
        this.resolution = resolution;
        this.text = text;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return text;
    }
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Resolution) && (((Resolution)obj).resolution == resolution);
    }
    /**
     * DOCUMENT ME!
     *
     * @param   selected  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement(final boolean selected) {
        final Element e = new Element("resolution");          // NOI18N
        e.setAttribute("selected", String.valueOf(selected)); // NOI18N
        e.setAttribute("dpi", resolution + "");               // NOI18N
        e.setText(text);
        return e;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  resolution  DOCUMENT ME!
     */
    public void setResolution(final int resolution) {
        this.resolution = resolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getText() {
        return text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  text  DOCUMENT ME!
     */
    public void setText(final String text) {
        this.text = text;
    }
}
