/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * PrintingWidget.java
 *
 * Created on 10. Juli 2006, 17:55
 */
package de.cismet.cismap.commons.gui.printing;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.HeadlessMapProvider.NotificationLevel;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.gui.Static2DTools;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.Download;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;

import static de.cismet.cismap.commons.HeadlessMapProvider.NotificationLevel.*;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PrintingWidget extends javax.swing.JDialog implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PrintingWidget.class);
    private static final boolean DEBUG = Debug.DEBUG;
    public static final String BB_MIN_X = "minX";
    public static final String BB_MIN_Y = "minY";
    public static final String BB_MAX_X = "maxX";
    public static final String BB_MAX_Y = "maxY";

    //~ Instance fields --------------------------------------------------------

    PDFCreatingWaitDialog pdfWait;
    private MappingComponent mappingComponent = null;
    private String interactionModeAfterPrinting = "";            // NOI18N
    private AbstractPrintingInscriber inscriber = null;
    private ImageIcon errorImage = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/error.png")); // NOI18N
    private Style styleTip;
    private Style styleSuccess;
    private Style styleInfo;
    private Style styleExpert;
    private Style styleWarn;
    private Style styleError;
    private Style styleErrorReason;
    private EnumMap<NotificationLevel, Style> styles = new EnumMap<NotificationLevel, Style>(NotificationLevel.class);
    private HeadlessMapProvider headlessMapProvider;
    private Future<Image> futureMapImage;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdBack;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lbl1;
    private javax.swing.JLabel lbl2;
    private javax.swing.JPanel panDesc;
    private javax.swing.JPanel panInscribe;
    private javax.swing.JPanel panLoadAndInscribe;
    private javax.swing.JPanel panProgress;
    private javax.swing.JProgressBar prbLoading;
    private javax.swing.JScrollPane scpLoadingStatus;
    private javax.swing.JTextPane txpLoadingStatus;
    private javax.swing.JTextField txt1;
    private javax.swing.JTextField txt2;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form PrintingWidget.
     *
     * @param  modal             DOCUMENT ME!
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PrintingWidget(final boolean modal, final MappingComponent mappingComponent) {
        super(StaticSwingTools.getParentFrame(mappingComponent), modal);
        final Runnable t = new Thread("PrintingWidget PDFCreatingWaitDialog()") {

                @Override
                public void run() {
                    pdfWait = new PDFCreatingWaitDialog(StaticSwingTools.getParentFrame(mappingComponent), true);
                }
            };
        CismetThreadPool.execute(t);
        this.mappingComponent = mappingComponent;
        initComponents();
        panDesc.setBackground(new Color(216, 228, 248));
        getRootPane().setDefaultButton(cmdOk);
        txpLoadingStatus.setBackground(this.getBackground());
        prbLoading.setForeground(panDesc.getBackground());
        styleTip = txpLoadingStatus.addStyle(TIP.name(), null);
        StyleConstants.setForeground(styleTip, Color.blue);
        StyleConstants.setFontSize(styleTip, 10);
        styles.put(TIP, styleTip);
        styleSuccess = txpLoadingStatus.addStyle(SUCCESS.name(), null);
        StyleConstants.setForeground(styleSuccess, Color.green.darker());
        StyleConstants.setFontSize(styleSuccess, 10);

        styles.put(SUCCESS, styleSuccess);
        styleInfo = txpLoadingStatus.addStyle(INFO.name(), null);
        StyleConstants.setForeground(styleInfo, Color.DARK_GRAY);
        StyleConstants.setFontSize(styleInfo, 10);
        styles.put(INFO, styleInfo);
        styleExpert = txpLoadingStatus.addStyle(EXPERT.name(), null);
        StyleConstants.setForeground(styleExpert, Color.gray);
        StyleConstants.setFontSize(styleExpert, 10);
        styles.put(EXPERT, styleExpert);
        styleWarn = txpLoadingStatus.addStyle(WARN.name(), null);
        StyleConstants.setForeground(styleWarn, Color.orange.darker());
        StyleConstants.setFontSize(styleWarn, 10);
        styles.put(WARN, styleWarn);
        styleError = txpLoadingStatus.addStyle(NotificationLevel.ERROR.name(), null);
        StyleConstants.setForeground(styleError, Color.red);
        StyleConstants.setFontSize(styleError, 10);
        StyleConstants.setBold(styleError, true);
        styles.put(NotificationLevel.ERROR, styleError);
        styleErrorReason = txpLoadingStatus.addStyle(ERROR_REASON.name(), null);
        StyleConstants.setForeground(styleErrorReason, Color.red);
        StyleConstants.setFontSize(styleErrorReason, 10);
        styles.put(ERROR_REASON, styleErrorReason);

        StaticSwingTools.setNiftyScrollBars(scpLoadingStatus);
        // txpLoadingStatus.setContentType("text/html");

        headlessMapProvider = HeadlessMapProvider.createHeadlessMapProviderAndAddLayers(mappingComponent);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   modal             DOCUMENT ME!
     * @param   mappingComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PrintingWidget cloneWithNewParent(final boolean modal, final MappingComponent mappingComponent) {
        final PrintingWidget newWidget = new PrintingWidget(modal, mappingComponent);
        return newWidget;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lbl1 = new javax.swing.JLabel();
        txt1 = new javax.swing.JTextField();
        lbl2 = new javax.swing.JLabel();
        txt2 = new javax.swing.JTextField();
        panDesc = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        cmdOk = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();
        panLoadAndInscribe = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        panInscribe = new javax.swing.JPanel();
        panProgress = new javax.swing.JPanel();
        scpLoadingStatus = new javax.swing.JScrollPane();
        txpLoadingStatus = new javax.swing.JTextPane();
        prbLoading = new javax.swing.JProgressBar();
        cmdBack = new javax.swing.JButton();

        lbl1.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.lbl1.text")); // NOI18N

        txt1.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.txt1.text")); // NOI18N

        lbl2.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.lbl2.text")); // NOI18N

        txt2.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.txt2.text")); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.title")); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {

                @Override
                public void componentShown(final java.awt.event.ComponentEvent evt) {
                    formComponentShown(evt);
                }
            });

        panDesc.setBackground(java.awt.SystemColor.inactiveCaptionText);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));                                                        // NOI18N
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.jLabel4.text")); // NOI18N

        jLabel5.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/frameprint.png"))); // NOI18N

        final org.jdesktop.layout.GroupLayout panDescLayout = new org.jdesktop.layout.GroupLayout(panDesc);
        panDesc.setLayout(panDescLayout);
        panDescLayout.setHorizontalGroup(
            panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                jSeparator3,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                286,
                Short.MAX_VALUE).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                panDescLayout.createSequentialGroup().addContainerGap(146, Short.MAX_VALUE).add(jLabel5)
                            .addContainerGap()).add(
                panDescLayout.createSequentialGroup().addContainerGap().add(
                    panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        panDescLayout.createSequentialGroup().add(
                            jSeparator2,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            262,
                            Short.MAX_VALUE).addContainerGap()).add(
                        panDescLayout.createSequentialGroup().add(
                            panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                                jLabel1).add(jLabel2).add(jLabel3).add(jLabel4)).add(83, 83, 83)))));
        panDescLayout.setVerticalGroup(
            panDescLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                panDescLayout.createSequentialGroup().addContainerGap().add(jLabel1).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator2,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    2,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel2).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel3).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel4).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED,
                    58,
                    Short.MAX_VALUE).add(jLabel5).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator3,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        cmdOk.setMnemonic('O');
        cmdOk.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.cmdOk.text")); // NOI18N
        cmdOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOkActionPerformed(evt);
                }
            });

        cmdCancel.setMnemonic('A');
        cmdCancel.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.cmdCancel.text")); // NOI18N
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCancelActionPerformed(evt);
                }
            });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11));                                                        // NOI18N
        jLabel6.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.jLabel6.text")); // NOI18N

        panInscribe.setLayout(new java.awt.BorderLayout());

        txpLoadingStatus.setBackground(java.awt.SystemColor.control);
        txpLoadingStatus.setEditable(false);
        scpLoadingStatus.setViewportView(txpLoadingStatus);

        prbLoading.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        prbLoading.setBorderPainted(false);

        final org.jdesktop.layout.GroupLayout panProgressLayout = new org.jdesktop.layout.GroupLayout(panProgress);
        panProgress.setLayout(panProgressLayout);
        panProgressLayout.setHorizontalGroup(
            panProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                prbLoading,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                435,
                Short.MAX_VALUE).add(
                scpLoadingStatus,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                435,
                Short.MAX_VALUE));
        panProgressLayout.setVerticalGroup(
            panProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                panProgressLayout.createSequentialGroup().add(
                    scpLoadingStatus,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    101,
                    Short.MAX_VALUE).add(4, 4, 4).add(
                    prbLoading,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    9,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        final org.jdesktop.layout.GroupLayout panLoadAndInscribeLayout = new org.jdesktop.layout.GroupLayout(
                panLoadAndInscribe);
        panLoadAndInscribe.setLayout(panLoadAndInscribeLayout);
        panLoadAndInscribeLayout.setHorizontalGroup(
            panLoadAndInscribeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                panLoadAndInscribeLayout.createSequentialGroup().addContainerGap().add(jLabel6).add(148, 148, 148)).add(
                jSeparator4,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                459,
                Short.MAX_VALUE).add(
                panLoadAndInscribeLayout.createSequentialGroup().addContainerGap().add(
                    jSeparator1,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    447,
                    Short.MAX_VALUE)).add(
                panLoadAndInscribeLayout.createSequentialGroup().addContainerGap().add(
                    panProgress,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addContainerGap()).add(
                panLoadAndInscribeLayout.createSequentialGroup().addContainerGap().add(
                    panInscribe,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    435,
                    Short.MAX_VALUE).addContainerGap()));
        panLoadAndInscribeLayout.setVerticalGroup(
            panLoadAndInscribeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                panLoadAndInscribeLayout.createSequentialGroup().addContainerGap().add(jLabel6).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator1,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panInscribe,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    125,
                    Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    panProgress,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    org.jdesktop.layout.LayoutStyle.RELATED).add(
                    jSeparator4,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                    org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

        cmdBack.setText(org.openide.util.NbBundle.getMessage(PrintingWidget.class, "PrintingWidget.cmdBack.text")); // NOI18N
        cmdBack.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdBackActionPerformed(evt);
                }
            });

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                layout.createSequentialGroup().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                        layout.createSequentialGroup().add(
                            panDesc,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            panLoadAndInscribe,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                            Short.MAX_VALUE)).add(
                        org.jdesktop.layout.GroupLayout.TRAILING,
                        layout.createSequentialGroup().add(
                            cmdCancel,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            125,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            cmdBack,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            125,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                            org.jdesktop.layout.LayoutStyle.RELATED).add(
                            cmdOk,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                            126,
                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                layout.createSequentialGroup().add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
                        panLoadAndInscribe,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE).add(
                        panDesc,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(cmdOk).add(cmdCancel).add(
                        cmdBack)).addContainerGap()));

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdBackActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdBackActionPerformed
        dispose();
    }                                                                           //GEN-LAST:event_cmdBackActionPerformed

    /**
     * DOCUMENT ME!
     */
    public void startLoading() {
        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("startLoading()");                        // NOI18N
            }
        }
        txpLoadingStatus.setText("");                               // NOI18N
        try {
            final Class c = Class.forName(mappingComponent.getPrintingSettingsDialog().getSelectedTemplate()
                            .getClassName());
            final Constructor constructor = c.getConstructor();
            inscriber = (AbstractPrintingInscriber)constructor.newInstance();
        } catch (Exception e) {
            LOG.error("Error while loading the print template", e); // NOI18N
        }
        panInscribe.removeAll();
        panInscribe.add(inscriber, BorderLayout.CENTER);

        cmdOk.setEnabled(false);
        final Template t = mappingComponent.getPrintingSettingsDialog().getSelectedTemplate();

        final Resolution r = mappingComponent.getPrintingSettingsDialog().getSelectedResolution();
        addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
                PrintingWidget.class,
                "PrintingWidget.startLoading().msg",
                new Object[] { r.getResolution() }),
            EXPERT); // NOI18N

        headlessMapProvider.addPropertyChangeListener(this);

        final BoundingBox bb =
            ((PrintingFrameListener)mappingComponent.getInputListener(MappingComponent.PRINTING_AREA_SELECTION))
                    .getPrintingBoundingBox();
        // transform BoundingBox to XBoundingBox
        final Crs crs = mappingComponent.getMappingModel().getSrs();
        final boolean isMetric = crs.isMetric();
        final XBoundingBox xbb = new XBoundingBox(bb.getX1(),
                bb.getY1(),
                bb.getX2(),
                bb.getY2(),
                crs.getCode(),
                isMetric);
        headlessMapProvider.setBoundingBox(xbb);

        futureMapImage = headlessMapProvider.getImage((int)PrintingFrameListener.DEFAULT_JAVA_RESOLUTION_IN_DPI,
                r.getResolution(),
                t.getMapWidth(),
                t.getMapHeight());

        if (DEBUG) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("BoundingBox:" + bb); // NOI18N
            }
        }

        prbLoading.setIndeterminate(true);

        super.pack();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void formComponentShown(final java.awt.event.ComponentEvent evt) { //GEN-FIRST:event_formComponentShown
    }                                                                          //GEN-LAST:event_formComponentShown

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdCancelActionPerformed
        mappingComponent.setInteractionMode(interactionModeAfterPrinting);
        mappingComponent.getPrintingFrameLayer().removeAllChildren();
        dispose();
    }                                                                             //GEN-LAST:event_cmdCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOkActionPerformed
        final Runnable t = new Thread("PrintingWidget actionPerformed") {

                @Override
                public void run() {
                    final Action a = mappingComponent.getPrintingSettingsDialog().getSelectedAction();
                    if (a.getId().equalsIgnoreCase(Action.PDF)) {
                        java.awt.EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    StaticSwingTools.showDialog(pdfWait);
                                }
                            });
                    }
                    final Template t = mappingComponent.getPrintingSettingsDialog().getSelectedTemplate();
                    final Scale s = mappingComponent.getPrintingSettingsDialog().getSelectedScale();
                    mappingComponent.getPrintingFrameLayer().removeAllChildren();
                    mappingComponent.setInteractionMode(interactionModeAfterPrinting);
                    if (DEBUG) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("interactionModeAfterPrinting:" + interactionModeAfterPrinting); // NOI18N
                        }
                    }

                    try {
                        final HashMap param = new HashMap();
                        param.put(t.getMapPlaceholder(), futureMapImage.get());
                        String scaleDenomString = "" + s.getDenominator();                                            // NOI18N
                        if (scaleDenomString.equals("0") || scaleDenomString.equals("-1"))                            // NOI18N
                        {
                            final int sd = (int)
                                (((PrintingFrameListener)mappingComponent.getInputListener(
                                            MappingComponent.PRINTING_AREA_SELECTION)).getScaleDenominator() + 0.5d); // +0.5=Runden
                            scaleDenomString = "" + sd;                                                               // NOI18N
                        }
                        param.put(t.getScaleDemoninatorPlaceholder(), scaleDenomString);
                        param.putAll(inscriber.getValues());

                        final BoundingBox bbox =
                            ((PrintingFrameListener)mappingComponent.getInputListener(
                                    MappingComponent.PRINTING_AREA_SELECTION)).getPrintingBoundingBox();
                        param.put(BB_MIN_X, bbox.getX1());
                        param.put(BB_MIN_Y, bbox.getY1());
                        param.put(BB_MAX_X, bbox.getX2());
                        param.put(BB_MAX_Y, bbox.getY2());

                        if (DEBUG) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Parameter:" + param); // NOI18N
                            }
                        }

                        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(getClass()
                                        .getResourceAsStream(t.getFile()));
                        final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param);

                        if (a.getId().equalsIgnoreCase(Action.PRINTPREVIEW)) {
                            final JRViewer aViewer = new JRViewer(jasperPrint);
                            final JFrame aFrame = new JFrame(org.openide.util.NbBundle.getMessage(
                                        PrintingWidget.class,
                                        "PrintingWidget.cmdOKActionPerformed(ActionEvent).aFrame.title")); // NOI18N
                            aFrame.getContentPane().add(aViewer);
                            final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                            aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
                            final java.awt.Insets insets = aFrame.getInsets();
                            aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
                                aFrame.getHeight()
                                        + insets.top
                                        + insets.bottom
                                        + 20);
                            aFrame.setLocationRelativeTo(PrintingWidget.this);
                            aFrame.setVisible(true);
                        } else if (a.getId().equalsIgnoreCase(Action.PDF)) {
                            if (mappingComponent.getPrintingSettingsDialog().isChooseFileName()) {
                                final File file = StaticSwingTools.chooseFile(DownloadManager.instance()
                                                .getDestinationDirectory().getAbsolutePath(),
                                        true,
                                        new String[] { "pdf" },
                                        "PDF (.pdf)",
                                        PrintingWidget.this.mappingComponent);

                                if (file != null) {
                                    final JasperDownload jd = new JasperDownload(
                                            jasperPrint,
                                            file.getParent(),
                                            "Cismap-Druck",
                                            file.getName().substring(0, file.getName().indexOf(".")));

                                    jd.setFileToSaveTo(file);

                                    if (DownloadManager.instance().getDownloads().contains(jd)) {
                                        // Previous downloads, that uses the same destination file, must be removed, so
                                        // that the new download can be executed
                                        final List<Download> downloads = new ArrayList(DownloadManager.instance()
                                                        .getDownloads());
                                        final int index = downloads.indexOf(jd);

                                        if (index != -1) {
                                            final Download d = downloads.get(index);
                                            DownloadManager.instance().removeDownload(d);
                                        }
                                    }

                                    DownloadManager.instance().add(jd);
                                }
                            } else if (DownloadManagerDialog.showAskingForUserTitle(
                                            PrintingWidget.this.mappingComponent)) {
                                final String jobname = DownloadManagerDialog.getJobname();
                                DownloadManager.instance()
                                        .add(new JasperDownload(jasperPrint, jobname, "Cismap-Druck", "cismap"));
                            }

                            java.awt.EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (pdfWait.isVisible()) {
                                            pdfWait.dispose();
                                        }
                                    }
                                });
                        } else if (a.getId().equalsIgnoreCase(Action.PRINT)) {
                            JasperPrintManager.printReport(jasperPrint, true);
                        }
                    } catch (Throwable tt) {
                        LOG.error("Error during Jaspern", tt); // NOI18N

                        final ErrorInfo ei = new ErrorInfo(org.openide.util.NbBundle.getMessage(
                                    PrintingWidget.class,
                                    "PrintingWidget.cmdOKActionPerformed(ActionEvent).ErrorInfo.title"),   // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    PrintingWidget.class,
                                    "PrintingWidget.cmdOKActionPerformed(ActionEvent).ErrorInfo.message"), // NOI18N
                                null,
                                null,
                                tt,
                                Level.ALL,
                                null);
                        JXErrorPane.showDialog(PrintingWidget.this.mappingComponent, ei);

                        if (pdfWait.isVisible()) {
                            pdfWait.dispose();
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
        dispose();
    } //GEN-LAST:event_cmdOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // new PrintingWidget(new javax.swing.JFrame(), true).setVisible(true);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInteractionModeAfterPrinting() {
        return interactionModeAfterPrinting;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  interactionModeAfterPrinting  DOCUMENT ME!
     */
    public void setInteractionModeAfterPrinting(final String interactionModeAfterPrinting) {
        this.interactionModeAfterPrinting = interactionModeAfterPrinting;
    }

    /**
     * set the progress bar to 100 percent and activates the ok button.
     */
    private void activateButton() {
        prbLoading.setIndeterminate(false);
        prbLoading.setValue(100);
        cmdOk.setEnabled(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  icon          DOCUMENT ME!
     * @param  tooltipImage  DOCUMENT ME!
     */
    private void addIconToProgressPane(final ImageIcon icon, final Image tooltipImage) {
        final JLabel label = new JLabel() {

                @Override
                public JToolTip createToolTip() {
                    if (tooltipImage != null) {
                        return new ImageToolTip(tooltipImage);
                    } else {
                        return super.createToolTip();
                    }
                }
            };
        synchronized (this) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final StyledDocument doc = (StyledDocument)txpLoadingStatus.getDocument();
                        final Style style = doc.addStyle("Icon", null); // NOI18N
                        label.setIcon(icon);
                        label.setText(" ");                             // NOI18N
                        // label.setVerticalAlignment(SwingConstants.TOP);
                        label.setAlignmentY(0.8f);
                        label.setToolTipText(
                            org.openide.util.NbBundle.getMessage(
                                PrintingWidget.class,
                                "PrintingWidget.addIconToProgressPane(ImageIcon,Image).label.setToolTipText")); // NOI18N
                        StyleConstants.setComponent(style, label);
                        try {
                            doc.insertString(doc.getLength(), "ico", style);                                    // NOI18N
                        } catch (BadLocationException ble) {
                            LOG.error("Error in addIconToProgressPane", ble);                                   // NOI18N
                        }
                    }
                });
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof HeadlessMapProvider.NotificationMessage) {
            final HeadlessMapProvider.NotificationMessage message = (HeadlessMapProvider.NotificationMessage)
                evt.getNewValue();
            addMessageToProgressPane(message.getMsg(), message.getLevel());

            if (message.getLevel().equals(UNLOCKED)) {
                activateButton();
            } else if (message.getLevel().equals(ERROR_REASON) && (evt.getOldValue() instanceof RetrievalEvent)) {
                final RetrievalEvent e = (RetrievalEvent)evt.getOldValue();
                if (e.getRetrievedObject() instanceof Image) {
                    final Image i = Static2DTools.removeUnusedBorder((Image)e.getRetrievedObject(), 5, 0.7);
                    addIconToProgressPane(errorImage, i);
                    addMessageToProgressPane(org.openide.util.NbBundle.getMessage(
                            PrintingWidget.class,
                            "PrintingWidget.retrievalComplete(RetrievalEvent).msg2",
                            new Object[] { e.getRetrievalService() }),
                        ERROR_REASON); // NOI18N
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  msg     DOCUMENT ME!
     * @param  reason  DOCUMENT ME!
     */
    private void addMessageToProgressPane(final String msg, final NotificationLevel reason) {
        synchronized (this) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            txpLoadingStatus.getStyledDocument()
                                    .insertString(
                                        txpLoadingStatus.getStyledDocument().getLength(),
                                        msg
                                        + "\n",
                                        styles.get(reason));       // NOI18N
                        } catch (BadLocationException ble) {
                            LOG.error("error during Insert", ble); // NOI18N
                        }
                    }
                });
        }
    }
}
