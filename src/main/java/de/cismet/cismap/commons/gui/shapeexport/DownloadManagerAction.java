/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.shapeexport;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * This action is responsible for the the steps to be done when the user wants to see the current downloads.
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadManagerAction extends AbstractAction implements WindowListener {

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
        final JDialog downloadManager = DownloadManagerDialog.instance(StaticSwingTools.getParentFrame(
                    CismapBroker.getInstance().getMappingComponent()));
        if (!downloadManager.isVisible()) {
            downloadManager.setLocationRelativeTo(StaticSwingTools.getParentFrame(
                    CismapBroker.getInstance().getMappingComponent()));
            downloadManager.addWindowListener(this);
            downloadManager.setVisible(true);
            downloadManager.pack();
        }
    }

    @Override
    public void windowOpened(final WindowEvent e) {
        setEnabled(false);
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        setEnabled(true);
    }

    @Override
    public void windowClosed(final WindowEvent e) {
    }

    @Override
    public void windowIconified(final WindowEvent e) {
    }

    @Override
    public void windowDeiconified(final WindowEvent e) {
    }

    @Override
    public void windowActivated(final WindowEvent e) {
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
    }
}
