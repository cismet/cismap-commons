package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.wms.capabilities.Operation;
import de.cismet.cismap.commons.wms.capabilities.Request;
import org.apache.log4j.Logger;
import org.deegree.owscommon_new.OperationsMetadata;



/**
 *
 * @author therter
 */
public class DeegreeRequest implements Request {
    private static final Logger log = Logger.getLogger(DeegreeRequest.class);
    private OperationsMetadata req;


    public DeegreeRequest(OperationsMetadata req) {
        this.req = req;
    }


    @Override
    public Operation getMapOperation() {
        for ( org.deegree.owscommon_new.Operation op : req.getOperations() ) {
            if (op.getName().getLocalName().indexOf("getMap") != -1 || op.getName().getLocalName().indexOf("GetMap") != -1) {
                return new DeegreeOperation(op);
            }
        }

        log.error("getMap operation not found");
        return null;
    }


    @Override
    public Operation getFeatureInfoOperation() {
        for ( org.deegree.owscommon_new.Operation op : req.getOperations() ) {
            if (op.getName().getLocalName().indexOf("getFeatureInfo") != -1 || op.getName().getLocalName().indexOf("GetFeatureInfo") != -1) {
                return new DeegreeOperation(op);
            }
        }

        log.error("getFeatureInfo operation not found");
        return null;
    }
}
