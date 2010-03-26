/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.SimpleGetFeatureInfoUrl;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import de.cismet.tools.BrowserLauncher;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 *
 * @author thorsten
 */
public class CustomFeatureInfoListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String featureInforetrievalUrl;

    public String getFeatureInforetrievalUrl() {
        return featureInforetrievalUrl;
    }

    public void setFeatureInforetrievalUrl(String featureInforetrievalUrl) {
        this.featureInforetrievalUrl = featureInforetrievalUrl;
    }
    
    @Override
    public void mouseClicked(PInputEvent pInputEvent) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            MappingComponent mc = (MappingComponent) pInputEvent.getComponent();
            log.debug("featurInfoRetrievalUrl "+featureInforetrievalUrl);//NOI18N
            SimpleGetFeatureInfoUrl url=new SimpleGetFeatureInfoUrl(featureInforetrievalUrl);
            url.setX((int)pInputEvent.getCanvasPosition().getX());
            url.setY((int)pInputEvent.getCanvasPosition().getY());
            url.setHeight(mc.getHeight());
            url.setWidth(mc.getWidth());
            BoundingBox bb=mc.getCurrentBoundingBox();
            url.setX1(bb.getX1());
            url.setX2(bb.getX2());
            url.setY1(bb.getY1());
            url.setY2(bb.getY2());
            try {
                String u=url.toString();                                
                if(u != null){
                    u = u.trim();
                }
                log.info("Open:"+u);//NOI18N
                BrowserLauncher.openURL(u);
            }
            catch (Exception e) {
                log.error("Error",e);//NOI18N
            }
        }
    }

    
}
