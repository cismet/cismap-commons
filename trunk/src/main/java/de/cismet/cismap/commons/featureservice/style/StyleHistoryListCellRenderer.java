/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * The StyleHistoryListCellRenderer creates a HistoryPanel that provides a decent display
 * for each style inside the StyleHistoryListModel.
 * @author nh
 */
public class StyleHistoryListCellRenderer implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel();
        HistoryPanel histPanel = new HistoryPanel();
        if (value instanceof Style) {
            Style s = (Style) value;
            histPanel.setAttributes(s.isDrawFill() ? s.getFillColor() : null,
                    s.getAlpha(), s.isDrawLine() ? s.getLineColor() : null,
                    s.getLineWidth(),
                    s.isDrawLabel() ? s.getFont() : null,
                    s.getFontColor(),
                    s.getLabel() != null ? s.getLabel() : null);
            histPanel.setCustomSize(new Dimension(45, 20));
            panel.setLayout(new GridBagLayout());
            panel.add(histPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

            // sets default-colors for the selection
            if (isSelected) {
                histPanel.setForeground(list.getSelectionForeground());
                histPanel.setBackground(list.getSelectionBackground());
                panel.setForeground(list.getSelectionForeground());
                panel.setBackground(list.getSelectionBackground());
            } else {
                histPanel.setForeground(list.getForeground());
                histPanel.setBackground(list.getBackground());
                panel.setForeground(list.getForeground());
                panel.setBackground(list.getBackground());
            }
        }
        return panel;
    }

    /**
     * A HistoryPanel serves as displayable component of a Style-object.
     */
    private class HistoryPanel extends JPanel {

        private Color fill, line, label;
        private float alpha;
        private int width;
        private BasicStroke stroke;
        private Font font;
        private String textToShow = "";//NOI18N

        /**
         * Constructor that sets a few default-values.
         */
        public HistoryPanel() {
            setBackground(Color.WHITE);
            stroke = new BasicStroke(0);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color originalColor = g.getColor();
            g.setColor(getBackground());
            ((Graphics2D) g).setStroke(stroke);
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            AlphaComposite c = AlphaComposite.SrcOver.derive(alpha);
            ((Graphics2D) g).setComposite(c);

            if (fill != null) {
                g.setColor(fill);
                g.fillRect(2, 2, getWidth() - 4, getHeight() - 4);
            }
            if (line != null) {
                g.setColor(line);
                ((Graphics2D) g).setStroke(stroke);
                g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            }
            if (font != null) {
                g.setColor(label);
                g.setFont(font);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                FontMetrics metrics = g.getFontMetrics(font);
                int h = metrics.getHeight();
                if (h > getHeight() - 6) {
                    h = getHeight() - 6;
                }
                g.drawString(textToShow, 5, h);
            }
            g.setColor(originalColor);
        }

        /**
         * Changes the size of the component.
         * @param d the new dimension
         */
        public void setCustomSize(Dimension d) {
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        /**
         * Set all paintvariables.
         * @param fill fillingcolor
         * @param alpha transparency
         * @param line linecolor
         * @param width linewidth
         * @param font font of the label
         * @param label fontcolor
         * @param text string of the label to print
         */
        public void setAttributes(Color fill, float alpha, Color line, int width, Font font, Color label, String text) {
            this.fill = fill;
            this.line = line;
            this.alpha = alpha;
            this.width = Double.valueOf(Math.min(2, width)).intValue();
            this.stroke = new BasicStroke(this.width);
            this.font = font;
            this.label = (label == null) ? Color.BLACK : label;
            this.textToShow = text;
        }
    }
}

