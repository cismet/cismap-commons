/*
 * AbstractRetrievalService.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 11. Juli 2005, 18:02
 *
 */
package de.cismet.cismap.commons.retrieval;

import de.cismet.tools.CurrentStackTrace;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public abstract class AbstractRetrievalService implements RetrievalService {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int progress;
    private Object errorObject;
    private boolean refreshNeeded;
    protected Vector listeners = new Vector();

    /**
     * Creates a new instance of AbstractRetrievalService
     */
    public AbstractRetrievalService() {
    }

    public void removeRetrievalListener(RetrievalListener irl) {
        listeners.remove(irl);
    }

    public void addRetrievalListener(RetrievalListener irl) {
        if (!(listeners.contains(irl))) {
            listeners.add(irl);
        }
    }
    Object fireRetrievalStartedLock = new Object();

    public void fireRetrievalStarted(RetrievalEvent e) {
        synchronized (fireRetrievalStartedLock) {
            e.setRetrievalService(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener) l).retrievalStarted(e);
                }
            }
        }
    }
    Object fireRetrievalProgressLock = new Object();

    public void fireRetrievalProgress(RetrievalEvent e) {
        synchronized (fireRetrievalProgressLock) {
            e.setRetrievalService(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener) l).retrievalProgress(e);
                }
            }
        }
    }
    Object fireRetrievalCompleteLock = new Object();

    public void fireRetrievalComplete(RetrievalEvent e) {
        synchronized (fireRetrievalCompleteLock) {
            e.setRetrievalService(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener) l).retrievalComplete(e);
                }
            }
        }
    }
    Object fireRetrievalAbortedLock = new Object();

    public void fireRetrievalAborted(RetrievalEvent e) {
        synchronized (fireRetrievalAbortedLock) {
            e.setRetrievalService(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener) l).retrievalAborted(e);
                }
            }
        }
    }
    Object fireRetrievalErrorLock = new Object();

    public void fireRetrievalError(RetrievalEvent e) {
        synchronized (fireRetrievalErrorLock) {
            log.warn("fireRetrievalError",new CurrentStackTrace());
            e.setRetrievalService(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                Object l = it.next();
                if (l instanceof RetrievalListener) {
                    ((RetrievalListener) l).retrievalError(e);
                }
            }
        }
    }

    public abstract void retrieve(boolean forced);

    public abstract Object clone();

    public AbstractRetrievalService cloneWithoutRetrievalListeners() {
        AbstractRetrievalService ret = (AbstractRetrievalService) clone();
        ret.listeners.clear();
        return ret;
    }

    public Vector getListeners() {
        return   listeners;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Object getErrorObject() {
        return errorObject;
    }

    public void setErrorObject(Object errorObject) {
        this.errorObject = errorObject;
    }

    public boolean isRefreshNeeded() {
        return refreshNeeded;
    }

    public void setRefreshNeeded(boolean refreshNeeded) {
        this.refreshNeeded = refreshNeeded;
    }

    public boolean hasErrors() {
        return errorObject != null;
    }
    private Vector<PropertyChangeListener> propertyChangeSupportListener = new Vector<PropertyChangeListener>();

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupportListener.remove(l);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupportListener.add(l);
    }
}
