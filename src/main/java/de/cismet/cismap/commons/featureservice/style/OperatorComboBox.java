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
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.lessThan"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.lessThanOrEqual"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.equal"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.notEqual"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.greaterThanOrEqual"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.greaterThan"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.contains"),//NOI18N
        org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.isLike")};//NOI18N
    
    public OperatorComboBox() {
        super();
        super.setModel(new DefaultComboBoxModel(arr));
    }
}
