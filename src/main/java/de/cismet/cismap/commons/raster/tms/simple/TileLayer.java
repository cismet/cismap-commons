/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.raster.tms.simple;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.XPImage;
import de.cismet.cismap.commons.raster.tms.tmscapability.TileSet;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;
import org.jdom.Element;

/**
 *
 * @author cschmidt
 */
public class TileLayer extends SimpleWMS {

    private final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(this.getClass());
    public final BoundingBox WUPP_BB;
    private final int PRE_LOADED_TILES = 8;    
    public  final WeakHashMap<XPImage, TileObject> hashIsLoaded = new WeakHashMap<XPImage, TileObject>();
    public final Map<XPImage, TileObject> isLoaded = Collections.synchronizedMap(hashIsLoaded);
    private ZoomLevel aktZoomLevel;
    private LinkedList<ZoomLevel> zoomLevels = new LinkedList<ZoomLevel>();
    private MappingComponent mappingComponent;
    private TileSet tileSet;
    private final String version;
    private final String host;
    private final String layer;
    private final String format;
    private final int tileSize;
    private final ThreadList threads = new ThreadList(); 
    private double aktCamViewScale = 0.0;
    private final Long delay = 600L;    
    


    
    public TileLayer(MappingComponent mapC, TileSet tileSet){
        this.tileSet = tileSet;
        log.debug("create TileLayer");//NOI18N
        this.mappingComponent = mapC;
        this.setName(tileSet.getLayer());
        this.version = tileSet.getVersion();
        this.host = tileSet.getHost();
        this.layer = tileSet.getLayer();
        this.format = "."+tileSet.getFormat();//NOI18N
        this.WUPP_BB = tileSet.getBoundingBox();
        if ( tileSet.getWidth() == tileSet.getHeight() ) {
            this.tileSize = tileSet.getWidth();
        }
        else{
            log.error("Error in TileSet. Width and Height must have the same value. Width: "+ //NOI18N
                    tileSet.getWidth()+" Height: "+tileSet.getHeight());//NOI18N
            this.tileSize = 0;
            return;
        }

        initZoomLevels(tileSet.getResolutions());

        retrieve(true);
        log.debug("TileLayer created");//NOI18N
        
    }

    @Override
    public String toString() {
        return getName();
    }

    
    private void initZoomLevels(Double[] resolutions){ 
        
        final int DPI = 100;
        for(int i = resolutions.length-1; i >= 0; i--){            
            double res = resolutions[i];                        
            double pointsPerMeter = ((DPI/2.54)*100);
            int scale = (int)Math.round(res*pointsPerMeter); 
            ZoomLevel newZoomLevel = new ZoomLevel(i, res, scale);
            zoomLevels.addFirst(newZoomLevel);
        }
        
    }
    
    
    
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    public void setMappingComponent(MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }

    
    public void setZoomLevel(double scaleDenominator) {
        logger.debug("setZoomLevel: aktCamViewScale " + aktCamViewScale + " scaleDenominator: " + scaleDenominator);//NOI18N
        isLoaded.clear();

        if (Math.round(scaleDenominator) >= zoomLevels.getFirst().getScale()) {
            aktZoomLevel = zoomLevels.getFirst();
            BoundingBox newBox = mappingComponent.getBoundingBoxFromScale(
                    aktZoomLevel.getScale());
            mappingComponent.gotoBoundingBoxWithHistory(newBox);
            return;
        } else if (Math.round(scaleDenominator) <= zoomLevels.getLast().getScale()) {
            aktZoomLevel = zoomLevels.getLast();
            BoundingBox newBox = mappingComponent.getBoundingBoxFromScale(
                    aktZoomLevel.getScale());
            mappingComponent.gotoBoundingBoxWithHistory(newBox);
            return;
        } else {
            final Iterator<ZoomLevel> zoomLevelIterator = zoomLevels.iterator();
            final long roundedScaleDenominator = Math.round(scaleDenominator);
            ZoomLevel nextZoomLevel = null;
            ZoomLevel newZoomLevel = null;

            while (zoomLevelIterator.hasNext()) {
                if (newZoomLevel == null && nextZoomLevel == null) {
                    newZoomLevel = zoomLevelIterator.next();
                    if (zoomLevelIterator.hasNext()) {
                        nextZoomLevel = zoomLevelIterator.next();
                    }
                } else {
                    newZoomLevel = nextZoomLevel;
                    nextZoomLevel = zoomLevelIterator.next();

                    if (newZoomLevel.getScale() >= roundedScaleDenominator && nextZoomLevel.getScale() < roundedScaleDenominator) {
                        aktZoomLevel = newZoomLevel;
                        BoundingBox newBox = mappingComponent.getBoundingBoxFromScale(
                                aktZoomLevel.getScale());
                        mappingComponent.gotoBoundingBoxWithHistory(newBox);
                        return;
                    }
                }
            }
        }

    }

