/*
 * MementoInterface.java
 *
 * Created on 6. Dezember 2007, 09:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.interaction.memento;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import java.util.Observable;

/**
 * MementoInterface schreibt bestimmte Methoden vor, die von einer Klasse
 * implementiert werden, um eine korrekte Memento-Funktionalit\u00E4t zu bieten.
 * @author nh
 */
public interface MementoInterface {
    
    public static final String ACTIVATE = "ACTIVATE";//NOI18N
    public static final String DEACTIVATE = "DEACTIVATE";//NOI18N
    
    public CustomAction getLastAction();
    
    public void addAction(CustomAction a);
    
    public boolean isEmpty();
    
    public void clear();
    
    public String getHistory();
}
