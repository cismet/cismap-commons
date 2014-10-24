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

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DefaultQueryButtonAction implements ActionListener {

    //~ Instance fields --------------------------------------------------------

    protected Logger LOG = Logger.getLogger(DefaultQueryButtonAction.class);
    protected JTextArea queryTextArea;
    protected String text;
    protected String queryText;
    protected int width;
    protected boolean startWithSpace = true;

    protected int posCorrection = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ActionListenerImpl object.
     */
    public DefaultQueryButtonAction() {
        this("");
    }

    /**
     * Creates a new DefaultQueryButtonAction object.
     *
     * @param  text  DOCUMENT ME!
     */
    public DefaultQueryButtonAction(final String text) {
        this(text, 2);
    }

    /**
     * Creates a new DefaultQueryButtonAction object.
     *
     * @param  queryText  DOCUMENT ME!
     * @param  text       DOCUMENT ME!
     */
    public DefaultQueryButtonAction(final String queryText, final String text) {
        this(queryText, text, 2);
    }

    /**
     * Creates a new DefaultQueryButtonAction object.
     *
     * @param  text   DOCUMENT ME!
     * @param  width  DOCUMENT ME!
     */
    public DefaultQueryButtonAction(final String text, final int width) {
        this(text, 2, 0);
    }

    /**
     * Creates a new DefaultQueryButtonAction object.
     *
     * @param  queryText  DOCUMENT ME!
     * @param  text       DOCUMENT ME!
     * @param  width      DOCUMENT ME!
     */
    public DefaultQueryButtonAction(final String queryText, final String text, final int width) {
        this(queryText, text, 2, 0);
    }

    /**
     * Creates a new DefaultQueryButtonAction object.
     *
     * @param  text           DOCUMENT ME!
     * @param  width          DOCUMENT ME!
     * @param  posCorrection  DOCUMENT ME!
     */
    public DefaultQueryButtonAction(final String text, final int width, final int posCorrection) {
        this(text, text, width, posCorrection);
    }

    /**
     * Creates a new DefaultQueryButtonAction object.
     *
     * @param  queryText      DOCUMENT ME!
     * @param  text           DOCUMENT ME!
     * @param  width          DOCUMENT ME!
     * @param  posCorrection  DOCUMENT ME!
     */
    public DefaultQueryButtonAction(final String queryText,
            final String text,
            final int width,
            final int posCorrection) {
        this.queryText = queryText;
        this.text = text;
        this.width = width;
        this.posCorrection = posCorrection;
    }

    //~ Methods ----------------------------------------------------------------

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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getQueryText() {
        return queryText;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  queryText  DOCUMENT ME!
     */
    public void setQueryText(final String queryText) {
        this.queryText = queryText;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (getQueryTextArea().getSelectionEnd() == getQueryTextArea().getSelectionStart()) {
            AppendString(queryText);
            CorrectCarret(posCorrection);
        } else {
            WriteOver(queryText);
            CorrectCarret(posCorrection);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  change  DOCUMENT ME!
     */
    protected void CorrectCarret(final int change) {
        if (change != 0) {
            getQueryTextArea().setCaretPosition(getQueryTextArea().getCaretPosition() + change);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  str  DOCUMENT ME!
     */
    protected void WriteOver(final String str) {
        String text1 = getQueryTextArea().getText().substring(0, getQueryTextArea().getSelectionStart());
        String text2 = getQueryTextArea().getText().substring(getQueryTextArea().getSelectionEnd());
        if (text1.length() >= 1) {
            switch (text1.charAt(text1.length() - 1)) {
                case ' ':
                case '(': {
                    text1 = text1 + " ";
                    break;
                }
            }
        }
        if (text2.length() >= 1) {
            switch (text2.charAt(0)) {
                case ' ':
                case '(': {
                    text2 = " " + text2;
                    break;
                }
            }
        }
        getQueryTextArea().setText(text1 + str + text2);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  str  DOCUMENT ME!
     */
    protected void AppendString(String str) {
        // jTextArea1.append(str + " ");
        if ((getQueryTextArea().getText() != null) && !queryTextArea.getText().isEmpty()) {
            try {
                if (!queryTextArea.getText(queryTextArea.getCaretPosition() - 1, 1).contains("(")) {
                    str = (startWithSpace ? " " : "") + str;
                }
            } catch (BadLocationException ex) {
                LOG.error("Error ewhile appending string", ex);
                str = (startWithSpace ? " " : "") + str;
            }
        }
        getQueryTextArea().insert(str, getQueryTextArea().getCaretPosition());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the queryTextArea
     */
    public JTextArea getQueryTextArea() {
        return queryTextArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  queryTextArea  the queryTextArea to set
     */
    public void setQueryTextArea(final JTextArea queryTextArea) {
        this.queryTextArea = queryTextArea;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  width  the width to set
     */
    public void setWidth(final int width) {
        this.width = width;
    }
}
