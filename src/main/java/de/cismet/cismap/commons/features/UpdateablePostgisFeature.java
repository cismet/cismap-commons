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
package de.cismet.cismap.commons.features;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import java.sql.Connection;

import java.util.logging.Level;

import de.cismet.cismap.commons.featureservice.factory.PostgisAction;
import de.cismet.cismap.commons.featureservice.factory.PostgisFeatureFactory;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RetrievalService;

import de.cismet.tools.ConnectionInfo;

import static de.cismet.cismap.commons.featureservice.factory.AbstractFeatureFactory.DEBUG;
import static de.cismet.cismap.commons.featureservice.factory.PostgisFeatureFactory.ID_TOKEN;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class UpdateablePostgisFeature extends PostgisFeature {

    //~ Instance fields --------------------------------------------------------

    private final ConnectionInfo connectionInfo;
    private final RetrievalService parentService;
    private PostgisAction action;
    private Connection connection;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UpdateablePostgisFeature object.
     *
     * @param  connectionInfo  DOCUMENT ME!
     * @param  parentService   DOCUMENT ME!
     * @param  action          DOCUMENT ME!
     * @param  connection      DOCUMENT ME!
     */
    public UpdateablePostgisFeature(final ConnectionInfo connectionInfo,
            final RetrievalService parentService,
            final PostgisAction action,
            final Connection connection) {
        this.connectionInfo = connectionInfo;
        this.parentService = parentService;
        this.action = action;
        this.connection = connection;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PostgisAction getAction() {
        return action;
    }

    /**
     * DOCUMENT ME!
     */
    public void doUpdate() {
        try {
            if ((action.getAction() != null) && (action.getAction().length() > 0)) {
                if ((this.connection == null) || this.connection.isClosed()) {
                    this.logger.error("Connection to database lost or not correctly initialised");
                    this.connection = PostgisFeatureFactory.createConnection(this.connectionInfo);
                }

                final java.sql.Statement statement = connection.createStatement();
                final String sql = action.getAction().replaceAll(ID_TOKEN, String.valueOf(getId()));
                if (DEBUG) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("performing action on feature #" + getId() + ": \n" + sql);
                    }
                }
                statement.execute(sql);
                statement.close();
                parentService.retrieve(true);
            } else {
                logger.warn("Feature Service not yet correclty initialised, ignoring action");
                throw new Exception("Feature Service not yet correclty initialised, ignoring action");
            }
        } catch (Exception ex) {
            logger.error("Error during doAction(): " + ex.getMessage(), ex);
            final ErrorInfo ei = new ErrorInfo(
                    "Fehler",
                    "Fehler beim Zugriff auf den FeatureService",
                    null,
                    null,
                    ex,
                    Level.ALL,
                    null);
            JXErrorPane.showDialog(CismapBroker.getInstance().getMappingComponent(), ei);
        }
    }
}
