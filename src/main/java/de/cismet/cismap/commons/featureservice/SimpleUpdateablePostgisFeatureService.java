/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.featureservice;

import org.jdom.CDATA;
import org.jdom.Element;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.PostgisAction;
import de.cismet.cismap.commons.featureservice.factory.PostgisFeatureFactory;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleUpdateablePostgisFeatureService extends SimplePostgisFeatureService {

    //~ Instance fields --------------------------------------------------------

    protected PostgisAction postgisAction;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleUpdateablePostgisFeatureService object.
     *
     * @param  supfs  DOCUMENT ME!
     */
    public SimpleUpdateablePostgisFeatureService(final SimpleUpdateablePostgisFeatureService supfs) {
        super(supfs);
        this.postgisAction = supfs.getPostgisAction();
    }

    /**
     * Creates a new instance of SimpleUpdateablePostgisFeatureService.
     *
     * @param   element  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SimpleUpdateablePostgisFeatureService(final Element element) throws Exception {
        super(element);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Element toElement() {
        final Element element = super.toElement();
        element.setAttribute("updateable", "true");                             // NOI18N
        final Element actionElement = new Element("action");                    // NOI18N
        actionElement.setAttribute("text", this.postgisAction.getActionText()); // NOI18N
        actionElement.setAttribute("icon", this.postgisAction.getIconPath());   // NOI18N
        actionElement.addContent(new CDATA(this.postgisAction.getAction()));
        element.addContent(actionElement);
        return element;
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        super.initFromElement(element);
        Element actionElement = null;
        this.postgisAction = new PostgisAction();
        try {
            actionElement = element.getChild("action");                                                          // NOI18N
            this.postgisAction.setAction(actionElement.getText());
        } catch (Exception e) {
            logger.warn("No action in updateable Service: " + e.getMessage());                                   // NOI18N
        }
        try {
            this.postgisAction.setActionText(actionElement.getAttribute("text").getValue());                     // NOI18N
            this.postgisAction.setIconPath(actionElement.getAttribute("icon").getValue());                       // NOI18N
            this.postgisAction.setIcon(new ImageIcon(getClass().getResource(this.postgisAction.getIconPath())));
        } catch (Exception e) {
            logger.warn("No actiontext in updateable Service: " + e.getMessage());                               // NOI18N
            this.postgisAction.setActionText(org.openide.util.NbBundle.getMessage(
                    SimpleUpdateablePostgisFeatureService.class,
                    "SimpleUpdateablePostgisFeatureService.initFromElement(Element).postgisAction.actionText")); // NOI18N
        }
    }

    /**
     * Get the value of postgisAction.
     *
     * @return  the value of postgisAction
     */
    public PostgisAction getPostgisAction() {
        return postgisAction;
    }

    @Override
    public SimpleUpdateablePostgisFeatureService clone() {
        return new SimpleUpdateablePostgisFeatureService(this);
    }

    @Override
    protected PostgisFeatureFactory createFeatureFactory() throws Exception {
        return new PostgisFeatureFactory(this.getLayerProperties(), this.getConnectionInfo(), this.postgisAction, this);
    }
}
