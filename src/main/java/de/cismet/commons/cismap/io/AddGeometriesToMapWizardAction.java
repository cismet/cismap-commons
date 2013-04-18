/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import com.vividsolutions.jts.geom.Geometry;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeometryConverter;

import de.cismet.commons.converter.Converter;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class AddGeometriesToMapWizardAction extends AbstractAction implements DropTargetListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_AVAILABLE_CONVERTERS = "__prop_available_converters__"; // NOI18N
    public static final String PROP_INPUT_FILE = "__prop_input_file__";                     // NOI18N
    public static final String PROP_CURRENT_EPSG_CODE = "__prop_current_epsg_code__";       // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AddGeometriesToMapWizardAction object.
     */
    public AddGeometriesToMapWizardAction() {
//        super("", ImageUtilities.loadImageIcon("", false)); // NOI18N
        super("AddCoordGeomWizard"); // NOI18N

        putValue(Action.SHORT_DESCRIPTION, "Wizard to add geometries from coordinates to the map");
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
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizard.setTitle("Add geometry to map");

        final Collection<? extends GeometryConverter> availableConverters = Lookup.getDefault()
                    .lookupAll(GeometryConverter.class);

        // erase previous data
        wizard.putProperty(AddGeometriesToMapEnterDataWizardPanel.PROP_COORDINATE_DATA, null);
        wizard.putProperty(AddGeometriesToMapPreviewWizardPanel.PROP_GEOMETRY, null);

        wizard.putProperty(PROP_AVAILABLE_CONVERTERS, new ArrayList<Converter>(availableConverters));
        // FIXME: dummy code to be independent of CismapBroker for testing
        wizard.putProperty(PROP_CURRENT_EPSG_CODE, "EPSG:4324");
//        wizard.putProperty(PROP_CURRENT_EPSG_CODE, CismapBroker.getInstance().getSrs().getCode());

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.pack();
        dialog.setLocationRelativeTo(CismapBroker.getInstance().getMappingComponent());
        dialog.setVisible(true);
        dialog.toFront();

        if (wizard.getValue() == WizardDescriptor.FINISH_OPTION) {
            final Geometry geometry = (Geometry)wizard.getProperty(AddGeometriesToMapPreviewWizardPanel.PROP_GEOMETRY);
            if (geometry == null) {
                // load geometry
            }

            final Feature feature = new DefaultStyledFeature();
            feature.setGeometry(geometry);

            final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
            // FIXME: removed for testing purposes
//            map.addFeaturesToMap(new Feature[] { feature });
//            map.zoomToAFeatureCollection(Arrays.asList(feature), true, false);
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
}
