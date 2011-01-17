/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MementoInterface.java
 *
 * Created on 6. Dezember 2007, 09:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.interaction.memento;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

import java.util.Observable;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;

/**
 * MementoInterface schreibt bestimmte Methoden vor, die von einer Klasse implementiert werden, um eine korrekte
 * Memento-Funktionalit\u00E4t zu bieten.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public interface MementoInterface {

    //~ Instance fields --------------------------------------------------------

    String ACTIVATE = "ACTIVATE";     // NOI18N
    String DEACTIVATE = "DEACTIVATE"; // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CustomAction getLastAction();

    /**
     * DOCUMENT ME!
     *
     * @param  a  DOCUMENT ME!
     */
    void addAction(CustomAction a);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isEmpty();

    /**
     * DOCUMENT ME!
     */
    void clear();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getHistory();
}
