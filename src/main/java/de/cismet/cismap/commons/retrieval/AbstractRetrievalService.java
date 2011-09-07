/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.retrieval;

import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.cismet.cismap.commons.Debug;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractRetrievalService implements RetrievalService {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROGRESS_PROPERTY = "progress"; // NOI18N
    public static final String PROGRESS_REFRESH = "refresh";   // NOI18N

    protected static final boolean DEBUG = Debug.DEBUG;

    protected static final Logger LOG = Logger.getLogger(AbstractRetrievalService.class);

    //~ Instance fields --------------------------------------------------------

    protected int progress = -1;
    protected boolean refreshNeeded = false;
    protected List<RetrievalListener> listeners = new ArrayList<RetrievalListener>();
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected Object errorObject = null;
    final Object fireRetrievalStartedLock = new Object();
    final Object fireRetrievalProgressLock = new Object();
    final Object fireRetrievalCompleteLock = new Object();

    final Object fireRetrievalAbortedLock = new Object();
    final Object fireRetrievalErrorLock = new Object();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of AbstractRetrievalService.
     */
    public AbstractRetrievalService() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void removeRetrievalListener(final RetrievalListener irl) {
        listeners.remove(irl);
    }

    @Override
    public void addRetrievalListener(final RetrievalListener irl) {
        if (!(listeners.contains(irl))) {
            listeners.add(irl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireRetrievalStarted(final RetrievalEvent e) {
        synchronized (fireRetrievalStartedLock) {
            this.setProgress(-1);
            e.setRetrievalService(this);
            final Iterator it = listeners.iterator();
            while (it.hasNext()) {
                final Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener)l).retrievalStarted(e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireRetrievalProgress(final RetrievalEvent e) {
        synchronized (fireRetrievalProgressLock) {
            this.setProgress(e.getPercentageDone());
            e.setRetrievalService(this);
            final Iterator it = listeners.iterator();
            while (it.hasNext()) {
                final Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener)l).retrievalProgress(e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireRetrievalComplete(final RetrievalEvent e) {
        synchronized (fireRetrievalCompleteLock) {
            this.setProgress(100);
            e.setRetrievalService(this);
            final Iterator it = listeners.iterator();
            while (it.hasNext()) {
                final Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener)l).retrievalComplete(e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireRetrievalAborted(final RetrievalEvent e) {
        synchronized (fireRetrievalAbortedLock) {
            this.setProgress(0);
            e.setRetrievalService(this);
            final Iterator it = listeners.iterator();
            while (it.hasNext()) {
                final Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener)l).retrievalAborted(e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireRetrievalError(final RetrievalEvent e) {
        synchronized (fireRetrievalErrorLock) {
            this.setProgress(0);
            LOG.warn("fireRetrievalError: ", new CurrentStackTrace()); // NOI18N
            e.setRetrievalService(this);
            final Iterator it = listeners.iterator();
            while (it.hasNext()) {
                final Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener)l).retrievalError(e);
                }
            }
        }
    }

    @Override
    public abstract void retrieve(boolean forced);
    @Override
    public abstract Object clone();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AbstractRetrievalService cloneWithoutRetrievalListeners() {
        final AbstractRetrievalService ret = (AbstractRetrievalService)clone();
        ret.listeners.clear();
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<RetrievalListener> getListeners() {
        return listeners;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void setProgress(final int progress) {
        final int oldProgress = this.progress;
        if ((progress > 100) || (progress < -1)) {
            LOG.warn("invalid progress '" + progress + "', setting to -1 (indeterminate)"); // NOI18N
            this.progress = -1;
        } else {
            this.progress = progress;
        }

        propertyChangeSupport.firePropertyChange(PROGRESS_PROPERTY, oldProgress, this.progress);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getErrorObject() {
        return errorObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  errorObject  DOCUMENT ME!
     */
    public void setErrorObject(final Object errorObject) {
        this.errorObject = errorObject;
    }

    @Override
    public boolean isRefreshNeeded() {
        return refreshNeeded;
    }

    @Override
    public void setRefreshNeeded(final boolean refreshNeeded) {
        final boolean oldRefreshNeeded = this.refreshNeeded;
        this.refreshNeeded = refreshNeeded;

        propertyChangeSupport.firePropertyChange(PROGRESS_REFRESH, oldRefreshNeeded, this.refreshNeeded);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasErrors() {
        return errorObject != null;
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.propertyChangeSupport.addPropertyChangeListener(l);
    }
}
