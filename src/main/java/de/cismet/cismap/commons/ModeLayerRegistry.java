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
package de.cismet.cismap.commons;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class ModeLayerRegistry {

    //~ Instance fields --------------------------------------------------------

    private HashMap<String, ModeLayer> modeLayers = new HashMap<String, ModeLayer>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ModeLayerRegistry object.
     */
    private ModeLayerRegistry() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  key        DOCUMENT ME!
     * @param  modeLayer  DOCUMENT ME!
     */
    public void putModeLayer(final String key, final ModeLayer modeLayer) {
        modeLayers.put(key, modeLayer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ModeLayer getModeLayer(final String key) {
        return modeLayers.get(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ModeLayerRegistry getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        static final ModeLayerRegistry INSTANCE = new ModeLayerRegistry();
    }
}
