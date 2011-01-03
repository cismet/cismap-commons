/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*

 * StylePreviewPanel.java

 *

 * Created on 25. Februar 2008, 14:31

 */
package de.cismet.cismap.commons.featureservice.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.tools.gui.PointSymbolCreator;

/**
 * The StylePreviewPanel is a JPanel that gives the user a visual feedback of the currently selected or created style of
 * the StyleDialog by painting a square and the pointsymbol beside it. It must to be configured with a Style and a
 * FeatureAnnotationSymbol.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class StylePreviewPanel extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final String SAMPLE_TEXT = org.openide.util.NbBundle.getMessage(
            StylePreviewPanel.class,
            "StylePreviewPanel.SAMPLE_TEXT"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Style style;
    private Color lineColor;
    private Color fillColor;
    private Image pointSymbol = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form StylePreviewPanel.
     */
    public StylePreviewPanel() {
        initComponents();
        if (log.isDebugEnabled()) {
            log.debug("Create StylePreviewPanel"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Reassigns the variables of this StylePreviewPanel and calls a repaint.
     *
     * @param  style        Style with all attributes to display
     * @param  pointSymbol  FeatureAnnotationSymbol or null
     */
    public void update(final Style style, final FeatureAnnotationSymbol pointSymbol) {
        this.style = style;

        this.fillColor = style.isDrawFill() ? style.getFillColor() : null;

        this.lineColor = style.isDrawLine() ? style.getLineColor() : null;

        this.pointSymbol = ((pointSymbol == null) ? createPointSymbol() : pointSymbol.getImage());

        repaint();
    }

    /**
     * Returns the pointsymbol that is shown in this StylePreviewPanel.
     *
     * @return  delivered pointsymbol-image or own created
     */
    public Image getPointSymbol() {
        return pointSymbol;
    }

    /**
     * Creates a new pointsymbol-image with attributes of the current style.
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage createPointSymbol() {
        if (log.isDebugEnabled()) {
            log.debug("createPointSymbol: PointSymbolSize=" + style.getPointSymbolSize() + ", LineWidth="
                        + style.getLineWidth()); // NOI18N
        }
        return PointSymbolCreator.createPointSymbol(style.isDrawLine(),
                style.isDrawFill(),
                (style.getPointSymbolSize() > Style.MIN_POINTSYMBOLSIZE) ? style.getPointSymbolSize()
                                                                         : Style.MIN_POINTSYMBOLSIZE,
                style.getLineWidth(),
                fillColor,
                lineColor);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, style.getAlpha()));

        final int[] pointsX = { 10, getWidth() - 50, getWidth() - 10, 40 };

        final int[] pointsY = { getHeight() / 3, 10, getHeight() / 2, getHeight() - 20 };

        // filling

        if (style.isDrawFill()) {
            g2d.setColor(fillColor);

            g2d.fillPolygon(pointsX, pointsY, 4);
        }

        // line

        if (style.isDrawLine()) {
            g2d.setColor(lineColor);

            g2d.setStroke(new BasicStroke(style.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2d.drawPolygon(pointsX, pointsY, 4);
        }

        // labelling

        if (style.isDrawLabel()) {
            g2d.setColor(style.getFontColor());

            g2d.setFont(style.getFont());

            g2d.drawString(SAMPLE_TEXT, getWidth() / 3, getHeight() / 2);
        }

        // Pointsymbol

        g2d.drawImage(
            pointSymbol,
            getWidth()
                    - (pointSymbol.getWidth(null) / 2)
                    - 40,
            getHeight()
                    - (pointSymbol.getWidth(null) / 2)
                    - 20,
            null);

        g2d.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents

    private void initComponents() {
        setBackground(new java.awt.Color(255, 255, 255));

        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        setMinimumSize(new java.awt.Dimension(50, 50));

        setLayout(new java.awt.BorderLayout());
    } // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables

    // End of variables declaration//GEN-END:variables
}
