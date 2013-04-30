/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import org.apache.log4j.Logger;

import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.Crs;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

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

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AddGeometriesToMapEnterDataWizardPanel.class);

    public static final String PROP_COORDINATE_DATA = "__prop_coordinate_data__"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient ExecutorService dispatcher;
    private final transient ThreadFactory threadFactory;
    private final transient PropertyChangeSupport propCSupport;

    // -- properties
    private transient File inputFile;
    private transient String coordinateData;
    private transient String crsName;
    private transient Converter selectedConverter;
    private transient ConverterPreselectionMode converterPreselectionMode;
    // -- properties

    private transient List<Converter> availableConverters;
    private transient ScheduledExecutorService detectorExecutor;
    private transient ScheduledFuture<?> currentDetectorTask;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AddGeometriesToMapEnterDataWizardPanel object.
     */
    public AddGeometriesToMapEnterDataWizardPanel() {
        threadFactory =
            CismetConcurrency.getInstance("cismap-commons")                               // NOI18N
            .createThreadFactory("AddGeometriesToMapEnterDataWizardPanel-threadfactory"); // NOI18N
        dispatcher = CismetExecutors.newSingleThreadExecutor(threadFactory);
        propCSupport = new PropertyChangeSupport(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public File getInputFile() {
        return inputFile;
    }

    /**
     * Sets the inputfile and reads it. if it is a valid file it sets the coordinate data accordingly.
     *
     * @param  inputFile  the input file to read from
     */
    public void setInputFile(final File inputFile) {
        final File oldData = this.inputFile;

        this.inputFile = inputFile;

        changeSupport.fireChange();
        propCSupport.firePropertyChange("inputFile", oldData, this.inputFile); // NOI18N

        processInputFile(inputFile);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   inputFile  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void processInputFile(final File inputFile) {
        dispatcher.execute(new SwingWorker<String, Void>() {

                @Override
                protected String doInBackground() throws Exception {
                    if (inputFile != null) {
                        if (inputFile.isFile() && inputFile.canRead()) {
                            BufferedReader fileReader = null;
                            try {
                                // possible encoding issues
                                fileReader = new BufferedReader(new FileReader(inputFile));
                                final StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = fileReader.readLine()) != null) {
                                    sb.append(line).append('\n');
                                }

                                if (sb.length() > 0) {
                                    sb.deleteCharAt(sb.length() - 1);
                                }

                                return sb.toString();
                            } catch (final FileNotFoundException ex) {
                                throw new IllegalStateException(
                                    "file was present and readable, but now is not anymore: "
                                            + inputFile,
                                    ex);                                                       // NOI18N
                            } catch (final IOException ex) {
                                LOG.warn("cannot read input file", ex);                        // NOI18N
                            } finally {
                                if (fileReader != null) {
                                    try {
                                        fileReader.close();
                                    } catch (final IOException ex) {
                                        LOG.warn("cannot close input file: " + inputFile, ex); // NOI18N
                                    }
                                }
                            }
                        }
                    }

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        final String fileData = get(300, TimeUnit.MILLISECONDS);

                        if (fileData != null) {
                            setCoordinateData(fileData);
                        }
                    } catch (final Exception ex) {
                        LOG.warn("cannot fetch result data from worker", ex); // NOI18N
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pcl  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener pcl) {
        propCSupport.addPropertyChangeListener(pcl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  property  DOCUMENT ME!
     * @param  pcl       DOCUMENT ME!
     */
    public void addPropertyChangeListener(final String property, final PropertyChangeListener pcl) {
        propCSupport.addPropertyChangeListener(property, pcl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pcl  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener pcl) {
        propCSupport.removePropertyChangeListener(pcl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  property  DOCUMENT ME!
     * @param  pcl       DOCUMENT ME!
     */
    public void removePropertyChangeListener(final String property, final PropertyChangeListener pcl) {
        propCSupport.removePropertyChangeListener(property, pcl);
    }

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
        final String oldData = this.coordinateData;

        this.coordinateData = coordinateData;

        changeSupport.fireChange();
        propCSupport.firePropertyChange("coordinateData", oldData, this.coordinateData); // NOI18N

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
        final String oldData = this.crsName;

        this.crsName = crsName;

        changeSupport.fireChange();
        propCSupport.firePropertyChange("crsName", oldData, this.crsName); // NOI18N
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
        final Converter oldData = this.selectedConverter;

        this.selectedConverter = selectedConverter;

        changeSupport.fireChange();
        propCSupport.firePropertyChange("selectedConverter", oldData, this.selectedConverter); // NOI18N
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
        final ConverterPreselectionMode oldData = this.converterPreselectionMode;

        this.converterPreselectionMode = converterPreselectionMode;

        changeSupport.fireChange();
        propCSupport.firePropertyChange("converterPreselectionMode", oldData, this.converterPreselectionMode); // NOI18N
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
        setInputFile((File)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_INPUT_FILE));
        setCrsName(((Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS)).getShortname());
        setSelectedConverter((Converter)wizard.getProperty(
                AddGeometriesToMapChooseConverterWizardPanel.PROP_CONVERTER));
    }

    @Override
    protected void store(final WizardDescriptor wizard) {
        wizard.putProperty(PROP_COORDINATE_DATA, coordinateData);
        wizard.putProperty(AddGeometriesToMapChooseConverterWizardPanel.PROP_CONVERTER, selectedConverter);
        // we do not save the input file that has probably been choosen so that any manual alteration of the file
        // content is preserved and not overridden again through the input file
        wizard.putProperty(AddGeometriesToMapWizardAction.PROP_INPUT_FILE, null);

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
                    final int converterRating = matchRating.rate(
                            getCoordinateData(),
                            ((Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS)).getCode());
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
