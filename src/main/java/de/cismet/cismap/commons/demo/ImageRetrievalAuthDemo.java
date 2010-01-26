package de.cismet.cismap.commons.demo;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.rasterservice.HTTPImageRetrieval;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import java.awt.Image;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.UIManager;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class ImageRetrievalAuthDemo extends javax.swing.JFrame implements RetrievalListener{
    private static final ResourceBundle I18N = ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle", Locale.FRANCE);
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    HTTPImageRetrieval ir;
    /** Creates new form Test */
    public ImageRetrievalAuthDemo() {
       Log4JQuickConfig.configure4LumbermillOnLocalhost();
        log.info("Simple Mapping Client started");
        //ClearLookManager.setMode(ClearLookMode.ON);
        //PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
            //javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            //javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
            //javax.swing.UIManager.setLookAndFeel(new com.jgoodies.plaf.plastic.PlasticXPLookAndFeel());
            //UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
           // UIManager.setLookAndFeel(new PlasticLookAndFeel());
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        } catch (Exception e) {
            log.warn("Fehler beim Einstellen des Look&Feels's!",e);
        }

        initComponents();
        
        prBar.setMaximum(100);
       

        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        prBar = new javax.swing.JProgressBar();
        panMain = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(prBar, java.awt.BorderLayout.SOUTH);

        panMain.setMinimumSize(new java.awt.Dimension(100, 100));
        getContentPane().add(panMain, java.awt.BorderLayout.CENTER);

        jButton1.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, java.awt.BorderLayout.NORTH);

        fileMenu.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.fileMenu.text")); // NOI18N

        openMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.openMenuItem.text")); // NOI18N
        fileMenu.add(openMenuItem);

        saveMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.saveMenuItem.text")); // NOI18N
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.saveAsMenuItem.text")); // NOI18N
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.exitMenuItem.text")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.editMenuItem.text")); // NOI18N

        cutMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.cutMenuItem.text")); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.copyMenuItem.text")); // NOI18N
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.pasteMenuItem.text")); // NOI18N
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.deleteMenuItem.text")); // NOI18N
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.helpMenu.text")); // NOI18N

        contentsMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.contentsMenuItem.text")); // NOI18N
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText(I18N.getString("de.cismet.cismap.commons.demo.ImageRetrievalAuthDemo.aboutMenuItem.text")); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-400)/2, (screenSize.height-300)/2, 400, 300);
    }// </editor-fold>//GEN-END:initComponents
    //new
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        prBar.setValue(0);
        prBar.setIndeterminate(true);        
        if (ir==null) {
            
            ir=new HTTPImageRetrieval(this);
            //ir.setUrl("http://s102w2k1.wuppertal-intra.de/wunda_dk_v61/isserver/ims/scripts/ShowMap.pl?datasource=grundlkarten&VERSION=1.1.1&REQUEST=GetMap&BBOX=2581794.0773859876,5684502.5686845,2581948.756619977,5684588.15786064&WIDTH=750&HEIGHT=415&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_inimage&LAYERS=02_11&STYLES=farbig");
            //ir.setUrl("http://demo.deegree.org:8080/deegree/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image/jpg&TRANSPARENT=false&WIDTH=400&HEIGHT=400&EXCEPTIONS=application/vnd.ogc.se_inimage&BGCOLOR=0xffffff&BBOX=3435804.0066602235,5790675.39930195,3437080.4463269715,5791951.838968698&LAYERS=osnabrueck%3Agruenpolyl,osnabrueck%3Agewaessepoly,osnabrueck%3Agewaesserlinien,osnabrueck%3Astrassen,osnabrueck%3Asights&STYLES=default,default,default,default,default&SRS=EPSG:31467");
            ir.setUrl("http://localhost:8080/deegree2/ogcwebservice?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=674&HEIGHT=527&LAYERS=StateBoundary&TRANSPARENT=TRUE&FORMAT=image/jpg&BBOX=-39862.32289224541,3995357.845975933,919186.2219233183,4745237.049414809&SRS=EPSG:26912&STYLES=");
            ir.start();
        }
        else {
            ir.endRetrieval();
            ir=new HTTPImageRetrieval(this);
            //ir.setUrl("http://s102w2k1.wuppertal-intra.de/wunda_dk_v61/isserver/ims/scripts/ShowMap.pl?datasource=grundlkarten&VERSION=1.1.1&REQUEST=GetMap&BBOX=2581794.0773859876,5684502.5686845,2581948.756619977,5684588.15786064&WIDTH=750&HEIGHT=415&SRS=EPSG:31466&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xF0F0F0&EXCEPTIONS=application/vnd.ogc.se_inimage&LAYERS=02_11&STYLES=farbig");
            //ir.setUrl("http://demo.deegree.org:8080/deegree/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image/jpg&TRANSPARENT=false&WIDTH=400&HEIGHT=400&EXCEPTIONS=application/vnd.ogc.se_inimage&BGCOLOR=0xffffff&BBOX=3435804.0066602235,5790675.39930195,3437080.4463269715,5791951.838968698&LAYERS=osnabrueck%3Agruenpolyl,osnabrueck%3Agewaessepoly,osnabrueck%3Agewaesserlinien,osnabrueck%3Astrassen,osnabrueck%3Asights&STYLES=default,default,default,default,default&SRS=EPSG:31467");
            ir.setUrl("http://localhost:8080/deegree2/ogcwebservice?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=674&HEIGHT=527&LAYERS=StateBoundary&TRANSPARENT=TRUE&FORMAT=image/jpg&BBOX=-39862.32289224541,3995357.845975933,919186.2219233183,4745237.049414809&SRS=EPSG:26912&STYLES=");
            ir.start();
        }

    }//GEN-LAST:event_jButton1ActionPerformed
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ImageRetrievalDemo().setVisible(true);
            }
        });
    }

    public void retrievalStarted(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
    }

    public void retrievalProgress(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        final double p=e.getPercentageDone();
        System.out.println(p);
        if (true||p>prBar.getValue()) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (prBar.isIndeterminate()) {
                        prBar.setIndeterminate(false);
                    }
                    prBar.setValue((int)p);
                    //prBar.setString(new Double(p).toString());
                }
            });        
        }
    }

    public void retrievalError(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        log.fatal("Fehler:"+e.getRetrievedObject());
    }

    public void retrievalComplete(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        Object o=e.getRetrievedObject();
        Image i=null;
        if (o instanceof Image) {
            i=(Image)o;
        }
        panMain.getGraphics().drawImage(i,0,0,this);
    }

    //new
    public void retrievalAborted(de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        prBar.setIndeterminate(false);
        panMain.repaint();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel panMain;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JProgressBar prBar;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
