/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.image.ColorTintFilter;

/**
 *
 * @author srichter
 */
public final class IconUtils {


    private static Font font = new Font("Courier", Font.PLAIN, 11);//NOI18N

    private IconUtils() {
    }
    public static final int SPACING_X = 5;
    private static final Graphics SAMPLE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();

    public static final Image mergeStringsToIcon(final Image img, final Collection<String> strings, final Color color) {
        if (img != null && strings != null && strings.size() > 0) {            
            final FontMetrics fm = SAMPLE.getFontMetrics(font);
            double maxX = 0.0d;
            double sumY = 0.0d;
            for (final String str : strings) {
                final Rectangle2D rec = fm.getStringBounds(str, SAMPLE);
                if (rec.getWidth() > maxX) {
                    maxX = rec.getWidth();
                }
                sumY += rec.getHeight() * 0.8;
            }
            final float offset = ((float)sumY) / strings.size();
            final int imageX = new Double(img.getWidth(null) + SPACING_X + maxX).intValue() + 1;
            final int imageY = Math.max(img.getHeight(null), new Double(sumY - offset * 0.2).intValue() + 1);
            final BufferedImage bi = new BufferedImage(imageX, imageY, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics2D g2d = (Graphics2D) bi.getGraphics();
//            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(img, 0, 0, null);
            g2d.setColor(color);
            final FontRenderContext frc = g2d.getFontRenderContext();
            final int xPos = img.getWidth(null) + SPACING_X;
            float yPos = offset * 0.8f;
            for (final String str : strings) {
                final TextLayout tl = new TextLayout(str, font, frc);
                tl.draw(g2d, xPos, yPos);
                yPos = yPos + offset;
            }
            return bi;
        }
        return img;
    }

    /**
     * text to add
     *
     * @param img
     * @param numbers
     * @param color
     * @return
     */
    public static final Image mergeNumbersToIcon(final Image img, final Collection<Integer> numbers, final Color color) {
        if (img != null && numbers != null && numbers.size() > 0) {
            final List<Integer> sort = new ArrayList<Integer>(numbers);
            Collections.sort(sort);
            final List<String> str = new ArrayList<String>(sort.size());
            for (final Integer i : sort) {
                str.add(String.valueOf(i));
            }
            return mergeStringsToIcon(img, str, color);
        }
        return img;
    }

    /**
     *
     * @param img
     * @param color
     * @param tint
     * @return
     */
    public static final Image changeImageColor(final Image img, final Color color, float tint) {
        if (img != null && color != null) {
            final BufferedImage src = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
            final BufferedImage dst = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics2D g2d = (Graphics2D) src.getGraphics();
            g2d.drawImage(img, 0, 0, null);
            final ColorTintFilter ctf = new ColorTintFilter(color, tint);
            ctf.filter(src, dst);
            return dst;
        }
        return img;
    }

    /**
     *
     * @param plain
     * @param merged
     * @return
     */
    public static final double calcNewSweepSpotX(double oldSweepX, final Image plain, final Image merged) {
        int oldW = plain.getWidth(null);
        int newW = merged.getWidth(null);

        if (newW > oldW && newW != 0) {
            return (oldSweepX * oldW) / newW;
        } else if (oldW > newW && oldW != 0) {
            return (oldSweepX * newW) / oldW;
        } else {
            return oldSweepX;
        }
    }

    /**
     * 
     * @param plain
     * @param merged
     * @return
     */
    public static final double calcNewSweepSpotY(double oldSweepY, final Image plain, final Image merged) {
        int oldH = plain.getHeight(null);
        int newH = merged.getHeight(null);
        if (newH > oldH && newH != 0) {
            return (oldSweepY * oldH) / newH;
        } else if (newH < oldH && oldH != 0) {
            return (oldSweepY * newH) / oldH;
        } else {
            return oldSweepY;
        }
    }

    public static Font getFont() {
        return font;
    }

    public static void setFont(Font font) {
        IconUtils.font = font;
    }
}
