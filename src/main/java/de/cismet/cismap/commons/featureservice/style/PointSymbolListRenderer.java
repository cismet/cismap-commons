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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class PointSymbolListRenderer implements ListCellRenderer {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus) {
        final JLabel label = new JLabel();
        label.setOpaque(true);
        if (value instanceof String) {
            try {
                label.setIcon(new ImageIcon(
                        getClass().getResource("/de/cismet/cismap/commons/featureservice/res/pointsymbols/" + value))); // NOI18N
            } catch (Exception ex) {
                label.setText((String)value);
            }
        }

        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
        } else {
            label.setBackground(list.getBackground());
        }
        return label;
    }
}
