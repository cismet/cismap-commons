/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.style;

import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author nh
 */
public class OperatorComboBox extends JComboBox {
    
    private static final ResourceBundle I18N = ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");

    String[] arr =  {
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.lessThan"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.lessThanOrEqual"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.equal"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.notEqual"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.greaterThanOrEqual"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.greaterThan"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.contains"),
        I18N.getString("de.cismet.cismap.commons.featureservice.style.OperatorComboBox.arr.isLike")};
    
    public OperatorComboBox() {
        super();
        super.setModel(new DefaultComboBoxModel(arr));
    }
}
