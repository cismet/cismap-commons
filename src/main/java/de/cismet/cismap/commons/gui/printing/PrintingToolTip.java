/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.printing;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;

/**
 * de.cismet.cismap.commons.gui.printing.PrintingToolTip.PrintingToolTip().
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PrintingToolTip extends PNode {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PrintingToolTip.
     *
     * @param  backgroundColor  DOCUMENT ME!
     */
    public PrintingToolTip(final Color backgroundColor) {
        final PImage image = new PImage(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/res/frameprint64.png")).getImage()); // NOI18N
        image.setOffset(10, 10);
        final PText t1 = new PText(org.openide.util.NbBundle.getMessage(
                    PrintingToolTip.class,
                    "PrintingToolTip.PrintingToolTip(Color).t1"));                                             // NOI18N

        final Font defaultFont = t1.getFont();
        final Font boldDefaultFont = new Font(defaultFont.getName(),
                defaultFont.getStyle()
                        + Font.BOLD,
                defaultFont.getSize());
        t1.setFont(boldDefaultFont);
        final PText t2 = new PText(org.openide.util.NbBundle.getMessage(
                    PrintingToolTip.class,
                    "PrintingToolTip.PrintingToolTip(Color).t2")); // NOI18N
        final PText t3 = new PText(org.openide.util.NbBundle.getMessage(
                    PrintingToolTip.class,
                    "PrintingToolTip.PrintingToolTip(Color).t3")); // NOI18N
        final PText t4 = new PText(org.openide.util.NbBundle.getMessage(
                    PrintingToolTip.class,
                    "PrintingToolTip.PrintingToolTip(Color).t4")); // NOI18N

        final double textHeight = t1.getHeight() + 5 + t2.getHeight() + 5 + t3.getHeight() + 5 + t4.getHeight();
        final double textWidth = Math.max(Math.max(t1.getWidth(), t2.getWidth()),
                Math.max(t3.getWidth(), t4.getWidth()));

        final double backgroundHeight = Math.max(textHeight, image.getHeight());

        final PPath background = new PPath(new RoundRectangle2D.Double(
                    0,
                    0,
                    10
                            + image.getWidth()
                            + textWidth
                            + 10,
                    5
                            + backgroundHeight
                            + 5,
                    10,
                    10));
        background.setPaint(backgroundColor);
        background.addChild(image);
        background.addChild(t1);
        background.addChild(t2);
        background.addChild(t3);
        background.addChild(t4);
        t1.setOffset(image.getWidth() + 5 + 10, 5);
        t2.setOffset(t1.getOffset().getX(), t1.getOffset().getY() + 5 + t1.getHeight());
        t3.setOffset(t1.getOffset().getX(), t2.getOffset().getY() + 5 + t2.getHeight());
        t4.setOffset(t1.getOffset().getX(), t3.getOffset().getY() + 5 + t3.getHeight());
        setTransparency(0.85f);
        addChild(background);
    }
}
