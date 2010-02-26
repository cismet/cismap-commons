/*----------------    FILE HEADER  ------------------------------------------
 * This file is part of cismap (http://cismap.sourceforge.net)
 *
 * Copyright (C) 2004 by:
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
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------

 * RetrievalEvent.java
 *
 * Created on 15. September 2004, 16:00
 */
package de.cismet.cismap.commons.retrieval;

import java.util.ArrayList;

/**
 *
 * @author  hell
 */
public class RetrievalEvent
{
  private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

  int percentageDone = 0;
  public static final String SERVERERROR = "SERVERERROR";
  public static final String CLIENTERROR = "CLIENTERROR";
  private static final String UNDEFINED = "UNDEFINED";
  private static String errorType = UNDEFINED;
  private Object retrievedObject = null;
  boolean isComplete = false;
  boolean hasErrors = false;
  private String contentType = null;
  private long requestIdentifier;
  private boolean refreshExisting = false;
  private boolean initialisationEvent = false;
  ArrayList errors = new ArrayList();
  private RetrievalService retrievalService = null;

  /** Creates a new instance of RetrievalEvent */
  public RetrievalEvent()
  {
  }

  /**
   * Getter for property errors.
   * @return Value of property errors.
   */
  public ArrayList getErrors()
  {
    return errors;
  }

  /**
   * Setter for property errors.
   * @param errors New value of property errors.
   */
  public void setErrors(ArrayList errors)
  {
    this.errors = errors;
  }

  /**
   * Getter for property hasErrors.
   * @return Value of property hasErrors.
   */
  public boolean isHasErrors()
  {
    return hasErrors;
  }

  /**
   * Setter for property hasErrors.
   * @param hasErrors New value of property hasErrors.
   */
  public void setHasErrors(boolean hasErrors)
  {
    this.hasErrors = hasErrors;
  }

  /**
   * Getter for property isComplete.
   * @return Value of property isComplete.
   */
  public boolean isIsComplete()
  {
    return isComplete;
  }

  /**
   * Setter for property isComplete.
   * @param isComplete New value of property isComplete.
   */
  public void setIsComplete(boolean isComplete)
  {
    this.isComplete = isComplete;
  }

  /**
   * Getter for property percentageDone.
   * @return Value of property percentageDone.
   */
  public int getPercentageDone()
  {
    return percentageDone;
  }

  /**
   * Setter for property percentageDone.
   * @param percentageDone New value of property percentageDone.
   */
  public void setPercentageDone(int percentageDone)
  {
    if(percentageDone < -1 || percentageDone > 100)
    {
      logger.warn("invalid percentage '"+percentageDone+"', setting to -1 (indeterminate)");
      this.percentageDone = -1;
    }
    else
    {
      this.percentageDone = percentageDone;
    }
  }

  public Object getRetrievedObject()
  {
    return retrievedObject;
  }

  public void setRetrievedObject(Object retrievedObject)
  {
    this.retrievedObject = retrievedObject;
  }

  public String getContentType()
  {
    return contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  public RetrievalService getRetrievalService()
  {
    return retrievalService;
  }

  public void setRetrievalService(RetrievalService retrievalService)
  {
    this.retrievalService = retrievalService;
  }

  public String getErrorType()
  {
    if (errorType == null || errorType.equals(UNDEFINED))
    {
      //log.error("undefined ErrorType !!!!!!1");
      return UNDEFINED;
    }
    return errorType;
  }

  public void setErrorType(String errorType)
  {
    this.errorType = errorType;
  }

  public long getRequestIdentifier()
  {
    return requestIdentifier;
  }

  public void setRequestIdentifier(long requestIdentifier)
  {
    this.requestIdentifier = requestIdentifier;
  }

  public boolean isRefreshExisting()
  {
    return refreshExisting;
  }

  public void setRefreshExisting(boolean refreshExisting)
  {
    this.refreshExisting = refreshExisting;
  }

  /**
   * Get the value of initialisationEvent
   *
   * @return the value of initialisationEvent
   */
  public boolean isInitialisationEvent()
  {
    return initialisationEvent;
  }

  /**
   * Set the value of initialisationEvent
   *
   * @param initialisationEvent new value of initialisationEvent
   */
  public void setInitialisationEvent(boolean initialisationEvent)
  {
    this.initialisationEvent = initialisationEvent;
  }
}
