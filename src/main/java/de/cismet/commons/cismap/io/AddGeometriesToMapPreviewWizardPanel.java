/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.WizardDescriptor;

import java.awt.EventQueue;

import java.util.concurrent.ExecutorService;

import de.cismet.commons.cismap.io.converters.GeometryConverter;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.commons.converter.ConversionException;
import de.cismet.commons.converter.Converter;

import de.cismet.commons.gui.wizard.AbstractWizardPanel;
import de.cismet.commons.gui.wizard.converter.AbstractConverterChooseWizardPanel;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
// TODO: cancellable
public final class AddGeometriesToMapPreviewWizardPanel extends AbstractWizardPanel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_GEOMETRY = "__prop_geometry__"; // NOI18N

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AddGeometriesToMapPreviewWizardPanel.class);

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
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    AddGeometriesToMapPreviewWizardPanel.this.geometry = geometry;

                    changeSupport.fireChange();
                }
            };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
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
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    AddGeometriesToMapPreviewWizardPanel.this.busy = busy;

                    changeSupport.fireChange();
                }
            };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
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
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    AddGeometriesToMapPreviewWizardPanel.this.statusMessage = statusMessage;

                    changeSupport.fireChange();
                }
            };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    @Override
    protected AddGeometriesToMapPreviewVisualPanel createComponent() {
        return new AddGeometriesToMapPreviewVisualPanel(this);
    }

    @Override
    protected void read(final WizardDescriptor wizard) {
        geometry = (Geometry)wizard.getProperty(PROP_GEOMETRY);

        // TODO: user proper executor
        final ExecutorService executor = CismetConcurrency.getInstance("cismap-commons").getDefaultExecutor();

        executor.execute(new Runnable() {

                @Override
                public void run() {
                    setStatusMessage("Converting data");
                    setBusy(true);

                    final Converter converter = (Converter)wizard.getProperty(
                            AbstractConverterChooseWizardPanel.PROP_CONVERTER);
                    final Object data = wizard.getProperty(AddGeometriesToMapEnterDataWizardPanel.PROP_COORDINATE_DATA);
                    final String epsgCode = (String)wizard.getProperty(
                            AddGeometriesToMapWizardAction.PROP_CURRENT_EPSG_CODE);

                    assert converter instanceof GeometryConverter : "illegal wizard initialisation"; // NOI18N

                    final GeometryConverter geomConverter = (GeometryConverter)converter;
                    try {
                        @SuppressWarnings("unchecked")
                        final Geometry geom = geomConverter.convertForward(data, epsgCode);

                        setStatusMessage("Convertion finished successfully");

                        setGeometry(geom);
                    } catch (final ConversionException ex) {
                        LOG.error("cannot convert geometry: [converter=" + geomConverter + "|data=" + data + "]", ex); // NOI18N
                        setStatusMessage("Error while converting data: " + ex.getLocalizedMessage());                  // NOI18N
                    } finally {
                        setBusy(false);
                    }
                }
            });
    }

    @Override
    protected void store(final WizardDescriptor wizard) {
        wizard.putProperty(PROP_GEOMETRY, geometry);
    }
}
