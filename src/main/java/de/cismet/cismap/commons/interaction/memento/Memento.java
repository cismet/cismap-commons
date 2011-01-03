/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Memento.java
 *
 * Created on 6. Dezember 2007, 09:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.interaction.memento;

import java.util.Observable;
import java.util.Stack;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;

/**
 * Die Memento-Klasse liefert die Moeglichkeit Aktionen zu speichern und zu einem spaeteren Zeitpunkt wieder nach dem
 * LIFO-Prinzip abzurufen.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class Memento extends Observable implements MementoInterface {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Stack history;

    //~ Constructors -----------------------------------------------------------

    /**
     * Konstruktor f\u00FCr Memento-Instanzen.
     */
    public Memento() {
        history = new Stack();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Liefert die letzte ausgefuehrte Aktion.
     *
     * @return  letzte Aktion
     */
    @Override
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
     *
     * @param  a  die einzufuegende Aktion
     */
    @Override
    public void addAction(final CustomAction a) {
        history.push(a);
        setChanged();
        notifyObservers(MementoInterface.ACTIVATE);
    }

    /**
     * Liefert ein boolean, ob der Stack leer ist.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isEmpty() {
        return history.isEmpty();
    }

    /**
     * L\u00F6scht den Stack.
     */
    @Override
    public void clear() {
        history.clear();
        setChanged();
        notifyObservers(MementoInterface.DEACTIVATE);
    }

    @Override
    public String getHistory() {
        return this.history.toString();
    }
}
