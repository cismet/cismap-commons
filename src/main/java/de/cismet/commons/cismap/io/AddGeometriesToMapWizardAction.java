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

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import org.jdom.Element;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.PureNewFeature.geomTypes;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeometryConverter;
import de.cismet.commons.cismap.io.converters.TextToGeometryConverter;

import de.cismet.commons.converter.ConversionException;
import de.cismet.commons.converter.Converter;

import de.cismet.commons.gui.wizard.converter.AbstractConverterChooseWizardPanel;
import de.cismet.commons.gui.wizard.converter.ConverterPreselectionMode;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class AddGeometriesToMapWizardAction extends AbstractAction implements DropTargetListener, Configurable {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_AVAILABLE_CONVERTERS = "__prop_available_converters__";         // NOI18N
    public static final String PROP_INPUT_FILE = "__prop_input_file__";                             // NOI18N
    public static final String PROP_CURRENT_CRS = "__prop_current_epsg_code__";                     // NOI18N
    public static final String PROP_PREVIEW_GETMAP_URL = "__prop_preview_getmap_url__";             // NOI18N
    public static final String PROP_CONVERTER_PRESELECT_MODE = "__prop_converter_preselect_mode__"; // NOI18N

    public static final String CONF_SECTION = "addGeometriesToMapWizardAction";   // NOI18N
    public static final String CONF_CONV_PRESELECT = "converterPreselectionMode"; // NOI18N
    public static final String CONF_PREVIEW_GETMAP_URL = "previewGetMapUrl";      // NOI18N

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(AddGeometriesToMapWizardAction.class);

    //~ Instance fields --------------------------------------------------------

    private transient Converter selectedConverter;

    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;

    private transient ConverterPreselectionMode converterPreselectionMode;
    private transient String previewGetMapUrl;
    private transient File inputFile;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AddGeometriesToMapWizardAction object.
     */
    public AddGeometriesToMapWizardAction() {
        super(
            "",                                   // NOI18N
            ImageUtilities.loadImageIcon(
                AddGeometriesToMapWizardAction.class.getPackage().getName().replace('.', '/')
                        + "/new_geom_wiz_22.png", // NOI18N
                false));

        putValue(
            Action.SHORT_DESCRIPTION,
            NbBundle.getMessage(
                AddGeometriesToMapWizardAction.class,
                "AddGeometriesToMapWizardAction.<init>.action.shortDescription")); // NOI18N

        setConverterPreselectionMode(getDefaultConverterPreselectionMode());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        assert EventQueue.isDispatchThread() : "can only be called from EDT"; // NOI18N

        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                    new AddGeometriesToMapEnterDataWizardPanel(),
                    new AddGeometriesToMapChooseConverterWizardPanel(),
                    new AddGeometriesToMapPreviewWizardPanel()
                };

            final String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                final Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    // assume Swing components
                    final JComponent jc = (JComponent)c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the
                    // background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }

        return panels;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.setTitleFormat(new MessageFormat("{0}"));                                      // NOI18N
        wizard.setTitle(NbBundle.getMessage(
                AddGeometriesToMapWizardAction.class,
                "AddGeometriesToMapWizardAction.actionPerformed(ActionEvent).wizard.title")); // NOI18N

        final Collection<? extends TextToGeometryConverter> availableConverters = Lookup.getDefault()
                    .lookupAll(TextToGeometryConverter.class);

        final ConverterPreselectionMode preselectionMode;
        if (ConverterPreselectionMode.DEFAULT == getConverterPreselectionMode()) {
            preselectionMode = getDefaultConverterPreselectionMode();
        } else {
            preselectionMode = getConverterPreselectionMode();
        }

        wizard.putProperty(AddGeometriesToMapPreviewWizardPanel.PROP_GEOMETRY, null);
        wizard.putProperty(AddGeometriesToMapChooseConverterWizardPanel.PROP_CONVERTER, selectedConverter);
        wizard.putProperty(PROP_PREVIEW_GETMAP_URL, getPreviewGetMapUrl());
        wizard.putProperty(PROP_AVAILABLE_CONVERTERS, new ArrayList<Converter>(availableConverters));
        wizard.putProperty(PROP_CURRENT_CRS, CismapBroker.getInstance().getSrs());
        wizard.putProperty(PROP_CONVERTER_PRESELECT_MODE, getConverterPreselectionMode());
        wizard.putProperty(PROP_INPUT_FILE, getInputFile());

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.pack();
        dialog.setLocationRelativeTo(CismapBroker.getInstance().getMappingComponent());
        dialog.setVisible(true);
        dialog.toFront();

        if (wizard.getValue() == WizardDescriptor.FINISH_OPTION) {
            // remember the selected converter
            if ((ConverterPreselectionMode.SESSION_MEMORY == converterPreselectionMode)
                        || (ConverterPreselectionMode.PERMANENT_MEMORY == converterPreselectionMode)
                        || (ConverterPreselectionMode.CONFIGURE_AND_MEMORY == converterPreselectionMode)) {
                setSelectedConverter((Converter)wizard.getProperty(
                        AddGeometriesToMapChooseConverterWizardPanel.PROP_CONVERTER));
            } else {
                setSelectedConverter(null);
            }

            final Frame parent = StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent());
            final WaitingDialogThread<Geometry> wdt = new WaitingDialogThread<Geometry>(
                    parent,
                    true,
                    NbBundle.getMessage(
                        AddGeometriesToMapWizardAction.class,
                        "AddGeometriesToMapWizardAction.actionPerformed(ActionEvent).waitingDialogThread.message"), // NOI18N
                    null,
                    50) {

                    @Override
                    @SuppressWarnings("unchecked")
                    protected Geometry doInBackground() throws Exception {
                        Geometry geometry = (Geometry)wizard.getProperty(
                                AddGeometriesToMapPreviewWizardPanel.PROP_GEOMETRY);

                        if (geometry == null) {
                            final Converter converter = (Converter)wizard.getProperty(
                                    AbstractConverterChooseWizardPanel.PROP_CONVERTER);
                            final Object data = wizard.getProperty(
                                    AddGeometriesToMapEnterDataWizardPanel.PROP_COORDINATE_DATA);
                            final Crs crs = (Crs)wizard.getProperty(AddGeometriesToMapWizardAction.PROP_CURRENT_CRS);

                            assert converter instanceof GeometryConverter : "illegal wizard initialisation"; // NOI18N

                            final GeometryConverter geomConverter = (GeometryConverter)converter;

                            geometry = geomConverter.convertForward(data, crs.getCode());
                        }

                        return geometry;
                    }

                    @Override
                    protected void done() {
                        try {
                            final Geometry geom = get();
                            final PureNewFeature feature = new PureNewFeature(geom);
                            feature.setGeometryType(getGeomType(geom));

                            final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
                            map.getFeatureCollection().addFeature(feature);
                            map.getFeatureCollection().holdFeature(feature);

                            // fixed extent means, don't move map at all
                            if (!map.isFixedMapExtent()) {
                                map.zoomToAFeatureCollection(Arrays.asList((Feature)feature),
                                    true,
                                    map.isFixedMapScale());
                            }
                        } catch (final Exception ex) {
                            final ErrorInfo errorInfo;
                            final StringWriter stacktraceWriter = new StringWriter();
                            ex.printStackTrace(new PrintWriter(stacktraceWriter));
                            if (ex instanceof ConversionException) {
                                errorInfo = new ErrorInfo(
                                        NbBundle.getMessage(
                                            AddGeometriesToMapWizardAction.class,
                                            "AddGeometriesToMapWizardAction.actionPerformed(ActionEvent).waitingDialogThread.conversionError.title"),   // NOI18N
                                        NbBundle.getMessage(
                                            AddGeometriesToMapWizardAction.class,
                                            "AddGeometriesToMapWizardAction.actionPerformed(ActionEvent).waitingDialogThread.conversionError.message"), // NOI18N
                                        stacktraceWriter.toString(),
                                        "WARNING",                                                                                                      // NOI18N
                                        ex,
                                        Level.WARNING,
                                        null);
                            } else {
                                errorInfo = new ErrorInfo(
                                        NbBundle.getMessage(
                                            AddGeometriesToMapWizardAction.class,
                                            "AddGeometriesToMapWizardAction.actionPerformed(ActionEvent).waitingDialogThread.genericError.title"),      // NOI18N
                                        NbBundle.getMessage(
                                            AddGeometriesToMapWizardAction.class,
                                            "AddGeometriesToMapWizardAction.actionPerformed(ActionEvent).waitingDialogThread.genericError.message"),    // NOI18N
                                        stacktraceWriter.toString(),
                                        "WARNING",                                                                                                      // NOI18N
                                        ex,
                                        Level.WARNING,
                                        null);
                            }

                            JXErrorPane.showDialog(parent, errorInfo);
                        }
                    }

                    // cannot map to ellipse
                    private geomTypes getGeomType(final Geometry geom) {
                        final String jtsGeomType = geom.getGeometryType();

                        // JTS v1.12 strings
                        if ("Polygon".equals(jtsGeomType)) {             // NOI18N
                            if (geom.isRectangle()) {
                                return geomTypes.RECTANGLE;
                            } else {
                                return geomTypes.POLYGON;
                            }
                        } else if ("Point".equals(jtsGeomType)) {        // NOI18N
                            return geomTypes.POINT;
                        } else if ("LineString".equals(jtsGeomType)) {   // NOI18N
                            return geomTypes.LINESTRING;
                        } else if ("MultiPolygon".equals(jtsGeomType)) { // NOI18N
                            return geomTypes.MULTIPOLYGON;
                        } else {
                            return geomTypes.UNKNOWN;
                        }
                    }
                };

            // FIXME: the WaitingDialogThread only works properly when using start, thus cannot be put in an executor
            wdt.start();
        }
    }

    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
        // noop
    }

    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
        // noop
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
        // noop
    }

    @Override
    public void dragExit(final DropTargetEvent dte) {
        // noop
    }

    @Override
    public void drop(final DropTargetDropEvent dtde) {
        // TODO
    }

    @Override
    public void configure(final Element parent) {
        doConfigure(parent);
    }

    @Override
    public void masterConfigure(final Element parent) {
        doConfigure(parent);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parent  DOCUMENT ME!
     */
    private void doConfigure(final Element parent) {
        if (parent == null) {
            // no configuration section present, simply leave
            return;
        }

        final Element actionConfigElement = parent.getChild(CONF_SECTION); // NOI18N
        if (actionConfigElement == null) {
            // no configuration section present, simply leave
            return;
        }

        final Element convPreselectModeElement = actionConfigElement.getChild(CONF_CONV_PRESELECT);
        if (convPreselectModeElement == null) {
            setConverterPreselectionMode(ConverterPreselectionMode.DEFAULT);
        } else {
            final String convPreselectModeString = convPreselectModeElement.getText();
            try {
                final ConverterPreselectionMode convPreselectMode = ConverterPreselectionMode.valueOf(
                        convPreselectModeString);
                setConverterPreselectionMode(convPreselectMode);
            } catch (final IllegalArgumentException e) {
                LOG.warn("illegal value for " + CONF_CONV_PRESELECT + ", configuring DEFAULT", e); // NOI18N
                setConverterPreselectionMode(ConverterPreselectionMode.DEFAULT);
            }
        }

        final Element convPreviewGetMapUrlElement = actionConfigElement.getChild(CONF_PREVIEW_GETMAP_URL);
        if (convPreviewGetMapUrlElement == null) {
            setPreviewGetMapUrl(null);
        } else {
            setPreviewGetMapUrl(convPreviewGetMapUrlElement.getText());
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element sectionElement = new Element(CONF_SECTION);

        final Element convPreselectModeElement = new Element(CONF_CONV_PRESELECT);
        convPreselectModeElement.setText(getConverterPreselectionMode().toString());

        final Element convPreviewGetMapUrlElement = new Element(CONF_PREVIEW_GETMAP_URL);
        convPreviewGetMapUrlElement.setText(getPreviewGetMapUrl());

        sectionElement.addContent(convPreselectModeElement);
        sectionElement.addContent(convPreviewGetMapUrlElement);

        return sectionElement;
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
     * @param   converterPreselectionMode  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void setConverterPreselectionMode(final ConverterPreselectionMode converterPreselectionMode) {
        switch (converterPreselectionMode) {
            case CONFIGURE:                                                                                 // fall-through
            case CONFIGURE_AND_MEMORY:                                                                      // fall-through
            case PERMANENT_MEMORY: {
                throw new IllegalArgumentException("mode not supported yet: " + converterPreselectionMode); // NOI18N
            }

            default: {
                this.converterPreselectionMode = converterPreselectionMode;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConverterPreselectionMode getDefaultConverterPreselectionMode() {
        return ConverterPreselectionMode.SESSION_MEMORY;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreviewGetMapUrl() {
        return previewGetMapUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  previewGetMapUrl  DOCUMENT ME!
     */
    public void setPreviewGetMapUrl(final String previewGetMapUrl) {
        this.previewGetMapUrl = previewGetMapUrl;
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
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public File getInputFile() {
        return inputFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  inputFile  DOCUMENT ME!
     */
    public void setInputFile(final File inputFile) {
        this.inputFile = inputFile;
    }
}
