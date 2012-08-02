package de.cismet.cismap.commons.gui.options;

import de.cismet.lookupoptions.AbstractOptionsCategory;
import de.cismet.lookupoptions.OptionsCategory;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * @author jruiz
 */
@ServiceProvider(service = OptionsCategory.class)
public class CismapOptionsCategory extends AbstractOptionsCategory {

    @Override
    public String getName() {
        return "Cismap";
    }

    @Override
    public Icon getIcon() {
        Image image = ImageUtilities.loadImage("de/cismet/cismap/commons/gui/options/cismap.png");
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
}
