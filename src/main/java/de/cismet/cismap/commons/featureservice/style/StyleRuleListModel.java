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
package de.cismet.cismap.commons.featureservice.style;

import org.jdom.Document;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * DOCUMENT ME!
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class StyleRuleListModel implements ListModel {

    //~ Instance fields --------------------------------------------------------

    private List<String> attribute = null;
    private List<String> operator = null;
    private List<String> value = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Konstruktor mit leerer History.
     */
    public StyleRuleListModel() {
    }

    /**
     * Konstruktor mit bereits bestehender History.s.
     *
     * @param  doc  JDOM-Document das die History beinh\u00E4lt
     */
    public StyleRuleListModel(final Document doc) {
        attribute = new LinkedList<String>();
        operator = new LinkedList<String>();
        value = new LinkedList<String>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Liefert das Element an der Stelle index der Historyliste.
     *
     * @param   index  Index des angew\u00E4hlten Objekts
     *
     * @return  JDOM-Element
     */
    @Override
    public Object getElementAt(final int index) {
        return null;
    }

    /**
     * Liefert die momentane Anzahl der gespeicherten Styles.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getSize() {
        return attribute.size();
    }

    /**
     * L\u00F6scht alle gespeicherten Styles.
     */
    public void clear() {
        attribute.clear();
        operator.clear();
        value.clear();
    }

    @Override
    public void addListDataListener(final ListDataListener l) {
    }

    @Override
    public void removeListDataListener(final ListDataListener l) {
    }
}
