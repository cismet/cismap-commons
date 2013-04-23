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

import java.awt.Component;

import de.cismet.cismap.commons.Crs;

import de.cismet.commons.gui.wizard.AbstractWizardPanel;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class AddGeometriesToMapEnterDataWizardPanel extends AbstractWizardPanel implements FinishablePanel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_COORDINATE_DATA = "__prop_coordinate_data__"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient String coordinateData;
    private transient String crsName;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCoordinateData() {
        return coordinateData;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinateData  DOCUMENT ME!
     */
    public void setCoordinateData(final String coordinateData) {
        this.coordinateData = coordinateData;

        changeSupport.fireChange();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCrsName() {
        return crsName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crsName  DOCUMENT ME!
     */
    public void setCrsName(final String crsName) {
        this.crsName = crsName;

        changeSupport.fireChange();
    }

    @Override
    public boolean isValid() {
        if ((coordinateData == null) || coordinateData.isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, "Please enter coordinate data");

            return false;
        } else {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, "Proceed to next step");

            return true;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void guessFormat() {
        // TODO
    }

    @Override
    protected Component createComponent() {
        return new AddGeometriesToMapEnterDataVisualPanel(this);
    }

    @Override
    protected void read(final WizardDescriptor wizard) {
        setCoordinateData((String)wizard.getProperty(PROP_COORDINATE_DATA));
        setCrsName(((Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS)).getShortname());
    }

    @Override
    protected void store(final WizardDescriptor wizard) {
        wizard.putProperty(PROP_COORDINATE_DATA, coordinateData);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
