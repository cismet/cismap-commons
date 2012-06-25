/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;

import java.util.Collection;
import java.util.LinkedList;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class InvalidPolygonTooltip extends PNode {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum Mode {

        //~ Enum constants -----------------------------------------------------

        SELECT_FEATURE, HOLE_ERROR, ENTITY_ERROR
    }

    //~ Instance fields --------------------------------------------------------

    private final Color COLOR_BACKGROUND = new Color(255, 255, 222, 200);
    private final String[] selectContent = {
            "Es muss genau eine Geometrie",
            "selektiert sein. Selektieren",
            "Sie jetzt eine Geometrie",
            "indem Sie [alt] gedrückt",
            "halten, während sie auf die",
            "gewünschte Geometrie",
            "klicken."
        };
    private final String[] holeContent = {
            "Löcher müssen vollständig in",
            "ihrem eigenen Teil-Polygon",
            "liegen, und dürfen andere",
            "Teil-Polygone oder Löcher",
            "nicht berühren."
        };
    private final String[] entityContent = {
            "Teil-Polygone dürfen keine",
            "anderen Teil-Polygone",
            "berühren. Sie dürfen dabei",
            "durchaus in Löchern liegen."
        };

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TransformationTooltip object.
     */
    public InvalidPolygonTooltip() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    public void setMode(final Mode mode) {
        removeAllChildren();

        String title = "";
        String[] content = new String[] {};

        switch (mode) {
            case SELECT_FEATURE: {
                content = selectContent;
            }
            break;
            case HOLE_ERROR: {
                title = "Ungültiges MultiPolygon";
                content = holeContent;
            }
            break;
            case ENTITY_ERROR: {
                title = "Ungültiges MultiPolygon";
                content = entityContent;
            }
            break;
        }

        final PText defaultPText = new PText();

        final Font defaultFont = defaultPText.getFont();
        final Font boldDefaultFont = new Font(defaultFont.getName(),
                defaultFont.getStyle()
                        + Font.BOLD,
                defaultFont.getSize());

        final PText pTextTitle = new PText(title);
        pTextTitle.setOffset(5, 5);
        pTextTitle.setFont(boldDefaultFont);

        final Collection<PNode> nodesToAdd = new LinkedList<PNode>();
        nodesToAdd.add(pTextTitle);

        PNode lastLabel = pTextTitle;
        double maxHeight = 5 + lastLabel.getOffset().getY() + lastLabel.getHeight();
        double maxWidth = lastLabel.getWidth();
        for (final String text : content) {
            final PText pTextContent = new PText(text);
            pTextContent.setOffset(10, maxHeight);
            nodesToAdd.add(pTextContent);
            lastLabel = pTextContent;
            maxHeight = 3 + lastLabel.getOffset().getY() + lastLabel.getHeight();
            maxWidth = Math.max(maxWidth, 10 + lastLabel.getWidth());
        }
        maxWidth += 10;
        maxHeight = maxHeight - 3 + 10;

        final PPath background = new PPath(new RoundRectangle2D.Double(0, 0, maxWidth, maxHeight, 10, 10));
        background.setPaint(COLOR_BACKGROUND);

        for (final PNode nodeToAdd : nodesToAdd) {
            background.addChild(nodeToAdd);
        }

        setTransparency(0.85f);
        addChild(background);
    }
}
