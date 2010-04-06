/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author nh
 */
public class PointSymbolListRenderer implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        if (value instanceof String) {
            try {
                label.setIcon(new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/pointsymbols/"+value)));//NOI18N
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
