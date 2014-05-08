/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.interaction;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.awt.event.ActionEvent;

import java.io.StringReader;

import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DefaultXMLQueryButtonAction extends DefaultQueryButtonAction {

    //~ Instance initializers --------------------------------------------------

    {
        posCorrection = -1;
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     */
    public DefaultXMLQueryButtonAction() {
        super();
    }

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     *
     * @param  text  DOCUMENT ME!
     */
    public DefaultXMLQueryButtonAction(final String text) {
        super(text);
    }

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     *
     * @param  queryText  DOCUMENT ME!
     * @param  text       DOCUMENT ME!
     */
    public DefaultXMLQueryButtonAction(final String queryText, final String text) {
        super(queryText, text);
    }

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     *
     * @param  text   DOCUMENT ME!
     * @param  width  DOCUMENT ME!
     */
    public DefaultXMLQueryButtonAction(final String text, final int width) {
        super(text, width);
    }

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     *
     * @param  queryText  DOCUMENT ME!
     * @param  text       DOCUMENT ME!
     * @param  width      DOCUMENT ME!
     */
    public DefaultXMLQueryButtonAction(final String queryText, final String text, final int width) {
        super(queryText, text, width);
    }

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     *
     * @param  text           DOCUMENT ME!
     * @param  width          DOCUMENT ME!
     * @param  posCorrection  DOCUMENT ME!
     */
    public DefaultXMLQueryButtonAction(final String text, final int width, final int posCorrection) {
        super(text, width, posCorrection);
    }

    /**
     * Creates a new DefaultXMLQueryButtonAction object.
     *
     * @param  queryText      DOCUMENT ME!
     * @param  text           DOCUMENT ME!
     * @param  width          DOCUMENT ME!
     * @param  posCorrection  DOCUMENT ME!
     */
    public DefaultXMLQueryButtonAction(final String queryText,
            final String text,
            final int width,
            final int posCorrection) {
        super(queryText, text, width, posCorrection);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (queryTextArea.getSelectionEnd() == 0) {
            AppendString(getStartTag(queryText) + getEndTag(queryText));
            CorrectCarret(-1 * getEndTag(queryText).length());
        } else {
            final int start = queryTextArea.getSelectionStart();
            final int end = queryTextArea.getSelectionEnd();
            queryTextArea.insert(getStartTag(queryText), start);
            queryTextArea.insert(getEndTag(queryText), end + getStartTag(queryText).length());
            // jTextArea1.setCaretPosition(end + 2);
            if (start == end) {
                CorrectCarret(posCorrection);
            } else {
                CorrectCarret(getEndTag(queryText).length() + 1);
            }
        }

//        // Test
//        String beforeCaret = queryTextArea.getText().substring(0, queryTextArea.getCaretPosition());
//
//        try {
//            SAXBuilder builder = new SAXBuilder();
//            Document d = builder.build(new StringReader("<root>" + queryTextArea.getText() + "</root>"));
//            String xmlText = FeatureServiceUtilities.elementToString(d.getRootElement());
//            xmlText = xmlText.substring(xmlText.indexOf("\n"), xmlText.lastIndexOf("\n"));
//            queryTextArea.setText(xmlText);
//        } catch (Exception ex) {
//            //nothing to do
//        }
//        //Ende Test
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getStartTag(final String name) {
        return "<" + name + ">";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getEndTag(final String name) {
        return "</" + name + ">";
    }
}
