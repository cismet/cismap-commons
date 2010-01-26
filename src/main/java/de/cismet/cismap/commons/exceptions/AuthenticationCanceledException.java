/*
 * AuthenticationCanceledException.java
 *
 * Created on 19. Oktober 2006, 09:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.exceptions;

import java.util.ResourceBundle;

/**
 *
 * @author Sebastian
 */
public class AuthenticationCanceledException extends Exception {

    private static final ResourceBundle I18N = 
            ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");

    /** Creates a new instance of AuthenticationCanceledException */
    public AuthenticationCanceledException() {
        super(I18N.getString("de.cismet.cismap.commons.exceptions.AuthenticationCanceledException"));
    }
    
    public AuthenticationCanceledException(String message) {
        super(message);
    }

}
