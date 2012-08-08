/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.options;

import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.lookupoptions.AbstractOptionsCategory;
import de.cismet.lookupoptions.OptionsCategory;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = OptionsCategory.class)
public class CismapOptionsCategory extends AbstractOptionsCategory {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(CismapOptionsCategory.class, "CismapOptionsCategory.name"); // NOI18N
    }

    @Override
    public Icon getIcon() {
        final Image image = ImageUtilities.loadImage("de/cismet/cismap/commons/gui/options/cismap.png");
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getTooltip() {
        return org.openide.util.NbBundle.getMessage(CismapOptionsCategory.class, "CismapOptionsCategory.tooltip"); // NOI18N
    }
}
