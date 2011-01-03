/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.retrieval;

import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class RetrievalEvent {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SERVERERROR = "SERVERERROR"; // NOI18N
    public static final String CLIENTERROR = "CLIENTERROR"; // NOI18N
    private static final String UNDEFINED = "UNDEFINED";    // NOI18N
    private static String errorType = UNDEFINED;

    //~ Instance fields --------------------------------------------------------

    int percentageDone = 0;
    boolean isComplete = false;
    boolean hasErrors = false;
    ArrayList errors = new ArrayList();
    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private Object retrievedObject = null;
    private String contentType = null;
    private long requestIdentifier;
    private boolean refreshExisting = false;
    private boolean initialisationEvent = false;
    private RetrievalService retrievalService = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RetrievalEvent.
     */
    public RetrievalEvent() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Getter for property errors.
     *
     * @return  Value of property errors.
     */
    public ArrayList getErrors() {
        return errors;
    }

    /**
     * Setter for property errors.
     *
     * @param  errors  New value of property errors.
     */
    public void setErrors(final ArrayList errors) {
        this.errors = errors;
    }

    /**
     * Getter for property hasErrors.
     *
     * @return  Value of property hasErrors.
     */
    public boolean isHasErrors() {
        return hasErrors;
    }

    /**
     * Setter for property hasErrors.
     *
     * @param  hasErrors  New value of property hasErrors.
     */
    public void setHasErrors(final boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    /**
     * Getter for property isComplete.
     *
     * @return  Value of property isComplete.
     */
    public boolean isIsComplete() {
        return isComplete;
    }

    /**
     * Setter for property isComplete.
     *
     * @param  isComplete  New value of property isComplete.
     */
    public void setIsComplete(final boolean isComplete) {
        this.isComplete = isComplete;
    }

    /**
     * Getter for property percentageDone.
     *
     * @return  Value of property percentageDone.
     */
    public int getPercentageDone() {
        return percentageDone;
    }

    /**
     * Setter for property percentageDone.
     *
     * @param  percentageDone  New value of property percentageDone.
     */
    public void setPercentageDone(final int percentageDone) {
        if ((percentageDone < -1) || (percentageDone > 100)) {
            logger.warn("invalid percentage '" + percentageDone + "', setting to -1 (indeterminate)"); // NOI18N
            this.percentageDone = -1;
        } else {
            this.percentageDone = percentageDone;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getRetrievedObject() {
        return retrievedObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  retrievedObject  DOCUMENT ME!
     */
    public void setRetrievedObject(final Object retrievedObject) {
        this.retrievedObject = retrievedObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  contentType  DOCUMENT ME!
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RetrievalService getRetrievalService() {
        return retrievalService;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  retrievalService  DOCUMENT ME!
     */
    public void setRetrievalService(final RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getErrorType() {
        if ((errorType == null) || errorType.equals(UNDEFINED)) {
            // log.error("undefined ErrorType !!!!!!1");
            return UNDEFINED;
        }
        return errorType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  errorType  DOCUMENT ME!
     */
    public void setErrorType(final String errorType) {
        this.errorType = errorType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public long getRequestIdentifier() {
        return requestIdentifier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  requestIdentifier  DOCUMENT ME!
     */
    public void setRequestIdentifier(final long requestIdentifier) {
        this.requestIdentifier = requestIdentifier;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isRefreshExisting() {
        return refreshExisting;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  refreshExisting  DOCUMENT ME!
     */
    public void setRefreshExisting(final boolean refreshExisting) {
        this.refreshExisting = refreshExisting;
    }

    /**
     * Get the value of initialisationEvent.
     *
     * @return  the value of initialisationEvent
     */
    public boolean isInitialisationEvent() {
        return initialisationEvent;
    }

    /**
     * Set the value of initialisationEvent.
     *
     * @param  initialisationEvent  new value of initialisationEvent
     */
    public void setInitialisationEvent(final boolean initialisationEvent) {
        this.initialisationEvent = initialisationEvent;
    }
}
