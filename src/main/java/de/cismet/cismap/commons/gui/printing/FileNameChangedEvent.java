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
package de.cismet.cismap.commons.gui.printing;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FileNameChangedEvent {

    //~ Instance fields --------------------------------------------------------

    private final String oldFileName;
    private final String newFileName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FileNameChangedEvent object.
     *
     * @param  oldFileName  DOCUMENT ME!
     * @param  newFileName  DOCUMENT ME!
     */
    public FileNameChangedEvent(final String oldFileName, final String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the oldFileName
     */
    public String getOldFileName() {
        return oldFileName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the newFileName
     */
    public String getNewFileName() {
        return newFileName;
    }
}
