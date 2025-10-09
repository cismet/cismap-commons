/***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
package de.cismet.commons.cismap.io;

import de.cismet.commons.converter.Converter;
import de.cismet.commons.gui.wizard.converter.AbstractConverterChooseWizardPanel;
import java.util.List;
import org.openide.WizardDescriptor.FinishablePanel;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class AddGeometriesToMapChooseConverterWizardPanel
    extends AbstractConverterChooseWizardPanel
    implements FinishablePanel {

    //~ Methods ----------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public List<Converter> getAvailableConverters() {
        return (List<Converter>) wizard.getProperty(AddGeometriesToMapWizardAction.PROP_AVAILABLE_CONVERTERS);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
