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

import de.cismet.cismap.commons.Debug;
import de.cismet.tools.CurrentStackTrace;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public abstract class AbstractRetrievalService implements RetrievalService
{
  public final static String PROGRESS_PROPERTY = "progress";//NOI18N
  public final static String PROGRESS_REFRESH = "refresh";//NOI18N

  protected final static boolean DEBUG = Debug.DEBUG;
  protected final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

  protected int progress = -1;
  protected Object errorObject = null;
  protected boolean refreshNeeded = false;
  protected Vector listeners = new Vector();
  protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * Creates a new instance of AbstractRetrievalService
   */
  public AbstractRetrievalService()
  {
  }

  @Override
  public void removeRetrievalListener(RetrievalListener irl)
  {
    listeners.remove(irl);
  }

  @Override
  public void addRetrievalListener(RetrievalListener irl)
  {
    if (!(listeners.contains(irl)))
    {
      listeners.add(irl);
    }
  }
  final Object fireRetrievalStartedLock = new Object();

  public void fireRetrievalStarted(RetrievalEvent e)
  {
    synchronized (fireRetrievalStartedLock)
    {
      this.setProgress(-1);
      e.setRetrievalService(this);
      Iterator it = listeners.iterator();
      while (it.hasNext())
      {
        Object l = it.next();
        if (l instanceof RetrievalListener)
        {
          ((RetrievalListener) l).retrievalStarted(e);
        }
      }
    }
  }
  final Object fireRetrievalProgressLock = new Object();

  public void fireRetrievalProgress(RetrievalEvent e)
  {
    synchronized (fireRetrievalProgressLock)
    {
      this.setProgress(e.getPercentageDone());
      e.setRetrievalService(this);
      Iterator it = listeners.iterator();
      while (it.hasNext())
      {
        Object l = it.next();
        if (l instanceof RetrievalListener)
        {
          ((RetrievalListener) l).retrievalProgress(e);
        }
      }
    }
  }
  final Object fireRetrievalCompleteLock = new Object();

  public void fireRetrievalComplete(RetrievalEvent e)
  {
    synchronized (fireRetrievalCompleteLock)
    {
      this.setProgress(100);
      e.setRetrievalService(this);
      Iterator it = listeners.iterator();
      while (it.hasNext())
      {
        Object l = it.next();
        if (l instanceof RetrievalListener)
        {
          ((RetrievalListener) l).retrievalComplete(e);
        }
      }
    }
  }

  final Object fireRetrievalAbortedLock = new Object();

  public void fireRetrievalAborted(final RetrievalEvent e)
  {
    synchronized (fireRetrievalAbortedLock)
    {
      this.setProgress(0);
      e.setRetrievalService(this);
      Iterator it = listeners.iterator();
      while (it.hasNext())
      {
        Object l = it.next();
        if (l instanceof RetrievalListener)
        {
          ((RetrievalListener) l).retrievalAborted(e);
        }
      }
    }
  }
  final Object fireRetrievalErrorLock = new Object();

  public void fireRetrievalError(RetrievalEvent e)
  {
    synchronized (fireRetrievalErrorLock)
    {
      this.setProgress(0);
      logger.warn("fireRetrievalError: ", new CurrentStackTrace());//NOI18N
      e.setRetrievalService(this);
      Iterator it = listeners.iterator();
      while (it.hasNext())
      {
        Object l = it.next();
        if (l instanceof RetrievalListener)
        {
          ((RetrievalListener) l).retrievalError(e);
        }
      }
    }
  }

  @Override
  public abstract void retrieve(boolean forced);

  @Override
  public abstract Object clone();

  public AbstractRetrievalService cloneWithoutRetrievalListeners()
  {
    AbstractRetrievalService ret = (AbstractRetrievalService) clone();
    ret.listeners.clear();
    return ret;
  }

  public Vector getListeners()
  {
    return listeners;
  }

  @Override
  public int getProgress()
  {
    return progress;
  }

  @Override
  public void setProgress(int progress)
  {
    int oldProgress = this.progress;
    if(progress > 100 || progress < -1)
    {
      logger.warn("invalid progress '" + progress + "', setting to -1 (indeterminate)");//NOI18N
      this.progress = -1;
    }
    else
    {
      this.progress = progress;
    }

    propertyChangeSupport.firePropertyChange(PROGRESS_PROPERTY, oldProgress, this.progress);
  }

  public Object getErrorObject()
  {
    return errorObject;
  }

  public void setErrorObject(Object errorObject)
  {
    this.errorObject = errorObject;
  }

  @Override
  public boolean isRefreshNeeded()
  {
    return refreshNeeded;
  }

  @Override
  public void setRefreshNeeded(boolean refreshNeeded)
  {
    boolean  oldRefreshNeeded = this.refreshNeeded;
    this.refreshNeeded = refreshNeeded;

    propertyChangeSupport.firePropertyChange(PROGRESS_REFRESH, oldRefreshNeeded, this.refreshNeeded);
  }

  public boolean hasErrors()
  {
    return errorObject != null;
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    this.propertyChangeSupport.removePropertyChangeListener(l);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    this.propertyChangeSupport.addPropertyChangeListener(l);
  }
}
