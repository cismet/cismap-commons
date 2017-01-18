/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.swing.Icon;
import javax.swing.JDialog;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StyleDialogStarter extends WaitingDialogThread<JDialog> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger(StyleDialogStarter.class);

    //~ Instance fields --------------------------------------------------------

    private final Frame parentFrame;
    private final AbstractFeatureService selectedService;
    private StyleDialogInterface styleDialog;
    private final ArrayList<String> panel;
    private final List<StyleDialogClosedListener> listener = new ArrayList<StyleDialogClosedListener>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StyleDialogStarter object.
     *
     * @param  parent           DOCUMENT ME!
     * @param  selectedService  DOCUMENT ME!
     * @param  panel            DOCUMENT ME!
     * @param  delay            DOCUMENT ME!
     */
    public StyleDialogStarter(final Frame parent,
            final AbstractFeatureService selectedService,
            final ArrayList<String> panel,
            final int delay) {
        super(
            parent,
            true,
            NbBundle.getMessage(StyleDialogStarter.class, "StyleDialogStarter.StyleDialogStarter()"),
            null,
            delay);
        this.parentFrame = parent;
        this.selectedService = selectedService;
        this.panel = panel;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected JDialog doInBackground() throws Exception {
        if (styleDialog == null) {
            if (log.isDebugEnabled()) {
                log.debug("creating new StyleDialog '"
                            + parentFrame.getTitle() + "'"); // NOI18N
            }

            final String lookupkey = CismapBroker.getInstance().getFeatureStylingComponentKey();

            if ((lookupkey != null) && !lookupkey.isEmpty()) {
                final Lookup.Result<StyleDialogInterface> result = Lookup.getDefault()
                            .lookupResult(StyleDialogInterface.class);

                for (final StyleDialogInterface dialog : result.allInstances()) {
                    if (lookupkey.equals(dialog.getKey())) {
                        styleDialog = dialog;
                    }
                }
            }
            if (styleDialog == null) {
                styleDialog = Lookup.getDefault().lookup(StyleDialogInterface.class);
            }
        }
        // configure dialog, adding attributes to the tab and
        // set style from the layer properties

        return styleDialog.configureDialog(
                selectedService,
                parentFrame,
                CismapBroker.getInstance().getMappingComponent(),
                panel);
    }

    @Override
    protected void done() {
        try {
            final JDialog dialog = get();

            dialog.setPreferredSize(new Dimension(
                    dialog.getPreferredSize().width
                            + 70,
                    dialog.getPreferredSize().height));
            if (log.isDebugEnabled()) {
                log.debug("set dialog visible"); // NOI18N
            }
            StaticSwingTools.showDialog(dialog);

            // check returnstatus
            if ((styleDialog != null) && styleDialog.isAccepted()) {
                final Runnable r = styleDialog.createResultTask();

                final ExecutorService es = CismetExecutors.newSingleThreadExecutor();
                es.submit(r);
                es.submit(new Runnable() {

                        @Override
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        fireStyleDialogClosed(new StyleDialogClosedEvent(styleDialog));
                                    }
                                });
                        }
                    });
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Style Dialog canceled"); // NOI18N
                }
            }
        } catch (Throwable t) {
            log.error("could not configure StyleDialog: " + t.getMessage(), t); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void fireStyleDialogClosed(final StyleDialogClosedEvent evt) {
        for (final StyleDialogClosedListener l : listener) {
            l.StyleDialogClosed(evt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addStyleDialogClosedListener(final StyleDialogClosedListener l) {
        listener.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void removeStyleDialogClosedListener(final StyleDialogClosedListener l) {
        listener.remove(l);
    }
}
