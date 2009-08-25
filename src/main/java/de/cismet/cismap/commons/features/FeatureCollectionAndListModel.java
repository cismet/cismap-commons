/*
 * FeatureCollectionAndListModel.java
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
 * Created on 23. August 2006, 10:13
 *
 */

package de.cismet.cismap.commons.features;

import java.util.Collection;
import java.util.Vector;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class FeatureCollectionAndListModel extends DefaultFeatureCollection implements ListSelectionModel{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected Vector<ListSelectionListener> listSelectionListeners=new Vector<ListSelectionListener>();
    private static final int MIN = -1;
    private static final int MAX = Integer.MAX_VALUE;
    private int selectionMode = MULTIPLE_INTERVAL_SELECTION;
    protected int leadIndex=0;
    protected int anchorIndex=0;
    protected boolean valueIsAdjusting=false;
    
    private int firstChangedIndex = MAX;
    private int lastChangedIndex = MIN;
    private int firstAdjustedIndex = MAX;
    private int lastAdjustedIndex = MIN;
    
    
    /**
     * Set the selection mode. The following selectionMode values are allowed:
     * <ul>
     * <li> <code>SINGLE_SELECTION</code>
     *   Only one list index can be selected at a time.  In this
     *   mode the setSelectionInterval and addSelectionInterval
     *   methods are equivalent, and only the second index
     *   argument (the "lead index") is used.
     * <li> <code>SINGLE_INTERVAL_SELECTION</code>
     *   One contiguous index interval can be selected at a time.
     *   In this mode setSelectionInterval and addSelectionInterval
     *   are equivalent.
     * <li> <code>MULTIPLE_INTERVAL_SELECTION</code>
     *   In this mode, there's no restriction on what can be selected.
     * </ul>
     *
     *
     * @see #getSelectionMode
     */
    public void setSelectionMode(int selectionMode) {
        switch (selectionMode) {
            case SINGLE_SELECTION:
            case SINGLE_INTERVAL_SELECTION:
            case MULTIPLE_INTERVAL_SELECTION:
                this.selectionMode = selectionMode;
                break;
            default:
                throw new IllegalArgumentException("invalid selectionMode");
        }
    }
    
    /**
     * Set the lead selection index.
     *
     *
     * @see #getLeadSelectionIndex
     */
    public void setLeadSelectionIndex(int index) {
        try {
            leadIndex=index;
            if (selectedFeatures.size()>0) {
                int from=-1;
                int to=-1;
                if (index>getAnchorSelectionIndex()) {
                    from=getAnchorSelectionIndex();
                    to=index;
                } else {
                    from=index;
                    to=getAnchorSelectionIndex();
                }
                Vector<Feature> v=new Vector<Feature>();
                for ( int i=from;i<=to;++i) {
                    v.add((Feature)getAllFeatures().get(i));
                }
                select(v);
            }
            fireValueChanged();
        } catch (Throwable t) {
            log.error("Error in setLeadSelectionIndex",t);
        }
        
    }
    
    /**
     * Set the anchor selection index.
     *
     *
     * @see #getAnchorSelectionIndex
     */
    public void setAnchorSelectionIndex(int index) {
        try {
            anchorIndex=index;
            if (index>-1) {
                select(features.get(index));
                fireValueChanged();
            }
        } catch (Throwable t) {
            log.error("Error in setAnchorSelectionIndex",t);
        }
        
    }
    
    /**
     *
     * Returns true if the specified index is selected.
     */
    public boolean isSelectedIndex(int index) {
        try {
            return isSelected((Feature)getAllFeatures().get(index));
        } catch (Throwable t) {
            log.error("Error in isSelectedIndex",t);
            return false;
        }
    }
    
    /**
     * This property is true if upcoming changes to the value
     * of the model should be considered a single event. For example
     * if the model is being updated in response to a user drag,
     * the value of the valueIsAdjusting property will be set to true
     * when the drag is initiated and be set to false when
     * the drag is finished.  This property allows listeners to
     * to update only when a change has been finalized, rather
     * than always handling all of the intermediate values.
     *
     *
     * @param valueIsAdjusting The new value of the property.
     * @see #getValueIsAdjusting
     */
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        try {
            this.valueIsAdjusting=valueIsAdjusting;
        } catch (Throwable t) {
            log.error("Error in setValueIsAdjusting",t);
        }
        
        
    }
    
    /**
     * Remove a listener from the list that's notified each time a
     * change to the selection occurs.
     *
     *
     * @param x the ListSelectionListener
     * @see #addListSelectionListener
     */
    public void removeListSelectionListener(ListSelectionListener x) {
        listSelectionListeners.remove(x);
    }
    
    /**
     * Add a listener to the list that's notified each time a change
     * to the selection occurs.
     *
     *
     * @param x the ListSelectionListener
     * @see #removeListSelectionListener
     * @see #setSelectionInterval
     * @see #addSelectionInterval
     * @see #removeSelectionInterval
     * @see #clearSelection
     * @see #insertIndexInterval
     * @see #removeIndexInterval
     */
    public void addListSelectionListener(ListSelectionListener x) {
        listSelectionListeners.add(x);
    }
    
    /**
     *
     * Insert length indices beginning before/after index.  This is typically
     * called to sync the selection model with a corresponding change
     * in the data model.
     */
    public void insertIndexInterval(int index, int length, boolean before) {
        //Nothing to be done here, because the selected items are stored in a collection
    }
    
    /**
     *
     * Change the selection to be between index0 and index1 inclusive.
     * If this represents a change to the current selection, then
     * notify each ListSelectionListener. Note that index0 doesn't have
     * to be less than or equal to index1.
     *
     *
     * @param index0 one end of the interval.
     * @param index1 other end of the interval
     * @see #addListSelectionListener
     */
    public void setSelectionInterval(int index0, int index1) {
        try {
            select(getVectorOfFeatures(index0,index1));
            fireValueChanged();
        } catch (Throwable t) {
            log.error("Error in setSelectionInterval",t);
        }
        
    }
    
    /**
     *
     * Change the selection to be the set difference of the current selection
     * and the indices between index0 and index1 inclusive.  If this represents
     * a change to the current selection, then notify each
     * ListSelectionListener.  Note that index0 doesn't have to be less
     * than or equal to index1.
     *
     *
     * @param index0 one end of the interval.
     * @param index1 other end of the interval
     * @see #addListSelectionListener
     */
    public void removeSelectionInterval(int index0, int index1) {
        try {
            unselect(getVectorOfFeatures(index0,index1));
            fireValueChanged();
        } catch (Throwable t) {
            log.error("Error in removeSelectionInterval",t);
        }
        
    }
    
    /**
     *
     * Change the selection to be the set union of the current selection
     * and the indices between index0 and index1 inclusive.  If this represents
     * a change to the current selection, then notify each
     * ListSelectionListener. Note that index0 doesn't have to be less
     * than or equal to index1.
     *
     *
     * @param index0 one end of the interval.
     * @param index1 other end of the interval
     * @see #addListSelectionListener
     */
    public void addSelectionInterval(int index0, int index1) {
        try {
            addToSelection(getVectorOfFeatures(index0,index1));
        } catch (Throwable t) {
            log.error("Error in addSelectionInterval",t);
        }
        
    }
    
    /**
     * Change the selection to the empty set.  If this represents
     * a change to the current selection then notify each ListSelectionListener.
     *
     *
     * @see #addListSelectionListener
     */
    public void clearSelection() {
        try {
            unselectAll();
            fireValueChanged();
        } catch (Throwable t) {
            log.error("Error in clearSelection",t);
        }
        
    }
    
    /**
     * Return the first index argument from the most recent call to
     * setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     * The most recent index0 is considered the "anchor" and the most recent
     * index1 is considered the "lead".  Some interfaces display these
     * indices specially, e.g. Windows95 displays the lead index with a
     * dotted yellow outline.
     *
     *
     * @see #getLeadSelectionIndex
     * @see #setSelectionInterval
     * @see #addSelectionInterval
     */
    public int getAnchorSelectionIndex() {
        return anchorIndex;
    }
    
    /**
     * Return the second index argument from the most recent call to
     * setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     *
     *
     * @see #getAnchorSelectionIndex
     * @see #setSelectionInterval
     * @see #addSelectionInterval
     */
    public int getLeadSelectionIndex() {
        return leadIndex;
    }
    
    /**
     * Returns the last selected index or -1 if the selection is empty.
     */
    public int getMaxSelectionIndex() {
        try {
            if (selectedFeatures.isEmpty()) {
                return -1;
            } else {
                int ret=-1;
                Collection<Feature> c=selectedFeatures;
                for (Feature f:c) {
                    int index=getAllFeatures().indexOf(f);
                    if (index>ret) {
                        ret=index;
                    }
                }
                return ret;
            }
        } catch (Throwable t) {
            log.error("Error in getMaxSelectionIndex",t);
            return -1;
        }
        
    }
    
    /**
     * Returns the first selected index or -1 if the selection is empty.
     */
    public int getMinSelectionIndex() {
        try {
            if (selectedFeatures.isEmpty()) {
                return -1;
            } else {
                int ret=features.size();
                Collection<Feature> c=selectedFeatures;
                for (Feature f:c) {
                    int index=getAllFeatures().indexOf(f);
                    if (index<ret) {
                        ret=index;
                    }
                }
                return ret;
            }
        } catch (Throwable t) {
            log.error("Error in getMinSelectionIndex",t);
            return -1;
        }
    }
    
    /**
     * Returns the current selection mode.
     *
     * @return The value of the selectionMode property.
     * @see #setSelectionMode
     */
    public int getSelectionMode() {
        return selectionMode;
    }
    
    /**
     * Returns true if the value is undergoing a series of changes.
     *
     * @return true if the value is currently adjusting
     * @see #setValueIsAdjusting
     */
    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }
    
    /**
     * Returns true if no indices are selected.
     */
    public boolean isSelectionEmpty() {
        try {
            return selectedFeatures.isEmpty();
        } catch (Throwable t) {
            log.error("Error in isSelectionEmpty",t);
            return true;
        }
    }
    
    /**
     *
     * Remove the indices in the interval index0,index1 (inclusive) from
     * the selection model.  This is typically called to sync the selection
     * model width a corresponding change in the data model.
     */
    public void removeIndexInterval(int index0, int index1) {
        try {
            removeFeatures(getVectorOfFeatures(index0,index1));
            fireValueChanged();
        } catch (Throwable t) {
            log.error("Error in ",t);
        }
        
    }
    
    private Vector<Feature> getVectorOfFeatures(int index0, int index1) {
        try {
            int from=-1;
            int to=-1;
            if (index0<index1){
                from=index0;
                to=index1;
            } else {
                from=index1;
                to=index0;
            }
            anchorIndex=from;
            leadIndex=to;
            Vector<Feature> v=new Vector<Feature>();
            for ( int i=from;i<=to;++i) {
                v.add((Feature)getAllFeatures().get(i));
            }
            return v;
        } catch (Throwable t) {
            log.error("Error in getVectorOfFeatures",t);
            return null;
        }
        
    }
    
    /**
     * Notifies listeners that we have ended a series of adjustments.
     */
    protected void fireValueChanged(boolean isAdjusting) {
        if (lastChangedIndex == MIN) {
            return;
        }
        /* Change the values before sending the event to the
         * listeners in case the event causes a listener to make
         * another change to the selection.
         */
        int oldFirstChangedIndex = firstChangedIndex;
        int oldLastChangedIndex = lastChangedIndex;
        firstChangedIndex = MAX;
        lastChangedIndex = MIN;
        fireValueChanged(oldFirstChangedIndex, oldLastChangedIndex, isAdjusting);
    }
    
    
    /**
     * Notifies <code>ListSelectionListeners</code> that the value
     * of the selection, in the closed interval <code>firstIndex</code>,
     * <code>lastIndex</code>, has changed.
     */
    protected void fireValueChanged(int firstIndex, int lastIndex) {
        fireValueChanged(firstIndex, lastIndex, getValueIsAdjusting());
    }
    
    /**
     * @param firstIndex the first index in the interval
     * @param lastIndex the last index in the interval
     * @param isAdjusting true if this is the final change in a series of
     *		adjustments
     * @see EventListenerList
     */
    protected void fireValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
        for (ListSelectionListener l:listSelectionListeners) {
            ListSelectionEvent e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
            l.valueChanged(e);
        }
    }
    
    private void fireValueChanged() {
        if (lastAdjustedIndex == MIN) {
            return;
        }
        /* If getValueAdjusting() is true, (eg. during a drag opereration)
         * record the bounds of the changes so that, when the drag finishes (and
         * setValueAdjusting(false) is called) we can post a single event
         * with bounds covering all of these individual adjustments.
         */
        if (getValueIsAdjusting()) {
            firstChangedIndex = Math.min(firstChangedIndex, firstAdjustedIndex);
            lastChangedIndex = Math.max(lastChangedIndex, lastAdjustedIndex);
        }
        /* Change the values before sending the event to the
         * listeners in case the event causes a listener to make
         * another change to the selection.
         */
        int oldFirstAdjustedIndex = firstAdjustedIndex;
        int oldLastAdjustedIndex = lastAdjustedIndex;
        firstAdjustedIndex = MAX;
        lastAdjustedIndex = MIN;
        
        fireValueChanged(oldFirstAdjustedIndex, oldLastAdjustedIndex);
    }
    
}
