/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.raster.tms.tmscapability;

import java.util.ArrayList;
import org.deegree.services.wms.capabilities.WMSCapabilities;


/**
 *
 * @author cschmidt
 */
public class TMSCapabilities {
    private final ArrayList<TileSet> tileSetList; 
    private final String name;
    private final org.apache.log4j.Logger log = 
            org.apache.log4j.Logger.getLogger(this.getClass());
    private final WMSCapabilities wmsCapabilities;
    
    
    TMSCapabilities(ArrayList<TileSet> tileSetList, String name, WMSCapabilities wmsCaps){
        this.tileSetList = tileSetList;        
        this.name = name;
        this.wmsCapabilities = wmsCaps;        
    }
    TMSCapabilities(ArrayList<TileSet> tileSetList, String name){
        this.tileSetList = tileSetList;
        this.name = name;
        wmsCapabilities = null;
        
    }
    
    TMSCapabilities(ArrayList<TileSet> tileSetList, WMSCapabilities wmsCaps){
        this.tileSetList = tileSetList;
        this.name = "TileCache";//NOI18N
        this.wmsCapabilities = wmsCaps;
    }

    public ArrayList<TileSet> getTileSetList() {
        return tileSetList;
    }
    
    public TileSet getTileSetByLayer(String layerName){
        for (TileSet aktTileSet : tileSetList){
            if(aktTileSet != null && aktTileSet.getLayer().compareTo(layerName) == 0){
                return aktTileSet;
            }
        }
        return null;
    }
    
    public String getName(){        
        if(name != null)
            return name;
        else 
            return "TMSCapabilities";//NOI18N
    }
    
    @Override
    public String toString(){        
        if(name != null)
            return name;
        else
            return "TMSCapabilities";//NOI18N
    }
    
   
    
    @Override
    public int hashCode(){
        int result = 11;
        int multiplikator = 43;
        
        result = multiplikator * result + this.getName().hashCode();
        result = multiplikator * result + this.getTileSetList().hashCode();
        
        return result;
    }

    @Override
    public boolean equals(Object obj) {
//        log.fatal("Call of TMSCapabilities.equals");
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TMSCapabilities other = (TMSCapabilities) obj;
        if (this.tileSetList != other.tileSetList && (this.tileSetList == null || !this.tileSetList.equals(other.tileSetList))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    public WMSCapabilities getWmsCapabilities() {
        return wmsCapabilities;
    }
    
}
