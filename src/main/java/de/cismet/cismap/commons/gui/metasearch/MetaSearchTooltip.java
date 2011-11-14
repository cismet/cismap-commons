/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.metasearch;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;

import java.util.Collection;
import java.util.LinkedList;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class MetaSearchTooltip extends PNode {

    //~ Static fields/initializers ---------------------------------------------

    private static final Color COLOR_BACKGROUND = new Color(255, 255, 222, 200);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaSearchTooltip object.
     *
     * @param  searchTopics  DOCUMENT ME!
     */
    public MetaSearchTooltip(final Collection<SearchTopic> searchTopics) {
        final Collection<PNode> nodesToAdd = new LinkedList<PNode>();

        String lblSelectedSearchTopicsText = "";
        if ((searchTopics == null) || searchTopics.isEmpty()) {
            if ((MetaSearch.instance().getSearchTopics() == null)
                        || MetaSearch.instance().getSearchTopics().isEmpty()) {
                lblSelectedSearchTopicsText = NbBundle.getMessage(
                        MetaSearchTooltip.class,
                        "MetaSearchTooltip.MetaSearchTooltip(Collection).lblSelectedSearchTopicsText.notInitialized");
            } else {
                lblSelectedSearchTopicsText = NbBundle.getMessage(
                        MetaSearchTooltip.class,
                        "MetaSearchTooltip.MetaSearchTooltip(Collection).lblSelectedSearchTopicsText.empty");
            }
        } else {
            lblSelectedSearchTopicsText = NbBundle.getMessage(
                    MetaSearchTooltip.class,
                    "MetaSearchTooltip.MetaSearchTooltip(Collection).lblSelectedSearchTopicsText");
        }
        final PText lblSelectedSearchTopics = new PText(lblSelectedSearchTopicsText); // NOI18N

        final Font defaultFont = lblSelectedSearchTopics.getFont();
        final Font boldDefaultFont = new Font(defaultFont.getName(),
                defaultFont.getStyle()
                        + Font.BOLD,
                defaultFont.getSize());
        lblSelectedSearchTopics.setFont(boldDefaultFont);
        lblSelectedSearchTopics.setOffset(5, 5);

        nodesToAdd.add(lblSelectedSearchTopics);

        PNode lastIcon = null;
        PNode lastLabel = lblSelectedSearchTopics;
        final double rowX = lastLabel.getOffset().getX() + 10;
        double rowY = lastLabel.getOffset().getY() + lastLabel.getHeight() + 5;
        double maxWidth = lastLabel.getWidth();
        double totalHeight = lastLabel.getHeight();
        for (final SearchTopic searchTopic : searchTopics) {
            final PImage lblIcon = new PImage(searchTopic.getIcon().getImage());
            lblIcon.setOffset(rowX, rowY);
            lastIcon = lblIcon;

            final PText lblSearchTopic = new PText(searchTopic.getName());
            double textPadding = 0.0;
            if (lastIcon.getHeight() > lblSearchTopic.getHeight()) {
                textPadding = (lastIcon.getHeight() - lblSearchTopic.getHeight()) / 2;
            }
            lblSearchTopic.setOffset(rowX + lastIcon.getWidth() + 5, rowY + textPadding);
            lastLabel = lblSearchTopic;

            nodesToAdd.add(lastIcon);
            nodesToAdd.add(lastLabel);

            rowY = Math.max(lastLabel.getOffset().getY() + lastLabel.getHeight(),
                    lastIcon.getOffset().getY()
                            + lastIcon.getHeight()) + 3;
            maxWidth = Math.max(maxWidth, lastIcon.getWidth() + 5 + lastLabel.getWidth());
            totalHeight += Math.max(lastIcon.getHeight(), lastLabel.getHeight()) + 3;
        }

        maxWidth += 20;
        totalHeight += 10;
        if ((searchTopics != null) && !searchTopics.isEmpty()) {
            totalHeight += 5;
        }

        final PPath background = new PPath(new RoundRectangle2D.Double(0, 0, maxWidth, totalHeight, 10, 10));
        background.setPaint(COLOR_BACKGROUND);

        for (final PNode nodeToAdd : nodesToAdd) {
            background.addChild(nodeToAdd);
        }

        setTransparency(0.85f);
        addChild(background);
    }
}
