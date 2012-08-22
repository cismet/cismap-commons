/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import java.awt.HeadlessException;

import java.beans.PropertyChangeEvent;

import java.util.MissingResourceException;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MetaSearchCreateSearchGeometryListener extends AbstractCreateSearchGeometryListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MetaSearchCreateSearchGeometryListener.class);

    //~ Instance fields --------------------------------------------------------

    private MetaSearchFacade metaSearch;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaSearchCreateSearchGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public MetaSearchCreateSearchGeometryListener(final MappingComponent mc) {
        super(mc);
    }

    /**
     * Creates a new MetaSearchCreateSearchGeometryListener object.
     *
     * @param  mc          DOCUMENT ME!
     * @param  metaSearch  DOCUMENT ME!
     */
    public MetaSearchCreateSearchGeometryListener(final MappingComponent mc, final MetaSearchFacade metaSearch) {
        this(mc);

        setMetaSearch(metaSearch);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  metaSearch  DOCUMENT ME!
     */
    public final void setMetaSearch(final MetaSearchFacade metaSearch) {
        this.metaSearch = metaSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isSearchTopicsSelected() {
        boolean result = false;

        if (metaSearch != null) {
            result = metaSearch.hasSelectedSearchTopics();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  HeadlessException         DOCUMENT ME!
     * @throws  MissingResourceException  DOCUMENT ME!
     */
    private void notifyUserAboutMissingSearchTopics() throws HeadlessException, MissingResourceException {
        if (!isSearchTopicsSelected()) {
            if ((metaSearch != null) && metaSearch.hasSearchTopics()) {
                JOptionPane.showMessageDialog(
                    StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent()),
                    org.openide.util.NbBundle.getMessage(
                        MetaSearchCreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().noSearchTopicsChosen"),       // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        MetaSearchCreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().noSearchTopicsChosen.title"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                    StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent()),
                    org.openide.util.NbBundle.getMessage(
                        MetaSearchCreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().notInitialized"),             // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        MetaSearchCreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().notInitialized.title"),       // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ((metaSearch != null) && metaSearch.isSearchTopicSelectedEvent(evt.getPropertyName())) {
            generateAndShowPointerAnnotation();
        } else {
            super.propertyChange(evt);
        }
    }

    @Override
    protected boolean performSearch(final PureNewFeature searchFeature) {
        if (!isSearchTopicsSelected()) {
            // finishGeometry is called before mousePressed. finishGeometry is not called if the user displayed the
            // last search feature. These conditions ensure that there is only one notification in any case.
// if (searchFeature.equals(getLastSearchFeature())) {
            notifyUserAboutMissingSearchTopics();
            return false;
//            }
        }

        final MapSearchEvent mse = new MapSearchEvent();
        mse.setGeometry(searchFeature.getGeometry());
        CismapBroker.getInstance().fireMapSearchInited(mse);

        return true;
    }

    @Override
    protected PNode getPointerAnnotation() {
        if (metaSearch == null) {
            return null;
        }
        return metaSearch.generatePointerAnnotationForSelectedSearchTopics();
    }
}
