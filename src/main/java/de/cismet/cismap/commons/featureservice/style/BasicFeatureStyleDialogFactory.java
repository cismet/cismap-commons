/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import org.jdom.Element;

import org.openide.util.lookup.ServiceProvider;

import java.awt.Frame;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.SLDStyledLayer;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerTableCellEditor;
import de.cismet.cismap.commons.wfs.WFSFacade;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = StyleDialogInterface.class)
public class BasicFeatureStyleDialogFactory implements StyleDialogInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final boolean DEBUG = Debug.DEBUG;
    public static final String KEY = "basic";

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private StyleDialog dialog;
    private AbstractFeatureService selectedService;
    private Frame parent;

    //~ Methods ----------------------------------------------------------------

    @Override
    public JDialog configureDialog(final AbstractFeatureService FeatureService,
            final Frame parentFrame,
            final MappingComponent mappingComponent,
            final ArrayList<String> configTabs) {
        selectedService = FeatureService;
        parent = parentFrame;
        dialog = new StyleDialog(parentFrame, true);
        dialog.configureDialog(FeatureService.getSLDDefiniton(),
            FeatureService.getName(),
            FeatureService.getLayerProperties(),
            FeatureService.getFeatureServiceAttributes(),
            FeatureService.getQuery());
        return dialog;
    }

    @Override
    public Runnable createResultTask() {
        return new Runnable() {

                @Override
                public void run() {
                    try {
                        boolean forceUpdate = false;
                        if (selectedService instanceof WebFeatureService) {
                            if (dialog.isGeoAttributeChanged()
                                        || dialog.isAttributeSelectionChanged()) {
                                if (DEBUG) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Attributes changed, updating the QUERY Element"); // NOI18N
                                    }
                                }
                                final Element query = ((WebFeatureService)selectedService).getQueryElement();
                                final WebFeatureService service = ((WebFeatureService)selectedService);
                                WFSFacade.setGeometry(query,
                                    dialog.getSelectedGeoAttribute(), service.getVersion());
                                WFSFacade.changePropertyNames(
                                    query,
                                    dialog.getSelectedAttributes(),
                                    service.getVersion());

                                service.setQueryElement(query);
                                forceUpdate = true;
                            }

                            if (dialog.isQueryStringChanged()) {
                                final int i = JOptionPane.showConfirmDialog(
                                        parent,
                                        org.openide.util.NbBundle.getMessage(
                                            ActiveLayerTableCellEditor.class,
                                            "ActiveLayerTableCellEditor.mouseClicked(MouseEvent).showConfirmDialog.message"), // NOI18N
                                        org.openide.util.NbBundle.getMessage(
                                            ActiveLayerTableCellEditor.class,
                                            "ActiveLayerTableCellEditor.mouseClicked(MouseEvent).showConfirmDialog.title"), // NOI18N
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE);
                                if (i == JOptionPane.YES_OPTION) {
                                    if (DEBUG) {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Query String changed, updating the QUERY String ");           // NOI18N
                                        }
                                    }
                                    selectedService.setQuery(dialog.getQueryString());
                                    forceUpdate = true;
                                }
                            }
                        }

                        // this causes a refresh of the last created features and fires a
                        // retrieval event
                        selectedService.setFeatureServiceAttributes(dialog.getFeatureServiceAttributes());

                        if (forceUpdate) {
                            ((WebFeatureService)selectedService).setLayerPropertiesWithoutUpdate(
                                dialog.getLayerProperties());
                            selectedService.retrieve(forceUpdate);
                        } else {
                            selectedService.setLayerProperties(dialog.getLayerProperties(), false);
                            if (selectedService instanceof SLDStyledLayer) {
                                ((SLDStyledLayer)selectedService).setSLDInputStream(
                                    dialog.getSLDStyle());
                            }
                            selectedService.refreshFeatures();
                        }
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                }
            };
    }

    @Override
    public boolean isAccepted() {
        return dialog.isAccepted();
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
