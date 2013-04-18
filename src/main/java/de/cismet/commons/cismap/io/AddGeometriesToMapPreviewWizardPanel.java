/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import com.vividsolutions.jts.geom.Geometry;

import org.openide.WizardDescriptor;

import de.cismet.commos.gui.wizard.AbstractWizardPanel;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class AddGeometriesToMapPreviewWizardPanel extends AbstractWizardPanel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_GEOMETRY = "__prop_geometry__"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient Geometry geometry;
    private transient boolean busy;
    private transient String statusMessage;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geometry  DOCUMENT ME!
     */
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;

        changeSupport.fireChange();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  busy  DOCUMENT ME!
     */
    public void setBusy(final boolean busy) {
        this.busy = busy;

        changeSupport.fireChange();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  statusMessage  DOCUMENT ME!
     */
    public void setStatusMessage(final String statusMessage) {
        this.statusMessage = statusMessage;

        changeSupport.fireChange();
    }

    @Override
    protected AddGeometriesToMapPreviewVisualPanel createComponent() {
        return new AddGeometriesToMapPreviewVisualPanel(this);
    }

    @Override
    protected void read(final WizardDescriptor wizard) {
        geometry = (Geometry)wizard.getProperty(PROP_GEOMETRY);
    }

    @Override
    protected void store(final WizardDescriptor wizard) {
        wizard.putProperty(PROP_GEOMETRY, geometry);
    }
}
