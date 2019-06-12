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
import org.openide.util.NbBundle;

import java.awt.EventQueue;

import java.util.concurrent.ExecutorService;

import de.cismet.cismap.commons.Crs;

import de.cismet.commons.cismap.io.converters.GeometryConverter;
import de.cismet.commons.cismap.io.converters.MultiGeometriesProvider;

import de.cismet.commons.concurrency.CismetConcurrency;

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
    private transient String previewUrl;
    private transient Crs currentCrs;
    private transient boolean multipleGeometries;

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
     * @return  DOCUMENT ME!
     */
    public boolean hasMultipleGeometries() {
        return multipleGeometries;
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreviewUrl() {
        return previewUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  previewUrl  DOCUMENT ME!
     */
    public void setPreviewUrl(final String previewUrl) {
        this.previewUrl = previewUrl;

        changeSupport.fireChange();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Crs getCurrentCrs() {
        return currentCrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  currentCrs  DOCUMENT ME!
     */
    public void setCurrentCrs(final Crs currentCrs) {
        this.currentCrs = currentCrs;

        changeSupport.fireChange();
    }

    @Override
    protected AddGeometriesToMapPreviewVisualPanel createComponent() {
        return new AddGeometriesToMapPreviewVisualPanel(this);
    }

    @Override
    protected void read(final WizardDescriptor wizard) {
        geometry = (Geometry)wizard.getProperty(PROP_GEOMETRY);
        previewUrl = (String)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_PREVIEW_GETMAP_URL);
        currentCrs = (Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS);

        // TODO: user proper executor
        final ExecutorService executor = CismetConcurrency.getInstance("cismap-commons").getDefaultExecutor(); // NOI18N

        executor.execute(new Thread("AddGeometriesToMapPreviewWizardPanel read()") {

                @Override
                public void run() {
                    setGeometry(null);
                    multipleGeometries = false;
                    setStatusMessage(
                        NbBundle.getMessage(
                            AddGeometriesToMapPreviewWizardPanel.class,
                            "AddGeometriesToMapPreviewWizardPanel.read(WizardDescriptor).runnable.statusMessage.convertingData")); // NOI18N
                    setBusy(true);

                    final Converter converter = (Converter)wizard.getProperty(
                            AbstractConverterChooseWizardPanel.PROP_CONVERTER);
                    final Object data = wizard.getProperty(AddGeometriesToMapEnterDataWizardPanel.PROP_COORDINATE_DATA);
                    final Crs crs = (Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS);

                    assert converter instanceof GeometryConverter : "illegal wizard initialisation"; // NOI18N

                    final GeometryConverter geomConverter = (GeometryConverter)converter;
                    try {
                        @SuppressWarnings("unchecked")
                        final Geometry geom = geomConverter.convertForward(data, crs.getCode());

                        setStatusMessage(
                            NbBundle.getMessage(
                                AddGeometriesToMapPreviewWizardPanel.class,
                                "AddGeometriesToMapPreviewWizardPanel.read(WizardDescriptor).runnable.statusMessage.conversionSuccessful")); // NOI18N

                        setGeometry(geom);

                        multipleGeometries = (geomConverter instanceof MultiGeometriesProvider);
                    } catch (final Exception ex) {
                        LOG.error("cannot convert geometry: [converter=" + geomConverter + "|data=" + data + "]", ex);             // NOI18N
                        setStatusMessage(
                            NbBundle.getMessage(
                                AddGeometriesToMapPreviewWizardPanel.class,
                                "AddGeometriesToMapPreviewWizardPanel.read(WizardDescriptor).runnable.statusMessage.convertError", // NOI18N
                                ex.getLocalizedMessage()));
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
