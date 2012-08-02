/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import java.util.LinkedList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.jdom.Document;

/**
 *
 * @author nh
 */
public class StyleRuleListModel implements ListModel {
    private List<String> attribute = null;
    private List<String> operator = null;
    private List<String> value = null;

    /**
     * Konstruktor mit bereits bestehender History.s
     * @param doc JDOM-Document das die History beinh\u00E4lt
     */
    public StyleRuleListModel(Document doc) {
        attribute = new LinkedList<String>();
        operator = new LinkedList<String>();
        value = new LinkedList<String>();
    }

    /**
     * Konstruktor mit leerer History.
     */
    public StyleRuleListModel() {
    }

    /**
     * Liefert das Element an der Stelle index der Historyliste.
     * @param index Index des angew\u00E4hlten Objekts
     * @return JDOM-Element
     */
    @Override
    public Object getElementAt(int index) {
        return null;
    }

    /**
     * Liefert die momentane Anzahl der gespeicherten Styles.
     * @return
     */
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

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }
}
