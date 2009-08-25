/*
 * BadHttpStatusCodeException.java
 *
 * Created on 19. Oktober 2006, 10:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.exceptions;

import java.awt.EventQueue;
import javax.swing.SwingUtilities;

/**
 *
 * @author Sebastian
 */
public class BadHttpStatusCodeException extends Exception{
    
    int statuscode;
    
    /** Creates a new instance of BadHttpStatusCodeException */
    public BadHttpStatusCodeException() {
        super();        
    }
    
    public BadHttpStatusCodeException(String message) {
        super(message);
    }
    
    public BadHttpStatusCodeException(String message, int statuscode) {
        super(message);
        this.statuscode = statuscode;
    }
    
    public int getHttpStatuscode(){
        return statuscode;
    }

    @Override
    public String getMessage() {
        return super.getMessage()+": "+statuscode;
    }
    
    
}
