/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import de.cismet.cismap.commons.ConvertableToXML;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author nh
 */
public class StyleHistoryListModel implements ListModel, ConvertableToXML {
    private final Logger log = Logger.getLogger(StyleHistoryListModel.class);
    public static final String STYLE_ROOT = "StyleHistory";
    private List<Style> list;
    private SAXBuilder builder = new SAXBuilder();

    /**
     * Konstruktor mit bereits bestehender History.s
     * @param doc JDOM-Document das die History beinhält
     */
    public StyleHistoryListModel(File f) throws Exception {
        list = new LinkedList<Style>();
        try {
            Document doc = builder.build(f);
            Element root = doc.getRootElement();
            if (root != null) {
                for (Object o : doc.getRootElement().getChildren(Style.STYLE_ELEMENT)) {
                    if (o instanceof Element) {
                        Style newStyle = StyleFactory.createStyle((Element) o);
                        if (newStyle != null) {
                            list.add(newStyle);                            
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Fehler beim Laden der Style-History", ex);
        }
    }

    /**
     * Konstruktor mit leerer History.
     */
    public StyleHistoryListModel() {
        list = new LinkedList<Style>();
    }
    
    /**
     * Hängt ein neues Element an die Liste der vorhandenen Elemente an.
     * @param e neues JDOM-Element
     */
    public void addStyle(Style newStyle) {
        if (!elementEquals(newStyle)) {
            list.add(newStyle);
            if (list.size() > 15) {
                list.remove(0);
            }
        }
    }

    /**
     * Liefert das Element an der Stelle index der Historyliste.
     * @param index Index des angewählten Objekts
     * @return JDOM-Element
     */
    @Override
    public Object getElementAt(int index) {
        return list.get(index);
    }

    /**
     * Liefert die momentane Anzahl der gespeicherten Styles.
     * @return
     */
    public int getSize() {
        return list.size();
    }

    /**
     * Löscht alle gespeicherten Styles.
     */
    public void clear() {
        list.clear();
    }

    /**
     * Vergleicht das übergebene Element mit allen in der Liste.
     * @param compare zu vergleichendes Element
     * @return true, wenn übereinstimmung gefunden, sonst false
     */
    public boolean elementEquals(Style compare) {
        if (list.isEmpty()) {
            return false;
        } else {
            boolean returnValue = false;
            for (Style s : list) {
                if (s.compareTo(compare) == 0) {
                    returnValue = true;
                    break;
                }
            }
            return returnValue;
        }
    }

    /**
     * Returns this model as JDOM-element with all styles as children.
     */
    public Element getElement() {
        Element e = new Element(STYLE_ROOT);
        for (Style s : list) {
            e.addContent(s.getElement());
        }
        return e;
    }
    
    public void addListDataListener(ListDataListener l) {}
    public void removeListDataListener(ListDataListener l) {}

}
