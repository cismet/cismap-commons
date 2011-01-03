/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * DOCUMENT ME!
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class OperatorComboBox extends JComboBox {

    //~ Instance fields --------------------------------------------------------

    String[] arr = {
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.lessThan"),           // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.lessThanOrEqual"),    // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.equal"),              // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.notEqual"),           // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.greaterThanOrEqual"), // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.greaterThan"),        // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.contains"),           // NOI18N
            org.openide.util.NbBundle.getMessage(OperatorComboBox.class, "OperatorComboBox.arr.isLike")
        };                                                                                                           // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new OperatorComboBox object.
     */
    public OperatorComboBox() {
        super();
        super.setModel(new DefaultComboBoxModel(arr));
    }
}
