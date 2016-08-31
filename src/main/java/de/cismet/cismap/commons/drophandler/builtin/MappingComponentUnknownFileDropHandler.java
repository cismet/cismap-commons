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
package de.cismet.cismap.commons.drophandler.builtin;

import lombok.Getter;

import org.openide.util.lookup.ServiceProvider;

import java.io.File;

import java.util.Collection;

import javax.swing.SwingUtilities;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandler;
import de.cismet.cismap.commons.drophandler.MappingComponentDropHandlerFileMatcher;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = MappingComponentDropHandler.class)
public class MappingComponentUnknownFileDropHandler implements MappingComponentDropHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final MappingComponentUnknownFileDropHandlerDialog DIALOG =
        MappingComponentUnknownFileDropHandlerDialog.getInstance();

    //~ Instance fields --------------------------------------------------------

    @Getter private final MappingComponentDropHandlerFileMatcher fileMatcher = new AllFileMatcher();

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getPriority() {
        return MappingComponentDropHandlerBuiltinPriorityConstants.UNKNOWN;
    }

    @Override
    public void dropFiles(final Collection<File> files) {
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    DIALOG.setUnknownFiles(files);
                    StaticSwingTools.showDialog(DIALOG);
                }
            });
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class AllFileMatcher implements MappingComponentDropHandlerFileMatcher {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isMatching(final File file) {
            return true;
        }
    }
}
