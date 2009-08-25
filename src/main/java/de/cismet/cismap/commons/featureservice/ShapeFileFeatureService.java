/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService.FeatureRetrievalWorker;
import java.net.URI;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.deegree2.io.shpapi.ShapeFile;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.FeatureFactory;
import org.jdom.Element;

/**
 *
 * @author spuhl
 */
//Todo optimieren wann welche Features geladen werden z.B. bei 150 MB file
public class ShapeFileFeatureService extends DocumentFeatureService {

    public static final HashMap<Integer,Icon> layerIcons = new HashMap<Integer,Icon>();
    static {          
//        layerIcons.put(LAYER_ENABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png")));
//        layerIcons.put(LAYER_ENABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));
//        layerIcons.put(LAYER_DISABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));
//        layerIcons.put(LAYER_DISABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png")));    
    }
    
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public ShapeFileFeatureService(String name, URI documentURI, Vector<FeatureServiceAttribute> attributes) throws Exception {
        super(name, documentURI, attributes);
    }

    protected ShapeFileFeatureService(ShapeFileFeatureService afs) {
        super(afs);
    }

    public ShapeFileFeatureService(Element e) throws Exception {
        super(e);
    }

    @Override
    public Object clone() {
        return new ShapeFileFeatureService(this);
    }

    @Override
    protected Feature[] retrieveFeatures(long ctm, FeatureRetrievalWorker currentWorker) throws Exception {
        //only inital size
        FeatureCollection fc = FeatureFactory.createFeatureCollection("id", 100);

        try {
            ShapeFile sf;
            if(getDocumentURI().getPath().endsWith(".shp")){
                sf = new ShapeFile(getDocumentURI().getPath().substring(0,getDocumentURI().getPath().length()-4));
            } else {
                sf = new ShapeFile(getDocumentURI().getPath());
            }             
            System.out.println(sf.getFileMBR());
            for (int i = 0; i < sf.getRecordNum(); i++) {                
                try {
                    Feature feat = sf.getFeatureByRecNo(i + 1);
                    fc.add(feat);
                } catch (Exception ex) {
                    //TODO Infrom User that not all Features of shape are visible
                    log.error("Fehler während dem umwandlen eines Features",ex);            
                }
            }
            sf.close();
        } catch (Exception ex) {
            log.error("Fehler beim parsen des Shapefiles",ex);
        }
        return fc.toArray();
    }

    @Override
    public Icon getLayerIcon(int type) {
        return layerIcons.get(type);
    }
            
}
