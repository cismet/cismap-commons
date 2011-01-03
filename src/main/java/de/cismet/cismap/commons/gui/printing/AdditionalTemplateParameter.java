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
public class AdditionalTemplateParameter {

    //~ Instance fields --------------------------------------------------------

    private String placeholder = ""; // NOI18N
    private String title = "";       // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AdditionalTemplateParameter object.
     *
     * @param  parameter  DOCUMENT ME!
     */
    public AdditionalTemplateParameter(final Element parameter) {
        placeholder = parameter.getAttribute("placeholder").getValue(); // NOI18N
        title = parameter.getAttribute("title").getValue();             // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Element getElement() {
        final Element e = new Element("parameter");      // NOI18N
        e.setAttribute("placeholder", getPlaceholder()); // NOI18N
        e.setAttribute("title", getTitle());             // NOI18N
        return e;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  placeholder  DOCUMENT ME!
     */
    public void setPlaceholder(final String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
