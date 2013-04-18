
package de.cismet.commons.cismap.io;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.commos.gui.wizard.AbstractWizardPanel;
import org.openide.WizardDescriptor;

/**
 *
 * @author martin.scholl@cismet.de
 */
public final class AddGeometriesToMapPreviewWizardPanel extends AbstractWizardPanel
{
    public static final String PROP_GEOMETRY = "__prop_geometry__"; // NOI18N
    
    private transient Geometry geometry;
    private transient boolean busy;
    private transient String statusMessage;

    public Geometry getGeometry()
    {
        return geometry;
    }

    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
        
        changeSupport.fireChange();
    }

    public boolean isBusy()
    {
        return busy;
    }

    public void setBusy(boolean busy)
    {
        this.busy = busy;
        
        changeSupport.fireChange();
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
        
        changeSupport.fireChange();
    }
    
    @Override
    protected AddGeometriesToMapPreviewVisualPanel createComponent()
    {
        return new AddGeometriesToMapPreviewVisualPanel(this);
    }

    @Override
    protected void read(WizardDescriptor wizard)
    {
        geometry = (Geometry)wizard.getProperty(PROP_GEOMETRY);
    }

    @Override
    protected void store(WizardDescriptor wizard)
    {
        wizard.putProperty(PROP_GEOMETRY, geometry);
    }

}
