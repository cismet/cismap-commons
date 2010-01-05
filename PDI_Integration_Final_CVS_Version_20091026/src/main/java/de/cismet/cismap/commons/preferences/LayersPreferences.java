package de.cismet.cismap.commons.preferences;

import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.jdom.Element;


public class LayersPreferences{
    private final CismapPreferences cismapPreferences;
    final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private TreeMap rasterServices = new TreeMap();
    private TreeMap featureServices = new TreeMap();
    private boolean appFeatureLayerEnabled = true;
    private float appFeatureLayerTranslucency = 0.9f;
    private String appFeatureLayerName = "";
    public LayersPreferences(CismapPreferences cismapPreferences, Element parent) {
        this.cismapPreferences = cismapPreferences;

        try {appFeatureLayerEnabled=parent.getChild("appFeatureLayer").getAttribute("enabled").getBooleanValue();} catch (Exception e){            this.cismapPreferences.log.warn("Preferences Auslesen. Fehler. appFeatureLayer.enabled  ", e);}
        try {appFeatureLayerTranslucency=parent.getChild("appFeatureLayer").getAttribute("translucency").getFloatValue();} catch (Exception e){            this.cismapPreferences.log.warn("Preferences Auslesen. Fehler. appFeatureLayer.translucency  ", e);}
        try {appFeatureLayerName=parent.getChild("appFeatureLayer").getAttribute("name").getValue();} catch (Exception e){            this.cismapPreferences.log.warn("Preferences Auslesen. Fehler. appFeatureLayer.name  ", e);}
        
        List simpleWmsList = parent.getChild("rasterLayers").getChildren("simpleWms");
        Iterator it = simpleWmsList.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Element) {
                Element el = (Element)o;
                try {
                    boolean skip = false;
                    try {skip=el.getAttribute("skip").getBooleanValue();} catch (Exception skipException){}
                    if (!skip) {
                        SimpleWMS swms = new SimpleWMS(el);
                        rasterServices.put(new Integer(swms.getLayerPosition()),swms);
                    }
                } catch (Exception ex) {
                    log.warn("Preferences Auslesen. Fehler. SimpleWMS erzeugen  ", ex);
                }
            }
        }
        List simplePostgisFeatureServiceList = parent.getChild("featureLayers").getChildren("simplePostgisFeatureService");
        it=simplePostgisFeatureServiceList.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Element) {
                Element el = (Element)o;
                log.debug("parsing '" + el.getName() + "' layer preferences");
                try {
                    log.debug("SimplePostgisFeatureService hinzugef\u00FCgt");
                    boolean skip = false;
                    boolean updateable=false;
                    try {skip=el.getAttribute("skip").getBooleanValue();} catch (Exception skipException){}
                    try {updateable=el.getAttribute("updateable").getBooleanValue();} catch (Exception skipException){}
                    if (!skip) {
                        SimplePostgisFeatureService spfs=null;
                        if (updateable) {
                            spfs = new SimpleUpdateablePostgisFeatureService(el); 
                        }
                        else {
                            spfs = new SimplePostgisFeatureService(el);
                        }
                        
                        featureServices.put(new Integer(spfs.getLayerPosition()),spfs);
                    }
                } catch (Exception ex) {
                    log.warn("Preferences Auslesen. Fehler. SimplePostgisFeatureService erzeugen  ", ex);
                }
            }
        }
        
        List simplePostgisWebServiceList = parent.getChild("featureLayers").getChildren("simpleWebFeatureService");
        it=simplePostgisWebServiceList.iterator();
        while (it.hasNext()) {            
            Object o = it.next();
            if (o instanceof Element) {
                
                Element el = (Element)o;
                try {                    
                    boolean skip = false;
                    boolean updateable=false;
                    try {skip=el.getAttribute("skip").getBooleanValue();} catch (Exception skipException){}
                    try {updateable=el.getAttribute("updateable").getBooleanValue();} catch (Exception skipException){}
                    if (!skip) {
                        WebFeatureService swfs=null;
                        if (updateable) {
                            //TODO IMPLEMENT ?
                            //spfs = new SimpleUpdateablePostgisFeatureService(el); 
                        }
                        else {                            
                            
                            swfs = new WebFeatureService(el);
                        }                        
                        featureServices.put(new Integer(swfs.getLayerPosition()),swfs);
                        log.debug("SimpleWebFeatureService hinzugef\u00FCgt");
                    }
                } catch (Exception ex) {
                    log.warn("Preferences Auslesen. Fehler. SimpleWebFeatureService erzeugen  ", ex);
                }
            }
        }
        
    }

    public TreeMap getRasterServices() {
        return rasterServices;
    }

    public void setRasterServices(TreeMap rasterServices) {
        this.rasterServices = rasterServices;
    }

    public TreeMap getFeatureServices() {
        return featureServices;
    }

    public void setFeatureServices(TreeMap featureServices) {
        this.featureServices = featureServices;
    }

    public boolean isAppFeatureLayerEnabled() {
        return appFeatureLayerEnabled;
    }

    public void setAppFeatureLayerEnabled(boolean appFeatureLayerEnabled) {
        this.appFeatureLayerEnabled = appFeatureLayerEnabled;
    }

    public float getAppFeatureLayerTranslucency() {
        return appFeatureLayerTranslucency;
    }

    public void setAppFeatureLayerTranslucency(float appFeatureLayerTranslucency) {
        this.appFeatureLayerTranslucency = appFeatureLayerTranslucency;
    }

    public String getAppFeatureLayerName() {
        return appFeatureLayerName;
    }

    public void setAppFeatureLayerName(String appFeatureLayerName) {
        this.appFeatureLayerName = appFeatureLayerName;
    }
    
}