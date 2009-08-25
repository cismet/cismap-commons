/*
 * AbstractPrintingInscriber.java
 *
 * Created on 11. Juli 2006, 11:52
 */

package de.cismet.cismap.commons.gui.printing;

import java.util.HashMap;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public abstract class AbstractPrintingInscriber extends javax.swing.JPanel {
    
    /** Creates new form AbstractPrintingInscriber */
    public AbstractPrintingInscriber() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    /** This Method should return the values in the Form<br>
     *  key: placeholderName
     *  value: value
     */
    public abstract HashMap<String,String> getValues();
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
