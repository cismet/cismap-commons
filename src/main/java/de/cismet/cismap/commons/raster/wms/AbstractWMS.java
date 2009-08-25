/*
 * AbstractWMS.java
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
 * Created on 28. November 2005, 14:20
 *
 */

package de.cismet.cismap.commons.raster.wms;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.CurrentStackTrace;
import java.awt.Image;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public abstract class AbstractWMS extends AbstractRetrievalService implements MapService,RetrievalListener,ServiceLayer{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected BoundingBox bb;
    protected boolean enabled=true;
    private boolean visible=true;
    protected int height=0;
    protected int width=0;
    protected ImageRetrieval ir;
    protected int layerPosition=0;
    protected String name=null;
    protected float translucency=1.0f;
    
    /** Creates a new instance of AbstractWMS */
    public AbstractWMS() {
    }
    
    public void setBoundingBox(de.cismet.cismap.commons.BoundingBox bb) {
        this.bb=bb;
    }

    public void setEnabled(boolean enabled) {
        this.enabled=enabled;
    }

    public void setSize(int height, int width) {
        this.height=height;
        this.width=width;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public boolean canBeDisabled() {
        return true;
    }

    public void retrievalStarted(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalStarted(e);
    }
    public void retrievalProgress(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalProgress(e);
    }
    public void retrievalError(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        log.warn("retrievalError",new CurrentStackTrace());
        this.fireRetrievalError(e);
    }

    
    public void retrievalComplete(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        //Test ob Bild bez\u00FCglich der Gr\u00F6\u00DFe auch dem angeforderten entspricht
        //ansonsten ist es sehr wahrscheinlich dass es sich um ein Fehlerbild handelt
        Object o =e.getRetrievedObject();
        if (o instanceof Image) {
            if (
                    Math.abs(((Image)o).getHeight(null)-height)>1
                    ||
                    Math.abs(((Image)o).getWidth(null)-width)>1
                    ) {
                    e.setHasErrors(true);
            }
            else {
                e.setHasErrors(false);
            }
        }
        if (ir==null||ir.isAlive()) { 
            
            this.fireRetrievalComplete(e);
        }
    }

    
    
    
    
    public void retrievalAborted(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalAborted(e);
    }


    public int getLayerPosition() {
        return layerPosition;
    }

    public String getName() {
        return name;
    }

    public void setLayerPosition(int layerPosition) {
        this.layerPosition = layerPosition;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTranslucency() {
        return translucency;
    }

    public void setTranslucency(float translucency) {
        this.translucency = translucency;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
}