    public ZoomLevel getZoomLevel() {
        return aktZoomLevel;
    }
    
    private Thread retrieveThread;    

    @Override
    public void retrieve(boolean bol){
                
        log.debug("In TileLayer.retrieve(boolean bol)");
        if(retrieveThread != null && retrieveThread.isAlive()){
            retrieveThread.interrupt();
        }
        retrieveThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                fireRetrievalStarted(new RetrievalEvent());
                if (!threads.isEmpty()) {
                    threads.interrupt();
                }
                
                // Um unnötige retrieves zu vermeiden wird im ZoomModus abgewartet ob noch ein weiteres retrieve erfolgt.
                if ((mappingComponent.getInteractionMode().compareTo(MappingComponent.ZOOM)) == 0) {
                    final Long t = System.currentTimeMillis();
                    while (System.currentTimeMillis() < t + delay) {
//                        try {
//                            Thread.sleep(50);
                            if (retrieveThread.isInterrupted()) {
                                return;
                            }
//                        } catch (InterruptedException e) {
//                            return;
//                        }
                    }
                }

                double scaleDenominator = mappingComponent.getScaleDenominator();
                // aktCamViewScale - scaleDenominator was replaced by Math.abs(...) > ... because some times, aktCanViewScale
                // and ScaleDenominator have a very small difference and this leads to an infinity loop.
                // The reason of this is probably a rounding problem
                if ((Math.abs(aktCamViewScale - scaleDenominator) > 0.00000000002D ) || aktCamViewScale == 0.0) {
                    aktCamViewScale = scaleDenominator;
                    setZoomLevel(scaleDenominator);
                    return;
                }                


                final BoundingBox currentBB = mappingComponent.getCurrentBoundingBox();

                final double diffX = currentBB.getX1() - WUPP_BB.getX1();
                final double diffY = currentBB.getY2() - WUPP_BB.getY1();

                final double meter_pro_tile = tileSize * aktZoomLevel.getResolution();

                final int firstTileXKoordinate = ((int) (diffX / meter_pro_tile)) - (PRE_LOADED_TILES / 2);
                final int firstTileYKoordinate = ((int) (diffY / meter_pro_tile)) + (PRE_LOADED_TILES / 2);

                final int anzNeededXTiles = ((int) (mappingComponent.getWidth() / tileSize)) + PRE_LOADED_TILES;
                final int anzNeededYTiles = ((int) (mappingComponent.getHeight() / tileSize)) + PRE_LOADED_TILES;

                final double camX = mappingComponent.getCamera().
                        getViewBounds().getX();
                final double camY = mappingComponent.getCamera().
                        getViewBounds().getY();
                final double scale = 1 / mappingComponent.getCamera().getViewScale();

                
                //alle benoetigten Tiles zu einer LinkedList hinzufügen falls diese sie nicht schon enthält
                for (int i = 0; i < anzNeededXTiles; i++) {
                    for (int j = 0; j < anzNeededYTiles; j++) {
                        final TileObject tile = new TileObject(aktZoomLevel.getZoomLevel(), firstTileXKoordinate + i, firstTileYKoordinate - j, host, version, layer, format);
                        if (!isLoaded.containsValue(tile)) {
                            final RetrieverWorker tileLoaderThread = new RetrieverWorker(tile, currentBB, tileSize, aktZoomLevel.getResolution(), scale, camX, camY);
                            threads.execute(tileLoaderThread);
                        }
                    }
                }

//                ////////////////////////////////////////////////////////////////////
                // nicht mehr benötigte Tiles aus PNode und HashMap entfernen.
                // Wenn Möglich einen Puffer rund um den aktuell sichtbaren Bereich weiter erhalten.

                final ArrayList<PNode> nodeList = new ArrayList<PNode>();
                final PNode iterationNode = getPNode();
                final ListIterator li = iterationNode.getChildrenIterator();
                final PBounds boundingBoxBounds = currentBB.getPBounds(mappingComponent.getWtst());
                        boundingBoxBounds.setSize(
                                boundingBoxBounds.getWidth() + (PRE_LOADED_TILES * meter_pro_tile),
                                boundingBoxBounds.getHeight() + (PRE_LOADED_TILES * meter_pro_tile));
                        boundingBoxBounds.setOrigin( //bedenken, dass Origin die obere LINKE Ecke benennt
                                boundingBoxBounds.getOrigin().getX() - ((PRE_LOADED_TILES / 2) * meter_pro_tile),
                                boundingBoxBounds.getOrigin().getY() - ((PRE_LOADED_TILES / 2) * meter_pro_tile)); 

                
                while (li.hasNext() && !retrieveThread.isInterrupted()) {
                    Object o = li.next();                                    
                    try {
                        PNode p = (PNode) o;

                        if (!p.getGlobalBounds().intersects(boundingBoxBounds)) {
                            nodeList.add(p);
                            isLoaded.remove(p);                            
                        }                        

                    } catch (Exception e) {
                        log.fatal("Exception", e);//NOI18N
                    }
                }
                iterationNode.removeChildren(nodeList);
                
                while(!threads.allDone()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                       log.warn("Thread interrupted");//NOI18N
                    }
                }
                RetrievalEvent e = new RetrievalEvent();
                e.setIsComplete(true);
                retrievalComplete(e);
            }            
        });
        
        retrieveThread.start();
    }
    

    
    public Element getElement(){
        log.debug("Create XML node for the TileLayer: "+this.getName());//NOI18N
        Element layerConf = new Element("TMSLayer");//       
        layerConf.setAttribute("name",tileSet.toString());
        
        Element curHost = new Element("Host");//NOI18N
        curHost.addContent(tileSet.getHost());
        
        Element version = new Element("Version");//NOI18N
        version.addContent(tileSet.getVersion());
        
        Element srs = new Element("SRS");//NOI18N
        srs.addContent(tileSet.getSRS());
        
        Element boundingBox = new Element("BoundingBox");//NOI18N
        boundingBox.setAttribute("minx", Double.toString(tileSet.getBoundingBox().getX1()));
        boundingBox.setAttribute("miny", Double.toString(tileSet.getBoundingBox().getY1()));
        boundingBox.setAttribute("maxx", Double.toString(tileSet.getBoundingBox().getX2()));
        boundingBox.setAttribute("maxy", Double.toString(tileSet.getBoundingBox().getY2()));
        
        Element resolutions = new Element("Resolutions"); //NOI18N
        String arrayString = Arrays.toString(tileSet.getResolutions());
        arrayString = arrayString.replace("[", "");//NOI18N
        arrayString = arrayString.replace("]", "");//NOI18N
        resolutions.addContent(arrayString);
        
        Element width = new Element("Width");//NOI18N
        width.addContent(Integer.toString(tileSet.getWidth()));
        
        Element height = new Element("Height");//NOI18N
        height.addContent(Integer.toString(tileSet.getHeight()));
        
        Element format = new Element("Format");//NOI18N
        format.addContent(tileSet.getFormat());
        
        Element layers = new Element("Layers");//NOI18N
        layers.addContent(tileSet.getLayer());
        
        Element styles = new Element("Styles");//NOI18N
        styles.addContent(tileSet.getStyle());
        
        layerConf.addContent(curHost);
        layerConf.addContent(version);
        layerConf.addContent(srs);
        layerConf.addContent(boundingBox);
        layerConf.addContent(resolutions);
        layerConf.addContent(width);
        layerConf.addContent(height);
        layerConf.addContent(format);
        layerConf.addContent(layers);
        layerConf.addContent(styles);
        
        log.debug("XML node for the TileLayer: "+this.getName()+" created.");//NOI18N
        
        return layerConf;
    }
 

    public String getHost() {
        return host;
    }

    private class RetrieverWorker extends SwingWorker<XPImage, Void> {

        public RetrieverWorker(TileObject tile, BoundingBox bb, int tileSize, double resolution, double scale, double camX, double camY) {
            this.tile = tile;
            this.tileSize = tileSize;
            this.resolution = resolution;
            this.scale = scale;
            this.bb = bb;
            this.camX = camX;
            this.camY = camY;            
        }
        private final TileObject tile;
        private final int tileSize;
        private final double resolution;
        private final double scale;
        private final BoundingBox bb;
        private final double camX;
        private final double camY;


        @Override
        protected XPImage doInBackground() throws Exception {
            if(!isCancelled()){                
        
                final XPImage xpImage = new XPImage();
                BufferedImage img = new BufferedImage(tileSize,tileSize,BufferedImage.TYPE_INT_ARGB);
                img = tile.loadTile();
                xpImage.setImage(img);
                xpImage.setScale(scale);

                final Point2D point = tile.getOffset(
                        WUPP_BB.getX1(), WUPP_BB.getY1(),
                        bb.getX1(), bb.getY2(),
                        (tileSize * resolution));

                final double offsetX = camX - point.getX();
                final double offsetY = camY - point.getY();

                xpImage.setOffset(offsetX, offsetY);
                return xpImage;
            }
            else 
                return null;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                tile.cancel();
                return;
            }
            try {
                final XPImage res = get();
                if (res != null) {
                        getPNode().addChild(res);
                        isLoaded.put(res, tile);
                }
            } catch (InterruptedException ex) {
                log.error("Exception In SwingWorker.done()", ex);//NOI18N
                tile.cancel();
            } catch (ExecutionException ex) {
                log.error("Exception In SwingWorker.done()", ex);//NOI18N
                tile.cancel();
            }            
        }
        
        
    }    

    private class ThreadList {

        private final List<RetrieverWorker> threadList;
        private final Executor cachedThreadPool = new ThreadPoolExecutor(0, 60,
                                      0L, TimeUnit.SECONDS,
                                      new LinkedBlockingQueue<Runnable>());
        

        public ThreadList() {
            this.threadList = new ArrayList<RetrieverWorker>();
        }

        public void execute(final RetrieverWorker t) {
            log.debug("threadList.size(): " + threadList.size());//NOI18N
            if(!t.isCancelled()){                 
                threadList.add(t);
                cachedThreadPool.execute(t);
            }            
        }

        public void interrupt() {
            for (final RetrieverWorker t : threadList) {
                t.cancel(true);                               
            }
            threadList.clear();
        }

        public boolean isEmpty() {
            return threadList.isEmpty();
        }
        
        public boolean allDone(){
            for(final RetrieverWorker t : threadList){
                if(!t.isDone())
                {                    
                    return false;
                }                
            }
            return true;
        }        
        
    }
}


