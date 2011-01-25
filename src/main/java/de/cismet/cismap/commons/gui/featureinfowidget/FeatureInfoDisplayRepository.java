/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.commons.LayerInfoProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class FeatureInfoDisplayRepository {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FeatureInfoDisplayRepository.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Map<FeatureInfoDisplayKey, FeatureInfoDisplay> repo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureInfoDisplayRepository object.
     */
    public FeatureInfoDisplayRepository() {
        final Lookup.Result<FeatureInfoDisplay> result = Lookup.getDefault().lookupResult(FeatureInfoDisplay.class);
        final Collection<? extends FeatureInfoDisplay> instances = result.allInstances();
        if (LOG.isDebugEnabled()) {
            LOG.debug("found " + instances.size() + " instances of FeatureInfoDisplay"); // NOI18N
        }

        final Map<FeatureInfoDisplayKey, FeatureInfoDisplay> map =
            new HashMap<FeatureInfoDisplayKey, FeatureInfoDisplay>(instances.size());

        for (final FeatureInfoDisplay display : instances) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("found entry: " + display.getDisplayKey() + " -> " + display.getClass()); // NOI18N
            }
            map.put(display.getDisplayKey(), display);
        }

        repo = Collections.unmodifiableMap(map);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   layerclass  DOCUMENT ME!
     * @param   layerinfo   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureInfoDisplay getDisplayClass(final String layerclass, final LayerInfoProvider layerinfo) {
        final String server = layerinfo.getServerURI();
        final String layer = layerinfo.getLayerURI();

        FeatureInfoDisplay display = repo.get(new FeatureInfoDisplayKey(layerclass, server, layer));
        if (display == null) {
            display = repo.get(new FeatureInfoDisplayKey(layerclass, server, FeatureInfoDisplayKey.ANY));

            if (display == null) {
                display = repo.get(new FeatureInfoDisplayKey(
                            layerclass,
                            FeatureInfoDisplayKey.ANY,
                            FeatureInfoDisplayKey.ANY));

                if (display == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("did not find any registered repo: layerclass: " + layerclass // NOI18N
                                    + " || layerinfo: " + layerinfo); // NOI18N
                    }
                    // TODO return null || infoDisplay || StandardInfoDisplay
                }
            }
        }

        return display;
    }
}
