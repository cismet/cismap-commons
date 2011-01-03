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
public class Scale {

    //~ Instance fields --------------------------------------------------------

    private int denominator = 0;
    private String text;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Scale object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Scale(final Element e) throws Exception {
        denominator = e.getAttribute("denominator").getIntValue(); // NOI18N
        text = e.getText();
    }
    /**
     * Creates a new Scale object.
     *
     * @param  scaleDenominator  DOCUMENT ME!
     * @param  text              DOCUMENT ME!
     */
    public Scale(final int scaleDenominator, final String text) {
        denominator = scaleDenominator;
        this.text = text;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return text;
    }
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Scale) && (((Scale)obj).denominator == denominator);
    }
    /**
     * DOCUMENT ME!
     *
     * @param   selected  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement(final boolean selected) {
        final Element e = new Element("scale");                       // NOI18N
        e.setAttribute("selected", new Boolean(selected).toString()); // NOI18N
        e.setAttribute("denominator", denominator + "");              // NOI18N
        e.setText(text);
        return e;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  denominator  DOCUMENT ME!
     */
    public void setDenominator(final int denominator) {
        this.denominator = denominator;
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
