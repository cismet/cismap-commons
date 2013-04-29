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
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import de.cismet.cismap.commons.Crs;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.commons.converter.Converter;
import de.cismet.commons.converter.Converter.MatchRating;

import de.cismet.commons.gui.wizard.AbstractWizardPanel;
import de.cismet.commons.gui.wizard.converter.ConverterPreselectionMode;

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

    private final transient ThreadFactory threadFactory;

    private transient String coordinateData;
    private transient String crsName;
    private transient Converter selectedConverter;
    private transient ConverterPreselectionMode converterPreselectionMode;
    private transient List<Converter> availableConverters;
    private transient ScheduledExecutorService detectorExecutor;
    private transient ScheduledFuture<?> currentDetectorTask;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AddGeometriesToMapEnterDataWizardPanel object.
     */
    public AddGeometriesToMapEnterDataWizardPanel() {
        threadFactory = CismetConcurrency.getInstance("cismap-commons")
                    .createThreadFactory("format-detector-scheduler"); // NOI18N
    }

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

        if (ConverterPreselectionMode.AUTO_DETECT == converterPreselectionMode) {
            detectFormat();
        }
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Converter getSelectedConverter() {
        return selectedConverter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedConverter  DOCUMENT ME!
     */
    public void setSelectedConverter(final Converter selectedConverter) {
        this.selectedConverter = selectedConverter;

        changeSupport.fireChange();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConverterPreselectionMode getConverterPreselectionMode() {
        return converterPreselectionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  converterPreselectionMode  DOCUMENT ME!
     */
    public void setConverterPreselectionMode(final ConverterPreselectionMode converterPreselectionMode) {
        this.converterPreselectionMode = converterPreselectionMode;

        changeSupport.fireChange();
    }

    @Override
    public boolean isValid() {
        if ((coordinateData == null) || coordinateData.isEmpty()) {
            wizard.putProperty(
                WizardDescriptor.PROP_INFO_MESSAGE,
                NbBundle.getMessage(
                    AddGeometriesToMapEnterDataWizardPanel.class,
                    "AddGeometriesToMapEnterDataWizardPanel.isValid().infoMessage.enterCoordinateData")); // NOI18N

            return false;
        } else {
            wizard.putProperty(
                WizardDescriptor.PROP_INFO_MESSAGE,
                NbBundle.getMessage(
                    AddGeometriesToMapEnterDataWizardPanel.class,
                    "AddGeometriesToMapEnterDataWizardPanel.isValid().infoMessage.proceed")); // NOI18N

            return true;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void detectFormat() {
        // EDT only
        if (currentDetectorTask != null) {
            currentDetectorTask.cancel(true);
        }

        final Runnable task = new DetectConverterTask();
        currentDetectorTask = detectorExecutor.schedule(task, 300, TimeUnit.MILLISECONDS);
    }

    @Override
    protected Component createComponent() {
        return new AddGeometriesToMapEnterDataVisualPanel(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void read(final WizardDescriptor wizard) {
        // initialise first so that setters work correctly when using auto detect mode
        setConverterPreselectionMode((ConverterPreselectionMode)wizard.getProperty(
                AddGeometriesToMapWizardAction.PROP_CONVERTER_PRESELECT_MODE));
        if (ConverterPreselectionMode.AUTO_DETECT == converterPreselectionMode) {
            availableConverters = (List<Converter>)wizard.getProperty(
                    AddGeometriesToMapWizardAction.PROP_AVAILABLE_CONVERTERS);
            detectorExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        }

        setCoordinateData((String)wizard.getProperty(PROP_COORDINATE_DATA));
        setCrsName(((Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS)).getShortname());
        setSelectedConverter((Converter)wizard.getProperty(
                AddGeometriesToMapChooseConverterWizardPanel.PROP_CONVERTER));
    }

    @Override
    protected void store(final WizardDescriptor wizard) {
        wizard.putProperty(PROP_COORDINATE_DATA, coordinateData);
        wizard.putProperty(AddGeometriesToMapChooseConverterWizardPanel.PROP_CONVERTER, selectedConverter);

        availableConverters = null;
        if (detectorExecutor != null) {
            detectorExecutor.shutdownNow();
            detectorExecutor = null;
        }
    }

    @Override
    public boolean isFinishPanel() {
        return selectedConverter != null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class DetectConverterTask implements Runnable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            Converter highScoreConverter = null;
            int highScoreConverterRating = 0;

            for (final Converter converter : availableConverters) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                if (converter instanceof Converter.MatchRating) {
                    final MatchRating matchRating = (MatchRating)converter;

                    @SuppressWarnings("unchecked")
                    final int converterRating = matchRating.rate(getCoordinateData());
                    if (converterRating > highScoreConverterRating) {
                        highScoreConverterRating = converterRating;
                        highScoreConverter = converter;
                    }
                }
            }

            final Converter detectedConverter = highScoreConverter;

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        setSelectedConverter(detectedConverter);
                    }
                });
        }
    }
}
