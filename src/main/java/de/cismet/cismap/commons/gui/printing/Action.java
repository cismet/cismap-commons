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
public class Action {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PRINTPREVIEW = "PRINTPREVIEW"; // NOI18N
    public static final String PDF = "PDF";                   // NOI18N
    public static final String PRINT = "PRINT";               // NOI18N

    //~ Instance fields --------------------------------------------------------

    private String id;
    private String title;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Action.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Action(final Element e) throws Exception {
        id = e.getAttribute("id").getValue(); // NOI18N
        title = e.getText();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return getTitle();
    }
    /**
     * DOCUMENT ME!
     *
     * @param   selected  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement(final boolean selected) {
        final Element e = new Element("action");              // NOI18N
        e.setAttribute("selected", String.valueOf(selected)); // NOI18N
        e.setAttribute("id", id);                             // NOI18N
        e.setText(getTitle());
        return e;
    }
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Action) && ((Action)obj).id.equals(id);
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public void setId(final String id) {
        this.id = id;
    }
}
