/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities.deegree;

import org.apache.log4j.Logger;

import org.deegree.owscommon_new.OperationsMetadata;

import de.cismet.cismap.commons.wms.capabilities.Operation;
import de.cismet.cismap.commons.wms.capabilities.Request;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeRequest implements Request {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger(DeegreeRequest.class);

    //~ Instance fields --------------------------------------------------------

    private OperationsMetadata req;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeRequest object.
     *
     * @param  req  DOCUMENT ME!
     */
    public DeegreeRequest(final OperationsMetadata req) {
        this.req = req;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Operation getMapOperation() {
        for (final org.deegree.owscommon_new.Operation op : req.getOperations()) {
            if ((op.getName().getLocalName().indexOf("getMap") != -1)
                        || (op.getName().getLocalName().indexOf("GetMap") != -1)) {
                return new DeegreeOperation(op);
            }
        }

        log.error("getMap operation not found");
        return null;
    }

    @Override
    public Operation getFeatureInfoOperation() {
        for (final org.deegree.owscommon_new.Operation op : req.getOperations()) {
            if ((op.getName().getLocalName().indexOf("getFeatureInfo") != -1)
                        || (op.getName().getLocalName().indexOf("GetFeatureInfo") != -1)) {
                return new DeegreeOperation(op);
            }
        }

        log.error("getFeatureInfo operation not found");
        return null;
    }
}
