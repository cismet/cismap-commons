/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.raster.tms;

import de.cismet.cismap.commons.raster.tms.tmscapability.TMSCapabilities;
import de.cismet.cismap.commons.raster.tms.tmscapability.TileSet;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author cschmidt
 */
public class TMSCapabilitiesTreeModel implements TreeModel{
    private final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(this.getClass());
    private final TMSCapabilities capabilities;
    private final Vector listener = new Vector();
    private final ArrayList<TileSet> tileSetList;

    public TMSCapabilitiesTreeModel(TMSCapabilities caps) {
//        log.fatal("Im Konstruktor des TMSCapabilitiesTreeModel");
        this.capabilities = caps;
        tileSetList = caps.getTileSetList();
    }
    
    public TMSCapabilities getCapabilities(){
        log.debug("TMSCapabilitiesTreeModel.getCapabilities");//NOI18N
        return capabilities;
    }
    
    

    @Override
    public Object getRoot() {
        return capabilities;
    }

    @Override
    public Object getChild(Object parent, int index) {
        int childs = getChildCount(parent);
        if (childs <= 0) {
            return null;
        } else {
            if (parent instanceof TMSCapabilities) {
                if (index < childs) {
                    return ((TMSCapabilities)parent).getTileSetList().get(index);
                } else {
                    return ((TMSCapabilities)parent).getTileSetList().get(index-childs);
                }
            }
            else{
                return null;
            }
        }        
    }

    @Override
    public int getChildCount(Object parent) {        
        if(parent instanceof TMSCapabilities){
            return ((TMSCapabilities)parent).getTileSetList().size();
        }else
            return 0;
    }

    @Override
    public boolean isLeaf(Object node) {        
        if(node instanceof TileSet){
            return true;
        }else
            return false;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        log.debug("TMSCapabilitiesTreeModel.valueForPathChanged");//NOI18N
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        log.debug("TMSCapabilitiesTreeModel.getIndexForChild");//NOI18N
        if(parent instanceof TMSCapabilities && child instanceof TileSet){
            return ((TMSCapabilities)parent).getTileSetList().indexOf(child);
        }
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        log.debug("TMSCapabilitiesTreeModel.addTreeModelListener");//NOI18N
        listener.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        log.debug("TMSCapabilitiesTreeModel.removeTreeModelListener");//NOI18N
        listener.remove(l);
    }

}
