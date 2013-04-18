/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;

import java.util.List;

import de.cismet.commons.converter.Converter;

import de.cismet.commos.gui.wizard.converter.AbstractConverterChooseWizardPanel;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class AddGeometriesToMapChooseConverterWizardPanel extends AbstractConverterChooseWizardPanel
        implements FinishablePanel {

    //~ Instance fields --------------------------------------------------------

    private transient List<Converter> availableConverters;

    //~ Methods ----------------------------------------------------------------

    @Override
    public List<Converter> getAvailableConverters() {
        return availableConverters;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void read(final WizardDescriptor wizard) {
        availableConverters = (List<Converter>)wizard.getProperty(
                AddGeometriesToMapWizardAction.PROP_AVAILABLE_CONVERTERS);

        super.read(wizard);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
