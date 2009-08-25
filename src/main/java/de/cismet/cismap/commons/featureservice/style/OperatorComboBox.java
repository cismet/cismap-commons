/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.style;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author nh
 */
public class OperatorComboBox extends JComboBox {
    String[] arr =  {"kleiner als", "kleiner oder gleich","gleich","ungleich",
                     "gr\u00F6\u00DFer oder gleich","gr\u00F6\u00DFer als","enth\u00E4lt","gleicht"};
    
    public OperatorComboBox() {
        super();
        super.setModel(new DefaultComboBoxModel(arr));
    }
}
