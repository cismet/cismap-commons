/*
 * Memento.java
 *
 * Created on 6. Dezember 2007, 09:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.interaction.memento;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import java.util.Observable;
import java.util.Stack;

/**
 * Die Memento-Klasse liefert die Moeglichkeit Aktionen zu speichern und zu einem
 * spaeteren Zeitpunkt wieder nach dem LIFO-Prinzip abzurufen.
 * @author nh
 */
public class Memento extends Observable implements MementoInterface {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Stack history;
    
    /**
     * Konstruktor f\u00FCr Memento-Instanzen.
     */
    public Memento() {
        history = new Stack();
    }
    
    /**
     * Liefert die letzte ausgefuehrte Aktion.
     * @return  letzte Aktion
     */
    public CustomAction getLastAction() {
        if ((history.size() > 1)) {
            return (CustomAction)history.pop();
        } else if (history.size() == 1) {
            setChanged();
            notifyObservers(MementoInterface.DEACTIVATE);
            return (CustomAction)history.pop();
        } else {
            setChanged();
            notifyObservers(MementoInterface.DEACTIVATE);
            return null;
        }
    }
    
    /**
     * Fuegt eine gerade getaetigte Aktion dem Stapel hinzu.
     * @param a die einzufuegende Aktion
     */
    public void addAction(CustomAction a) {
        history.push(a);
        setChanged();
        notifyObservers(MementoInterface.ACTIVATE);
    }
    
    /**
     * Liefert ein boolean, ob der Stack leer ist.
     */
    public boolean isEmpty() {
        return history.isEmpty();
    }
    
    /**
     * L\u00F6scht den Stack.
     */
    public void clear() {
        history.clear();
        setChanged();
        notifyObservers(MementoInterface.DEACTIVATE);
    }
    
    public String getHistory() {
        return this.history.toString();
    }
}
