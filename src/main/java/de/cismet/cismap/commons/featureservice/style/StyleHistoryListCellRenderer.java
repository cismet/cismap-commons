/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

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
import org.jdom.Element;

/**
 *
 * @author nh
 */
public class StyleHistoryListCellRenderer implements ListCellRenderer {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel();
        HistoryPanel histPanel = new HistoryPanel();
        if (value instanceof BasicStyle) {
            BasicStyle s = (BasicStyle) value;
            histPanel.setAttributes(s.isPaintFill() ? s.getFillColor() : null, s.isPaintLine() ? s.getLineColor() : null,
                    s.getLineWidth(), s.isPaintLabel() ? s.getFont() : null, s.getFontColor(), s.getAttribute());
            histPanel.setCustomSize(new Dimension(45, 20));
            panel.setLayout(new GridBagLayout());
            panel.add(histPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

            // Setzt die Standardfarben bei einer Selektion
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
     * HistoryPanel dient der Anzeige eines Styles in der Historyliste.
     */
    private class HistoryPanel extends JPanel {
        private Color fill,  line,  label;
        private int width;
        private BasicStroke stroke;
        private Font font;
        private String textToShow = "";

        /**
         * Konstruktor, setzt Hintergrund auf weiß und die Linie = 0
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
         * Setzt die Größe fest auf die übergebene Dimension.
         * @param d neue Größe
         */
        public void setCustomSize(Dimension d) {
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        /**
         * Setzt alle nötigen Zeichenattribute.
         * @param fill Füllung des Panels
         * @param line Linie um das Panel
         * @param width Dicke der Linie
         * @param font Font des anzuzeigenden Labels
         * @param label FontColor
         * @param text primaryAnnotationAttribute-Wert
         */
        public void setAttributes(Color fill, Color line, int width, Font font, Color label, String text) {
            this.fill = fill;
            this.line = line;
            this.width = new Double(Math.min(2, width)).intValue();
            this.stroke = new BasicStroke(this.width);
            this.font = font;
            this.label = (label == null) ? Color.BLACK : label;
            this.textToShow = text;
        }
    }
}
