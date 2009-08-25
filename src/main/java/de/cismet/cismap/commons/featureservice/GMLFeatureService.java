/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService.FeatureRetrievalWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.GMLFeatureCollectionDocument;
import org.jdom.Element;

/**
 *
 * @author spuhl
 */
public class GMLFeatureService extends DocumentFeatureService {

    public static final HashMap<Integer,Icon> layerIcons = new HashMap<Integer,Icon>();
    static {         
//        layerIcons.put(LAYER_ENABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerGml.png")));
//        layerIcons.put(LAYER_ENABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerGmlInvisible.png")));
//        layerIcons.put(LAYER_DISABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGml.png")));
//        layerIcons.put(LAYER_DISABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerGmlInvisible.png")));
    }
    
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public GMLFeatureService(String name, URI documentURI, Vector<FeatureServiceAttribute> attributes) throws Exception {
        super(name, documentURI, attributes);
    }

    protected GMLFeatureService(GMLFeatureService afs) {
        super(afs);
    }

    public GMLFeatureService(Element e) throws Exception {
        super(e);
    }
    private InputStreamReader reader;

    @Override
    protected Feature[] retrieveFeatures(final long ctm,FeatureRetrievalWorker currentWorker) throws Exception {
        log.info("FeatureCollection für Document wird initialisiert");
        log.info("DocumentUri: " + getDocumentURI());
        reader = new InputStreamReader(new FileInputStream(new File(getDocumentURI())));

        log.debug("Parse jetzt das Ergebniss");       
        FeatureCollection fc = parse(reader, ctm);
        log.debug("Parsen fertig");
        reader.close();
        if (fc == null) {
            throw new Exception("FeatureCollection ist Null");
        }
                        
        return fc.toArray();
    }
           
    
    //TODO redundant because webfeature services uses the same method --> UtilClass
    /**
     * This method parses the features out of the XML response
     * @param reader The reader which contains the FeatureCollection which should be parsed
     * @return the parsed FeatureCollection
     */
    private FeatureCollection parse(InputStreamReader reader, long time) {

        try {
//                String result="";
//                BufferedReader br=new BufferedReader(reader);
//                
//                String line="";
//                while ((line=br.readLine())!=null) {
//                    result+=line;
//                }
//                
//                log.fatal(StaticHtmlTools.stringToHTMLString(result));
//                log.debug("start parsing");
            long start = System.currentTimeMillis();
//                if (isCancelled()) {
//                    log.debug("doInBackground (parse) is canceled");
//                    return null;
//                }
//                ByteArrayInputStream str = new ByteArrayInputStream(result.getBytes());
//                InputStreamReader resultreader=new InputStreamReader(str);
//                
            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();

//                doc.load(resultreader, "http://dummyID");
            doc.load(reader, "http://dummyID");

            FeatureCollection tmp = doc.parse();

            long stop = System.currentTimeMillis();
            log.info(((stop - start) / 1000.0) + " Sekunden dauerte das Parsen");
            reader.close();
            return tmp;
        } catch (Throwable e) {
            log.error("Fehler beim Parsen der Features.", e);

            try {
                reader.close();
            } catch (Exception silent) {
            }
        //e.printStackTrace();
        }
        return null;
    }

//    @Override
//    protected Vector<FeatureServiceAttribute> createFeatureAttributes(final long ctm, final FeatureRetrievalWorker currentWorker) throws Exception {
//        Vector<FeatureServiceAttribute> tmp = new Vector<FeatureServiceAttribute>();
//        Feature[] fc = getFeatures();
//        if (fc != null && fc.length > 0) {
//            for (int i = 0; i < fc.length; i++) {
//                FeatureProperty[] featureProperties = fc[i].getProperties();
//                for (FeatureProperty fp : featureProperties) {
//                    try {
//                        FeatureType type = fc[i].getFeatureType();
//
//                        for (PropertyType pt : type.getProperties()) {
//                            //log.fatal("Property Name=" + pt.getName() + " PropertyType=" + pt.getType());
//                            FeatureServiceAttribute fsa = new FeatureServiceAttribute(pt.getName().getAsString(), Integer.toString(pt.getType()));
//                            if (!tmp.contains(fsa)) {
//                                tmp.add(fsa);
//                            }
//                        }
//                    } catch (Exception ex) {
//                        log.error("Fehler beim Anlegen eines FeatureServiceAttribute");
//                    }
//                }
//            }
//            //setAttributes(tmp);
//            return tmp;
//        } else {
//            return tmp;
//        }
//    }
                
    @Override
    public Object clone() {
        return new GMLFeatureService(this);
    }

    @Override
    public Icon getLayerIcon(int type) {
        return layerIcons.get(type);
    }        
}
