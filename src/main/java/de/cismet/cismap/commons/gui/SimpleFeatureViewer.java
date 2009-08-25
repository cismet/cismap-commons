/*
 * SimpleFeatureViewer.java
 *
 * Created on 4. M\u00E4rz 2005, 13:58
 */

package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Envelope;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.MappingModelEvent;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PNodeFactory;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.BackgroundRefreshingPanEventListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleSingleSelectionListener;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PImage;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;



/**
 *
 * @author hell
 */
public class SimpleFeatureViewer {//extends PCanvas implements MappingModelListener {
//    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
//
//    private Feature[] currentlyShownFeatures=null;
//    private com.vividsolutions.jts.geom.Envelope currentFeatureEnvelope=null;
//    private MappingModel mappingModel;
//    private HashMap pFeatureHM=new HashMap();
//
//    
//    //Attribute die zum selektieren von PNodes gebraucht werden
//    private PFeature selectedFeature=null;
//
//    private Paint paint=null;
//
//    private WorldToScreenTransform wtst=null;
//    private double clip_offset_x;
//    private double clip_offset_y;
//    
//    private PImage imageBackground=new PImage();  
//    private SimpleWmsGetMapUrl wmsBackgroundUrl;    
//    private boolean wmsBackgroundEnabled=false;
//    private LocalWMSLoaderThread loadWMSBackgroundThread;
//
//    private PLayer featureLayer=new PLayer();
//    private PLayer tmpFeatureLayer=new PLayer();
//    private PLayer backgroundLayer=new PLayer();
//    private PLayer handleLayer=new PLayer();
//    private PLayer rubberBandLayer=new PLayer();
//    
//    
//    public static final Cursor SELECT_CURSOR=new Cursor(Cursor.DEFAULT_CURSOR);
//    public static final Cursor ZOOM_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
//    public static final Cursor PAN_CURSOR=new Cursor(Cursor.HAND_CURSOR);
//    public static final Cursor MOVE_POLYGON_CURSOR=new Cursor(Cursor.HAND_CURSOR);
//    public static final Cursor NEW_POLYGON_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
//    public static final Cursor REMOVE_POLYGON_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
//    public static final Cursor ATTACH_POLYGON_TO_ALPHADATA_CURSOR=new Cursor(Cursor.HAND_CURSOR);
//
//    public static final Cursor MOVE_HANDLE_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
//    public static final Cursor REMOVE_HANDLE_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
//    public static final Cursor NEW_HANDLE_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
//    
//    
//    // "Phantom PCanvas" der nie selbst dargestellt wird
//    // wird nur dazu benutzt das Graphics Objekt up to date
//    // zu halten und dann als Hintergrund von z.B. einem 
//    // Panel zu fungieren
//    // coooooooool, was ? ;-)
//    private PCanvas selectedObjectPresenter=new PCanvas();
//
//     
//    private PInputEventListener zoom=null;
//    private PInputEventListener select=null;
//    private PInputEventListener pan=null;
//    private PInputEventListener motion=null;
//    private PInputEventListener movePolygon=null;
//    private PInputEventListener addPolygons=null;
//    private PInputEventListener deletePolygons=null;
//    private PInputEventListener attachFeature=null;
//    private PInputEventListener splitPoly=null;
//    
//    private boolean editMode=false;
//    
//    private boolean moveHandlesMode=true;
//    private boolean addHandlesMode=false;
//    private boolean removeHandlesMode=false;
//    
//    
//    private boolean zoomMode=false;
//    private boolean panMode=false;
//    private boolean selectionMode=false;
//    private boolean movePolygonMode=false;
//    private boolean newPolygonMode=false;
//    private boolean deletePolygonMode=false;
//    private boolean attachPolygonMode=false;    
//    private boolean splitPolygonMode=false;
//    
//    
//    /** Creates a new instance of SimpleFeatureViewer */
//    public SimpleFeatureViewer() {
//        //remove the default handlers
//        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
//        removeInputEventListener(getPanEventHandler());
//        removeInputEventListener(getZoomEventHandler());
//        addComponentListener(new java.awt.event.ComponentAdapter() {
//            public void componentResized(java.awt.event.ComponentEvent evt) {
//                formComponentResized(evt);
//            }
//        });
//        
//        
//        
//        PRoot root = getRoot();
//       
//        PCamera otherCamera=new PCamera();
//        otherCamera.addLayer(featureLayer);
//        selectedObjectPresenter.setCamera(otherCamera);
//
//        root.addChild(otherCamera);
//        
//        getLayer().addChild(backgroundLayer);
//        getLayer().addChild(featureLayer);
//        getLayer().addChild(tmpFeatureLayer);
//        getLayer().addChild(handleLayer);
//        getLayer().addChild(rubberBandLayer);
//        
//        getCamera().addLayer(0,backgroundLayer);
//        getCamera().addLayer(1,featureLayer);
//        getCamera().addLayer(2,tmpFeatureLayer);
//        getCamera().addLayer(3,handleLayer);
//        getCamera().addLayer(4,rubberBandLayer);
//        
//        otherCamera.setTransparency(0.05f);
//
//        zoom=new RubberBandZoomListener();
//        select=new SimpleSingleSelectionListener();
//        pan=new BackgroundRefreshingPanEventListener();
////        motion=new SimpleMoveListener(this);
////        movePolygon=new FeatureMoveListener(this);
////        addPolygons=new CreatePolygonFeatureListener(this);
////        deletePolygons=new DeleteFeatureListener();
////        attachFeature=new AttachFeatureListener();
////        splitPoly=new SimpleClickDetectionListener();
////        
//        this.addInputEventListener(select);
//        this.addInputEventListener(motion);
//
//        
//        
//        
//    }
//    
//    public PLayer getRubberBandLayer() {
//        return rubberBandLayer;
//    }
//    public PLayer getTmpFeatureLayer() {
//        return tmpFeatureLayer;
//    }
//    public PLayer getFeatureLayer() {
//        return featureLayer;
//    }
//    
//    public PFeature getSolePureNewFeature() {
//        int counter=0;
//        PFeature sole=null;
//        Iterator it=featureLayer.getChildrenIterator();
//        while (it.hasNext()){
//            Object o=it.next();
//            if (o instanceof PFeature) {
//                if (((PFeature)o).getFeature() instanceof PureNewFeature) {
//                    ++counter;
//                    sole=((PFeature)o);
//                }
//            }
//        }
//        if (counter==1) {
//            return sole;
//        }
//        else {
//            return null;
//        }
//    }
//    
//    public PCanvas getSelectedObjectPresenter() {
//        //System.out.println("selectedObjectPresenter:"+selectedObjectPresenter);
//        return selectedObjectPresenter;
//    }
//    
//    public void setBackground(SimpleWmsGetMapUrl wmsBackgroundUrl) {
//        this.wmsBackgroundUrl=wmsBackgroundUrl;
//    }
//    public void setWmsBackgroundEnabled(boolean wmsBackgroundEnabled) {
//        this.wmsBackgroundEnabled=wmsBackgroundEnabled;
//        refreshBackground();
//    }
//    public boolean isWmsBackgroundEnabled() {
//        return wmsBackgroundEnabled;
//    }
//    public void refreshBackground() {
//        log.debug("Enter refreshBackground()");
//        log.debug(wtst);
//        if (loadWMSBackgroundThread!=null &&loadWMSBackgroundThread.isAlive()) {
//            loadWMSBackgroundThread.youngerWMSCall();
//        }
//        loadWMSBackgroundThread=new LocalWMSLoaderThread();  
//        loadWMSBackgroundThread.start();
//        log.debug("Leave refreshBackground()");
//        log.info("Scale:"+getCamera().getViewScale());
//    }
//
//    private class LocalWMSLoaderThread extends Thread {
//       // {start();}
//        private boolean youngerCall=false;
//        public void youngerWMSCall() {
//            youngerCall=true;
//        }
//        public void run() {
//            try { 
//                if (wmsBackgroundUrl!=null && wmsBackgroundEnabled && getWtst()!=null) {
//                    log.debug(wmsBackgroundUrl);
//                    log.debug(new Boolean(wmsBackgroundEnabled).toString());
//                    log.debug(getWtst());
//                    while (getAnimating()==true&&youngerCall==false){
//                       sleep(10);
//                    }
//                    if (youngerCall) return;
//                    PBounds bounds=getCamera().getViewBounds();
//log.fatal(bounds);
//                    wmsBackgroundUrl.setWidth(getWidth());
//                    wmsBackgroundUrl.setHeight(getHeight());
//
//
//                    wmsBackgroundUrl.setX1(getWtst().getSourceX(bounds.getMinX()-getClip_offset_x()));
//                    wmsBackgroundUrl.setY1(getWtst().getSourceY(bounds.getMaxY()-getClip_offset_y()));
//                    wmsBackgroundUrl.setX2(getWtst().getSourceX(bounds.getMaxX()-getClip_offset_x()));
//                    wmsBackgroundUrl.setY2(getWtst().getSourceY(bounds.getMinY()-getClip_offset_y()));
//log.fatal(wmsBackgroundUrl);
//                    try {
//                        log.debug("Lade Bild");
//                        URL u=new URL(wmsBackgroundUrl.toString());
//                        Image image =Toolkit.getDefaultToolkit().getImage(u);
//                        ImageIcon imageLoader = new ImageIcon(image);
//                        do {
//                            sleep(10);
//                        }while (imageLoader.getImageLoadStatus()==MediaTracker.LOADING&&youngerCall==false);
//                        if (youngerCall) {
//                            image=null;
//                            imageLoader=null;
//                            return;
//                        }
//                        final Image background=image;
//                        Runnable doWorkRunnable = new Runnable() {
//                        public void run() {setBackgroundNode(background); }};
//                        SwingUtilities.invokeLater(doWorkRunnable);
//
//                    }
//                    catch (Exception e) {
//                        log.warn("Fehler beim Laden des Hintergrundbildes",e);
//                    }
//                }
//                else {
//                    if (imageBackground!=null) {
//
//        imageBackground.removeFromParent();
//                    }
//                }
//            }
//            catch ( Exception  e ) { 
//                e.printStackTrace();
//                //log.error("Fehler beim Auslesen der Flaecheninformationen (Uebersicht)!",e); 
//            }
//        }
//    }
//    
//    
//    
//    private void setBackgroundNode(Image image) {
//        if (imageBackground!=null) {
//            imageBackground.removeFromParent();
//            //imageBackground.setImage((Image)null);
//            //imageBackground=null;
//            //System.gc();
//        }
//        imageBackground.setImage(image);
//
////        getLayer().addChild(imageBackground); 
//        backgroundLayer.addChild(imageBackground); 
//
//        imageBackground.setScale(1/getCamera().getViewScale());
//        imageBackground.setOffset(getCamera().getViewBounds().getOrigin());
//        imageBackground.moveToBack();
//    }
//    public void setMappingModel(MappingModel mm) {
//        mappingModel=mm;
//        mappingModel.addMappingModelListener(this);
//        
//    }
//    public MappingModel getMappingModel() {
//        return mappingModel;
//    }    
//    public void mapChanged(MappingModelEvent mme) {
//        if (mappingModel!=null) {
//            backgroundLayer.removeAllChildren();
//            repaint();
//            featureLayer.removeAllChildren();
//            Vector features=getFeatureCollection().getAllFeatures();
//            if (features!=null&&features.size()!=0) {
//                Feature[] fa=new Feature[features.size()];
//                fa=(Feature[])features.toArray(fa);
//                showFeatures(fa);
//            }
//
//            
//            
//            if (featureLayer.getChildrenCount()==0){
//                PBounds bounds=new PBounds(0,0,getWidth(),getHeight());
//                getCamera().animateViewToCenterBounds(bounds, true, 500);
//              
//                gotoInitialBB();
//            }
//        }
//        repaint();
//        if (isWmsBackgroundEnabled()) {
//            refreshBackground();
//        }
//    }
//    
//    double initial_x1=0;
//    double initial_y1=0;
//    double initial_x2=0;
//    double initial_y2=0;
//    
//    
//    public void setInitialBB(double x1,double y1,double x2,double y2){
//        initial_x1=x1;
//        initial_y1=y1;
//        initial_x2=x2;
//        initial_y2=y2;
//    }
//    
//    
//    public void gotoInitialBB() {
//        showFeatures(new Feature[0],new Envelope(initial_x1,initial_x2,initial_y1,initial_y2));        
//    }
//
//    public void reconsiderFeature(Feature f) {
//        if (f!=null) {
//            PFeature node=((PFeature)pFeatureHM.get(f));
//            node.visualize();
//            repaint();
////            if ((f instanceof StyledFeature)) {
////                java.awt.Paint paint=((StyledFeature)f).getFillingStyle();
////                if (paint!=null) {
////                    
////                    node.setNonSelectedPaint(paint);
////                    node.setNonHighlightingPaint(paint);
////                }
////                if (!node.isSelected()){
////                    //node.setPaint(paint);
////                }
////                
////            }
//        }
//    }
//    public void removeSingleFeature(Feature f) {
//        if (f!=null) {
//            PFeature node=(PFeature)pFeatureHM.get(f);
//            if (node!=null) {
//                featureLayer.removeChild(node);
//            }
//        }
//    }
//    
//    public void selectionChanged(MappingModelEvent mme) {
//        Feature f=mme.getFeature();
//        if (f==null) {
//            selectPFeatureManually(null);
//        }
//        else {
//            PFeature fp=((PFeature)pFeatureHM.get(f));
//            if (fp!=null&&fp.getFeature()!=null&&fp.getFeature().getGeometry()!=null) {
////                PNode p=fp.getChild(0);
//                selectPFeatureManually(fp);
//            }
//            else {
//                selectPFeatureManually(null);
//            }
//        }
//    }
//    public PNode getSelectedNode() {
//        return selectedFeature;
//    }
//    
//    
//    public void selectPFeatureManually(PFeature feature) {
//        if (feature==null) {
//            handleLayer.removeAllChildren();
//            if (selectedFeature!=null) {
//                selectedFeature.setSelected(false);
//            }
//        }
//        else if (feature==selectedFeature) {
//            return;
//        }
//        else {
//            if (selectedFeature!=null) {
//                selectedFeature.setSelected(false);
//            }
//            feature.setSelected(true);
//            selectedFeature=feature;
//
//
//            //Fuer den selectedObjectPresenter (Eigener PCanvas)
//            syncSelectedObjectPresenter(1000);
//            
//            handleLayer.removeAllChildren();
//            if (isInEditMode()&&(this.isInSelectionMode()||this.isInPanMode()||this.isInZoomMode())) {
//                selectedFeature.addHandles(handleLayer);
//            }
//      }
//    }
//    
//    public void syncSelectedObjectPresenter(int ms) {
//        selectedObjectPresenter.setVisible(true);
//        selectedObjectPresenter.getCamera().animateViewToCenterBounds(selectedFeature.getBounds(), true,1000);
//    }
//    
//    
//    public void zoomToFullBounds() {
//        if(featureLayer.getChildrenCount()>0) {
//            log.info("zoomToFullBounds:featureLayer.getChildrenCount()>0)");
//            //            boolean background=false;
////            if (this.imageBackground!=null) {
////                imageBackground.removeFromParent();
////                background=true;
////            }
//            PBounds bounds=featureLayer.getFullBounds();
////            if (background) {
////                featureLayer.addChild(imageBackground); 
//////              imageBackground.setScale(1/getCamera().getViewScale());
//////                imageBackground.setOffset(getCamera().getViewBounds().getOrigin());
////                imageBackground.moveToBack();                
////            }
//            getCamera().animateViewToCenterBounds(bounds, true, 500);
//            refreshBackground();
//            log.info("Scale:"+getCamera().getViewScale());
//        }
//        else  if(backgroundLayer.getChildrenCount()>0) {
//            log.info("zoomToFullBounds:backgroundLayer.getChildrenCount()>0");
//            //System.out.println(":................");
//            PBounds bounds=new PBounds(0,0,getWidth(),getHeight());
//            getCamera().animateViewToCenterBounds(bounds, true, 500);
//        }
//        else {
//            log.info("zoomToFullBounds:kein Zoom");
//        }
//        
//        
//    }
//    
//    
//    public void showFeatures(Feature[] features) {
//        com.vividsolutions.jts.geom.Envelope env=computeFeatureEnvelope(features);
//        showFeatures(features,env);
//    }
//    
//    
//    public void showFeatures(Feature[] features,com.vividsolutions.jts.geom.Envelope featureEnvelope) {
//        handleLayer.removeAllChildren();
//        pFeatureHM.clear();
//        currentlyShownFeatures=features;
//        currentFeatureEnvelope=featureEnvelope;
//        featureLayer.removeAllChildren();
//        
//        double y_real=featureEnvelope.getHeight();
//        double x_real=featureEnvelope.getWidth();
//        
//        double clip_height;
//        double clip_width;
//        
//        double x_screen=getWidth();
//        double y_screen=getHeight();
//        
//        if (x_real/x_screen>=y_real/y_screen) { //X ist Bestimmer d.h. x wird nicht ver\u00E4ndert
//            // X ist Bestimmer
//            clip_height=x_screen*y_real/x_real;
//            clip_width=x_screen;
//            clip_offset_y=(y_screen-clip_height)/2;
//            clip_offset_x=0;
//        }
//        else {
//            // Y ist Bestimmer
//            clip_height=y_screen;
//            clip_width=y_screen*x_real/y_real;
//            clip_offset_y=0;
//            clip_offset_x=(x_screen-clip_width)/2;
//        }
//
//        
////        wtst= new WorldToScreenTransform(featureEnvelope.getMinX(),featureEnvelope.getMinY(),featureEnvelope.getMaxX(),featureEnvelope.getMaxY(),0,0,clip_width,clip_height);
//        wtst= new WorldToScreenTransform(0,0);
//        
//        //getLayer().setTransform(AffineTransform.getScaleInstance(1.0, -1.0));
//       
//        for (int i=0;i<features.length;++i) {
//             PFeature p=new PFeature(features[i],wtst,clip_offset_x,clip_offset_y);
//             //p.setViewer(this);
//             //So kann man es Piccolo \u00FCberlassen (m\u00FCsste nur noch ein transformation machen, die die y achse spiegelt) 
//             //PNode p=PNodeFactory.createPNode(features[i],null,0,0);
//             //((PPath)p).setStroke(new BasicStroke(0.5f));
//             if (features[i].getGeometry()!=null) {
//                pFeatureHM.put(p.getFeature(), p);
//                //((PPath)p).setStroke(new BasicStroke(0.5f));
//                featureLayer.addChild(p);
//             }
//
//        }
//        
//        zoomToFullBounds();
//        refreshBackground();
//    }
//    
//    
//    public void refreshHM(PFeature p){
//        pFeatureHM.put(p.getFeature(), p);
//    }
//    public void removeFromHM(Feature f) {
//        PFeature pf=(PFeature)pFeatureHM.get(f);
//        if (pf!=null) {
//            pFeatureHM.remove(f);
//            try {
//                featureLayer.removeChild(pf);
//            }
//            catch (Exception ex) {
//                log.debug("Remove Child ging Schief. Ist beim Splitten aber normal.",ex);
//            }
//        }
//    }
//    
//    
//
//    private com.vividsolutions.jts.geom.Envelope computeFeatureEnvelope(Feature[] features) {
//        PNode root =new PNode();
//        for (int i=0;i<features.length;++i) {
//            PNode p=PNodeFactory.createPFeature(features[i]);
//            if (p!=null) {
//                root.addChild(p);
//            }
//        }
//        PBounds ext=root.getFullBounds();
//        com.vividsolutions.jts.geom.Envelope env=new com.vividsolutions.jts.geom.Envelope(ext.x,ext.x+ext.width,ext.y,ext.y+ext.height);
//        return env;
//    }
//    public void refreshFeatures() {
//        if (this.currentlyShownFeatures!=null) {
//            if (currentFeatureEnvelope!=null) {
//                this.showFeatures(currentlyShownFeatures, currentFeatureEnvelope);
//            }
//            else {
//                this.showFeatures(currentlyShownFeatures);
//            }
//        }
//        
//    }
//    
//    public void zoomToSelectedNode() {
//        if (this.selectedFeature!=null) {
//           getCamera().animateViewToCenterBounds(selectedFeature.getBounds(), true, 500); 
//           this.refreshBackground();
//        }
//    }
//    
//    //Auf Gr\u00F6\u00DFen\u00E4nderung reagieren
//
//    private void formComponentResized(java.awt.event.ComponentEvent evt) {                                      
//       log.info("formComponentResized");
//        if(featureLayer.getChildrenCount()>0) {
//            PBounds bounds=featureLayer.getFullBounds();
//            getCamera().animateViewToCenterBounds(bounds, true, 500);
//            refreshBackground();
//       }
//       else if (backgroundLayer.getChildrenCount()>0) {
//            log.info("resize: keine Features, aber Hintergrund");
//           //            PBounds bounds=new PBounds(0,0,getWidth(),getHeight());
////            getCamera().animateViewToCenterBounds(bounds, true, 500);
//           this.refreshFeatures();
//           refreshBackground();
//       }
//    }
//
//    public boolean isInEditMode() {
//        return editMode;
//    }
//    public void setEditMode(boolean editMode) {
//        handleLayer.removeAllChildren();
//        this.editMode=editMode;   
//        if (!editMode) {
//            //handleLayer.removeAllChildren();
//        }
//        else {
//            try {
//                if (getSelectedNode()!=null) {
//                    ((PFeature)getSelectedNode()).addHandles(handleLayer);
//                }
//            }
//            catch (ClassCastException cce) {
//                log.error("getSelectedNode() kein PFeature",cce);
//            }
//        }
//    }    
//    public boolean isInMoveHanlesMode() {
//        return moveHandlesMode;
//    }
//    public boolean isInAddHandlesMode() {
//        return addHandlesMode;
//    }
//    public boolean isInRemoveHandlesMode() {
//        return removeHandlesMode;
//    }
//    
//    public void activateMoveHandlesMode() {
//        moveHandlesMode=true;
//        addHandlesMode=false;
//        removeHandlesMode=false;    
//    }
//    public void activateAddHandlesMode() {
//        moveHandlesMode=false;
//        addHandlesMode=true;
//        removeHandlesMode=false;    
//    }
//    public void activateRemoveHandlesMode() {
//        moveHandlesMode=false;
//        addHandlesMode=false;
//        removeHandlesMode=true;    
//    }
//    
//
//    public void activateZoomMode() {
//        resetModes();
//        zoomMode=true;
//        this.addInputEventListener(zoom);
//    }
//    public void activatePanMode() {
//        resetModes();
//        panMode=true;
//        this.addInputEventListener(pan);
//    }
//    public void activateSelectionMode() {
//        resetModes();
//        selectionMode=true;
//        this.addInputEventListener(select);
//    }
//    public void activateMovePolygonMode() {
//        resetModes();
//        movePolygonMode=true;
//        handleLayer.removeAllChildren();
//        this.addInputEventListener(movePolygon);
//    }
//    public void activateNewPolygonMode() {
//        resetModes();
//        newPolygonMode=true;
//        handleLayer.removeAllChildren();        
////        this.selectPFeatureManually(null);
//        this.addInputEventListener(addPolygons);
//    }
//    public void activateDeletePolygonMode() {
//        resetModes();
//        deletePolygonMode=true;
//        handleLayer.removeAllChildren();
//        this.addInputEventListener(deletePolygons);
//    }
//    public void activateAttachPolygonMode() {
//        resetModes();
//        handleLayer.removeAllChildren();
//        attachPolygonMode=true;
//        this.addInputEventListener(attachFeature);
//    }
//    public void activateSplitPolygonMode() {
//        resetModes();
//        splitPolygonMode=true;
//        this.addInputEventListener(splitPoly);
//    }
//    
//    
//    private void resetModes() {
//        zoomMode=false;
//        panMode=false;
//        selectionMode=false;
//        movePolygonMode=false;
//        newPolygonMode=false;
//        deletePolygonMode=false;
//        attachPolygonMode=false;
//        this.removeInputEventListener(zoom);
//        this.removeInputEventListener(select);
//        this.removeInputEventListener(pan);
//        this.removeInputEventListener(movePolygon);
//        this.removeInputEventListener(addPolygons);
//        this.removeInputEventListener(deletePolygons);
//        this.removeInputEventListener(attachFeature);
//        this.removeInputEventListener(splitPoly);
//    }
//    
//    public boolean isInZoomMode() {
//        return zoomMode;
//    }
//    public boolean isInPanMode() {
//        return panMode;
//    }
//    public boolean isInSelectionMode() {
//        return selectionMode;
//    }
//    public PInputEventListener getSelectionHandler() {
//        return select;
//    }
//    public PInputEventListener getMovePolygonHandler() {
//        return movePolygon;
//    }
//    public PInputEventListener getDeleteFeatureListener() {
//        return deletePolygons;
//    }
//    public PInputEventListener getAttachFeatureListener() {
//        return attachFeature;
//    }
//    public PInputEventListener getSplitPolygonListener() {
//        return splitPoly;
//    }
//    
//    
//    
//    
//    public boolean isInMovePolygonMode() {
//        return movePolygonMode;
//    }
//    public boolean isInNewPolygonMode() {
//        return newPolygonMode;
//    }
//    public boolean isInDeletePolygonMode() {
//        return deletePolygonMode;
//    }
//    public boolean isInAttachPolygonMode() {
//        return attachPolygonMode;
//    }
//    public boolean isInSplitPolygonMode() {
//        return splitPolygonMode;
//    }
//    
//    public WorldToScreenTransform getWtst() {
//        return wtst;
//    }
//
//    public void setWtst(WorldToScreenTransform wtst) {
//        this.wtst = wtst;
//    }
//
//    public double getClip_offset_x() {
//        return clip_offset_x;
//    }
//
//    public void setClip_offset_x(double clip_offset_x) {
//        this.clip_offset_x = clip_offset_x;
//    }
//
//    public double getClip_offset_y() {
//        return clip_offset_y;
//    }
//
//    public void setClip_offset_y(double clip_offset_y) {
//        this.clip_offset_y = clip_offset_y;
//    }
//
//    public void featureCollectionChanged(MappingModelEvent mme) {
//    }
//
//    public void rasterServiceLayerStructureChanged(MappingModelEvent mme) {
//    }
//
//    public void rasterServiceRemoved(de.cismet.cismap.commons.rasterservice.RasterService rasterService) {
//    }
//
//    public void rasterServiceAdded(de.cismet.cismap.commons.rasterservice.RasterService rasterService) {
//    }
//    
}