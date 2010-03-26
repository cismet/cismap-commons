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
    String[] arr =  {
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.lessThan"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.lessThanOrEqual"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.equal"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.notEqual"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.greaterThanOrEqual"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.greaterThan"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.contains"),
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.isLike")};
    
    public OperatorComboBox() {
        super();
        super.setModel(new DefaultComboBoxModel(arr));
    }
}
