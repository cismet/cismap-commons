/*
 * AuthenticationCanceledException.java
 *
 * Created on 19. Oktober 2006, 09:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.exceptions;

/**
 *
 * @author Sebastian
 */
public class AuthenticationCanceledException extends Exception {
    
    /** Creates a new instance of AuthenticationCanceledException */
    public AuthenticationCanceledException() {
    super(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("Exception.AuthenticationCanceledException"));    
    }
    
    public AuthenticationCanceledException(String message) {
    super(message);
    }
    
}
