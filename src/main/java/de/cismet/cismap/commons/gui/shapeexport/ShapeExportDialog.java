/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 jweintraut
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * WFSExportDialog.java
 *
 * Created on 28.04.2011, 09:46:47
 */
package de.cismet.cismap.commons.gui.shapeexport;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.font.FontRenderContext;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class ShapeExportDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShapeExportDialog.class);

    //~ Instance fields --------------------------------------------------------

    private Collection<ExportWFS> wfsCollection;
    private Map<ExportWFS, JCheckBox> checkboxes;
    private Collection<ExportWFS> selectedWFSs = null;
    private boolean cancelled = true;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel lblDialogHint;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblStep1;
    private javax.swing.JLabel lblStep1Header;
    private javax.swing.JLabel lblSteps;
    private javax.swing.JPanel panDesc;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JPanel pnlExportParameters;
    private javax.swing.JPanel pnlFill;
    private javax.swing.JScrollPane scpExportParameters;
    private javax.swing.JSeparator sepControls;
    private javax.swing.JSeparator sepStep1Header;
    private javax.swing.JSeparator sepSteps;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeExportDialog object.
     *
     * @param  parent         DOCUMENT ME!
     * @param  wfsCollection  DOCUMENT ME!
     */
    public ShapeExportDialog(final JComponent parent, final Collection<ExportWFS> wfsCollection) {
        this(StaticSwingTools.getParentFrame(parent), true, wfsCollection);
    }

    /**
     * Creates new form WFSExportDialog.
     *
     * @param  parent         DOCUMENT ME!
     * @param  modal          DOCUMENT ME!
     * @param  wfsCollection  DOCUMENT ME!
     */
    public ShapeExportDialog(final java.awt.Frame parent,
            final boolean modal,
            final Collection<ExportWFS> wfsCollection) {
        super(parent, modal);
        this.wfsCollection = wfsCollection;
        checkboxes = new HashMap<ExportWFS, JCheckBox>(wfsCollection.size());

        initComponents();
        createCheckboxes();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void createCheckboxes() {
        // Variables 'font' and 'fontRenderContext' are only there to save some coding. Get rid of them if needed ;)
        Font font = null;
        FontRenderContext fontRenderContext = null;

        // Create the JCheckBox objects and determine max width for "special layout"
        int maxWidth = -1;
        for (final ExportWFS wfs : wfsCollection) {
            final JCheckBox newCheckBox = new JCheckBox(wfs.getTitle());
            checkboxes.put(wfs, newCheckBox);

            if (font == null) {
                font = newCheckBox.getFont();
            }
            if (fontRenderContext == null) {
                fontRenderContext = newCheckBox.getFontMetrics(font).getFontRenderContext();
            }

            final int width = (int)font.getStringBounds(newCheckBox.getText(),
                    fontRenderContext).getWidth();
            if (maxWidth < width) {
                maxWidth = width;
            }
        }

        // Calculate the gap between checkbox an text and add the checkboxes to the panel
        int verticalPosition = 3;
        final GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        layoutConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        layoutConstraints.gridx = 1;
        layoutConstraints.weightx = 1.0;

        for (final ExportWFS wfs : this.wfsCollection) {
            layoutConstraints.gridy = verticalPosition;
            verticalPosition++;

            final JCheckBox newCheckBox = checkboxes.get(wfs);
            newCheckBox.setIconTextGap((maxWidth
                            - (int)font.getStringBounds(newCheckBox.getText(),
                                fontRenderContext).getWidth()) + 10);
            pnlExportParameters.add(newCheckBox, layoutConstraints);
        }

        // Add a panel which fills the remaining space. Especially useful for users who like to resize dialogs.
        final JPanel pnlFill = new JPanel();
        pnlFill.setOpaque(false);
        layoutConstraints.fill = GridBagConstraints.BOTH;
        layoutConstraints.gridwidth = 2;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = verticalPosition;
        layoutConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        layoutConstraints.weighty = 1.0;
        pnlExportParameters.add(pnlFill, layoutConstraints);

        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlControls = new javax.swing.JPanel();
        sepControls = new javax.swing.JSeparator();
        pnlButtons = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        panDesc = new javax.swing.JPanel();
        lblSteps = new javax.swing.JLabel();
        sepSteps = new javax.swing.JSeparator();
        lblStep1 = new javax.swing.JLabel();
        lblIcon = new javax.swing.JLabel();
        pnlFill = new javax.swing.JPanel();
        scpExportParameters = new javax.swing.JScrollPane();
        pnlExportParameters = new javax.swing.JPanel();
        lblDialogHint = new javax.swing.JLabel();
        lblStep1Header = new javax.swing.JLabel();
        sepStep1Header = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ShapeExportDialog.class, "ShapeExportDialog.title")); // NOI18N
        setModal(true);

        pnlControls.setLayout(new java.awt.BorderLayout());
        pnlControls.add(sepControls, java.awt.BorderLayout.PAGE_START);

        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnCancel.setText(org.openide.util.NbBundle.getMessage(
                ShapeExportDialog.class,
                "ShapeExportDialog.btnCancel.text")); // NOI18N
        btnCancel.setPreferredSize(new java.awt.Dimension(100, 25));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnCancelActionPerformed(evt);
                }
            });
        pnlButtons.add(btnCancel);

        btnOK.setText(org.openide.util.NbBundle.getMessage(ShapeExportDialog.class, "ShapeExportDialog.btnOK.text")); // NOI18N
        btnOK.setPreferredSize(new java.awt.Dimension(100, 25));
        btnOK.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnOKActionPerformed(evt);
                }
            });
        pnlButtons.add(btnOK);

        pnlControls.add(pnlButtons, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlControls, java.awt.BorderLayout.PAGE_END);

        panDesc.setBackground(new java.awt.Color(216, 228, 248));
        panDesc.setMaximumSize(new java.awt.Dimension(200, 32767));
        panDesc.setMinimumSize(new java.awt.Dimension(200, 150));
        panDesc.setPreferredSize(new java.awt.Dimension(200, 150));
        panDesc.setLayout(new java.awt.GridBagLayout());

        lblSteps.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblSteps.setText(org.openide.util.NbBundle.getMessage(
                ShapeExportDialog.class,
                "ShapeExportDialog.lblSteps.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        panDesc.add(lblSteps, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 319;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panDesc.add(sepSteps, gridBagConstraints);

        lblStep1.setText(org.openide.util.NbBundle.getMessage(
                ShapeExportDialog.class,
                "ShapeExportDialog.lblStep1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        panDesc.add(lblStep1, gridBagConstraints);

        lblIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblIcon.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/shapeexport.png"))); // NOI18N
        lblIcon.setPreferredSize(new java.awt.Dimension(128, 128));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = -8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 7);
        panDesc.add(lblIcon, gridBagConstraints);

        pnlFill.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panDesc.add(pnlFill, gridBagConstraints);

        getContentPane().add(panDesc, java.awt.BorderLayout.LINE_START);

        scpExportParameters.setBorder(null);

        pnlExportParameters.setLayout(new java.awt.GridBagLayout());

        lblDialogHint.setText(org.openide.util.NbBundle.getMessage(
                ShapeExportDialog.class,
                "ShapeExportDialog.lblDialogHint.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlExportParameters.add(lblDialogHint, gridBagConstraints);

        lblStep1Header.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblStep1Header.setText(org.openide.util.NbBundle.getMessage(
                ShapeExportDialog.class,
                "ShapeExportDialog.lblStep1Header.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        pnlExportParameters.add(lblStep1Header, gridBagConstraints);

        sepStep1Header.setMaximumSize(new java.awt.Dimension(0, 0));
        sepStep1Header.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 0, 5);
        pnlExportParameters.add(sepStep1Header, gridBagConstraints);

        scpExportParameters.setViewportView(pnlExportParameters);

        getContentPane().add(scpExportParameters, java.awt.BorderLayout.CENTER);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnOKActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOKActionPerformed
        cancelled = false;

        selectedWFSs = new LinkedList<ExportWFS>();
        for (final Entry<ExportWFS, JCheckBox> entry : checkboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedWFSs.add(entry.getKey());
            }
        }

        setVisible(false);
    } //GEN-LAST:event_btnOKActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        cancelled = true;
        selectedWFSs = new LinkedList<ExportWFS>();
        setVisible(false);
    }                                                                             //GEN-LAST:event_btnCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<ExportWFS> getSelectedWFSs() {
        return selectedWFSs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static final void main(final String[] args) {
        final URL url;
        try {
            url = new URL("http://wfs.fis-wasser-mv.de/services");
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        final ExportWFS wfs1 = new ExportWFS(
                "Test-Thema 1",
                "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:app=\"http://www.deegree.org/app\" version=\"1.1.0\" service=\"WFS\" outputFormat=\"SHAPE\" maxFeatures=\"3000\">"
                        + "<wfs:Query typeName=\"app:route\" srsName=\"EPSG:35833\">"
                        + "<ogc:Filter>"
                        + "<ogc:BBOX>"
                        + "<ogc:PropertyName>app:the_geom</ogc:PropertyName>"
                        + "<cismet:BBOX/>"
                        + "</ogc:BBOX>"
                        + "</ogc:Filter>"
                        + "<wfs:PropertyName>app:id</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:gwk</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:the_geom</wfs:PropertyName>"
                        + "</wfs:Query>"
                        + "</wfs:GetFeature>",
                url);

        final ExportWFS wfs2 = new ExportWFS(
                "Dann das Test-Thema 2",
                "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:app=\"http://www.deegree.org/app\" version='1.1.0' service=\"WFS\" outputFormat=\"SHAPE\" maxFeatures=\"3000\">"
                        + "<wfs:Query typeName=\"app:route\" srsName=\"EPSG:35833\">"
                        + "<ogc:Filter>"
                        + "<ogc:BBOX>"
                        + "<ogc:PropertyName>app:the_geom</ogc:PropertyName>"
                        + "<cismet:BBOX/>"
                        + "</ogc:BBOX>"
                        + "</ogc:Filter>"
                        + "<wfs:PropertyName>app:id</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:gwk</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:the_geom</wfs:PropertyName>"
                        + "</wfs:Query>"
                        + "</wfs:GetFeature>",
                url);

        final ExportWFS wfs3 = new ExportWFS(
                "Und dann noch das Test-Thema 3",
                "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:app=\"http://www.deegree.org/app\" version='1.1.0' service=\"WFS\" outputFormat=\"SHAPE\" maxFeatures=\"3000\">"
                        + "<wfs:Query typeName=\"app:ogc.oeg\" srsName=\"EPSG:35833\">"
                        + "<ogc:Filter>"
                        + "<ogc:BBOX>"
                        + "<ogc:PropertyName>app:the_geom</ogc:PropertyName>"
                        + "<cismet:BBOX/>"
                        + "</ogc:BBOX>"
                        + "</ogc:Filter>"
                        + "<wfs:PropertyName>app:gid</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:area</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:perimeter</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:ezg3_</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:ezg3_id</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:poly_</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:subclass</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:subclass_</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:rings_ok</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:rings_nok</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:schluessel</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:zehn</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:typ</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:typ1</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:typ2</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:teilgebnr</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:pegelnr</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:pegelname</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:pkz</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:seenr</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:seename</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:flussgeb</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:von</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:modi_geo</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:modi_gwk</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:modi_gebbz</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:key_pl</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:key_pl_ten</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:teilgeb</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:wrkarea</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:area_km2</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:mst</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:a_sum</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:id_ezg</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:nsaldo_amt</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:min_nsl_lw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:max_nsl_lw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:nsaldo_lw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:area_lw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:kg_lw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:gwn_0</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:rd_p1</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:gwn_p1</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:no3_sw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:no3_gw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:no3_kg_gw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:no3_kg_ow</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:et0</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:no3_gw1</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:no3_gw2</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:kf_hoch</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:mv</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:proz_gw</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:nsald_amt2</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:nsald_lw2</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:gwk_fluss</wfs:PropertyName>"
                        + "<wfs:PropertyName>app:the_geom</wfs:PropertyName>"
                        + "</wfs:Query>"
                        + "</wfs:GetFeature>",
                url);

        final Collection<ExportWFS> wfsList = new LinkedList<ExportWFS>();

        wfsList.add(wfs1);
        wfsList.add(wfs2);
        wfsList.add(wfs3);

        final ShapeExportDialog dialog = new ShapeExportDialog(null, wfsList);

        dialog.setVisible(true);

        if (dialog.isCancelled()) {
            return;
        }

        final Collection<ExportWFS> selectedWFS = dialog.getSelectedWFSs();
        LOG.fatal(selectedWFS);
        System.out.println(selectedWFS);

        for (final ExportWFS wfs : selectedWFS) {
            wfs.setQuery(wfs.getQuery().replace(
                    "<cismet:BBOX/>",
                    "<gml:Box><gml:coord><gml:X>3.3260837108302265E7</gml:X><gml:Y>5939174.86179747</gml:Y></gml:coord><gml:coord><gml:X>3.3306013669564433E7</gml:X><gml:Y>5954878.55311782</gml:Y></gml:coord></gml:Box>"));
        }

        final JFrame frame = new JFrame("Download-Manager");
        final DownloadManagerPanel pnlDownload = new DownloadManagerPanel();
        frame.setLayout(new BorderLayout());
        frame.add(pnlDownload, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pnlDownload.add(selectedWFS);
        frame.validate();
        frame.pack();
        frame.setVisible(true);
    }
}
