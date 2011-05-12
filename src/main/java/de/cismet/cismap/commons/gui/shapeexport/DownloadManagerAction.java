/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.shapeexport;

import org.openide.util.NbBundle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadManagerAction extends AbstractAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadManagerAction object.
     */
    public DownloadManagerAction() {
        super();
        putValue(
            SMALL_ICON,
            new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/downloadmanager.png")));
        putValue(
            SHORT_DESCRIPTION,
            NbBundle.getMessage(DownloadManagerAction.class, "DownloadManagerAction.tooltiptext"));
        putValue(NAME, NbBundle.getMessage(DownloadManagerAction.class, "DownloadManagerAction.name"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JDialog downloadManager = new JDialog(StaticSwingTools.getParentFrame(
                    CismapBroker.getInstance().getMappingComponent()),
                NbBundle.getMessage(
                    DownloadManagerAction.class,
                    "DownloadManagerAction.actionPerformed(ActionEvent).JDialog.title"));
        final DownloadManagerPanel pnlDownload = new DownloadManagerPanel();
        downloadManager.setLayout(new BorderLayout());
        downloadManager.add(pnlDownload, BorderLayout.CENTER);
        downloadManager.addWindowListener(pnlDownload);
        downloadManager.setPreferredSize(new Dimension(600, 150));
        downloadManager.validate();
        downloadManager.pack();
        downloadManager.setLocationRelativeTo(StaticSwingTools.getParentFrame(
                CismapBroker.getInstance().getMappingComponent()));
        downloadManager.setVisible(true);
    }
}
