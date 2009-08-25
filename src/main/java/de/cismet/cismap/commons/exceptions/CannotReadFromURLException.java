/*
 * CantReadFromURLException.java
 *
 * Created on 19. Oktober 2006, 10:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.exceptions;

/**
 *
 * @author Sebastian
 */
public class CannotReadFromURLException extends Exception {
    
    /** Creates a new instance of CantReadFromURLException */
    public CannotReadFromURLException() {
        super();
    }
    
    public CannotReadFromURLException(String message) {
        super(message);
    }
    
}
