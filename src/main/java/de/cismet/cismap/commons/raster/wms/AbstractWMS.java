/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;

import org.apache.log4j.Logger;

import java.awt.Image;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractWMS extends AbstractRetrievalService implements MapService,
    RetrievalListener,
    ServiceLayer {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(AbstractWMS.class);

    //~ Instance fields --------------------------------------------------------

    protected BoundingBox bb;
    protected boolean enabled = true;
    protected int height = 0;
    protected int width = 0;
    protected volatile ImageRetrieval ir;
    protected int layerPosition = 0;
    protected String name = null;
    protected float translucency = 1.0f;
    protected boolean visible = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AbstractWMS.
     */
    public AbstractWMS() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setBoundingBox(final de.cismet.cismap.commons.BoundingBox bb) {
        this.bb = bb;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setSize(final int height, final int width) {
        this.height = height;
        this.width = width;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public void retrievalStarted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalStarted(e);
    }

    @Override
    public void retrievalProgress(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalProgress(e);
    }

    @Override
    public void retrievalError(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        LOG.warn("retrievalError", new CurrentStackTrace()); // NOI18N
        this.fireRetrievalError(e);
    }

    @Override
    public void retrievalComplete(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        // Test ob Bild bez\u00FCglich der Gr\u00F6\u00DFe auch dem angeforderten entspricht
        // ansonsten ist es sehr wahrscheinlich dass es sich um ein Fehlerbild handelt
        final Object o = e.getRetrievedObject();
        if (o instanceof Image) {
            if ((Math.abs(((Image)o).getHeight(null) - height) > 1)
                        || (Math.abs(((Image)o).getWidth(null) - width) > 1)) {
                e.setHasErrors(true);
            } else {
                e.setHasErrors(false);
            }
        }
        if ((ir == null) || ir.isAlive()) {
            this.fireRetrievalComplete(e);
        }
    }

    @Override
    public void retrievalAborted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalAborted(e);
    }

    @Override
    public int getLayerPosition() {
        return layerPosition;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
        this.layerPosition = layerPosition;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public float getTranslucency() {
        return translucency;
    }

    @Override
    public void setTranslucency(final float translucency) {
        this.translucency = translucency;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
}
