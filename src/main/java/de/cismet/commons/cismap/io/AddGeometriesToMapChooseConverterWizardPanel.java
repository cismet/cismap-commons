
package de.cismet.commons.cismap.io;

import de.cismet.commons.converter.Converter;
import de.cismet.commos.gui.wizard.converter.AbstractConverterChooseWizardPanel;
import java.util.List;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class AddGeometriesToMapChooseConverterWizardPanel extends AbstractConverterChooseWizardPanel implements FinishablePanel
{
    private transient List<Converter> availableConverters;
    
    @Override
    public List<Converter> getAvailableConverters()
    {
        return availableConverters;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void read(WizardDescriptor wizard)
    {
        super.read(wizard);
        
        availableConverters = (List<Converter>)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_AVAILABLE_CONVERTERS);
    }
    
    @Override
    public boolean isFinishPanel()
    {
        return true;
    }
}
