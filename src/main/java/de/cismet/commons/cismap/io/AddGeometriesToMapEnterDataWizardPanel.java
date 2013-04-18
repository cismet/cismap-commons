
package de.cismet.commons.cismap.io;

import de.cismet.commos.gui.wizard.AbstractWizardPanel;
import java.awt.Component;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class AddGeometriesToMapEnterDataWizardPanel extends AbstractWizardPanel implements FinishablePanel
{
    public static final String PROP_COORDINATE_DATA = "__prop_coordinate_data__"; // NOI18N
    
    private transient String coordinateData;

    public String getCoordinateData()
    {
        return coordinateData;
    }

    public void setCoordinateData(String coordinateData)
    {
        this.coordinateData = coordinateData;
        
        changeSupport.fireChange();
    }

    @Override
    public boolean isValid()
    {
        if(coordinateData == null || coordinateData.isEmpty()){
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, "Please enter coordinate data");
            
            return false;
        } else {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, "Proceed to next step");
            
            return true;
        }
    }
    
    private void guessFormat(){
        // TODO
    }
    
    @Override
    protected Component createComponent()
    {
        return new AddGeometriesToMapEnterDataVisualPanel(this);
    }

    @Override
    protected void read(WizardDescriptor wizard)
    {
        setCoordinateData((String)wizard.getProperty(PROP_COORDINATE_DATA));
    }

    @Override
    protected void store(WizardDescriptor wizard)
    {
        wizard.putProperty(PROP_COORDINATE_DATA, coordinateData);
    }

    @Override
    public boolean isFinishPanel()
    {
        return true;
    }

}
