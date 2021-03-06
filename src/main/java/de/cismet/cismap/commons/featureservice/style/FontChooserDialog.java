/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FontChooserDialog.java
 *
 * Created on 5. M\u00E4rz 2008, 09:17
 */
package de.cismet.cismap.commons.featureservice.style;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

import de.cismet.tools.CismetThreadPool;

/**
 * DOCUMENT ME!
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class FontChooserDialog extends JDialog {

    //~ Static fields/initializers ---------------------------------------------

    /** Display text. */
    private static final String SAMPLE_TEXT = org.openide.util.NbBundle.getMessage(
            FontChooserDialog.class,
            "FontChooserDialog.SAMPLE_TEXT"); // NOI18N
    /** Selected index of the default fontsize. */
    private static final int DEFAULT_SIZE = 4;

    //~ Instance fields --------------------------------------------------------

    /** The font the user has chosen. */
    private Font resultFont;
    /** The resulting font name. */
    private String resultName;
    /** The resulting font size. */
    private int resultSize;
    /** The resulting boldness. */
    private boolean isBold;
    /** The resulting italicness. */
    private boolean isItalic;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkBold;
    private javax.swing.JCheckBox chkItalic;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JList lstFontName;
    private javax.swing.JList lstFontSize;
    private javax.swing.JPanel panDialogButtons;
    private javax.swing.JPanel panFontAttrib;
    private javax.swing.JPanel panPreview;
    private javax.swing.JPanel panPreviewLabel;
    private javax.swing.JScrollPane scrFontName;
    private javax.swing.JScrollPane scrFontSize;
    private javax.swing.JScrollPane scrPreview;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FontChooserDialog.
     *
     * @param  parent  DOCUMENT ME!
     */
    public FontChooserDialog(final JFrame parent) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
    }

    /**
     * Creates new form FontChooserDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  title   DOCUMENT ME!
     */
    public FontChooserDialog(final JDialog parent, final String title) {
        super(parent, title, true);
        initComponents();
        setLocationRelativeTo(parent);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Called from the action handlers to get the font info, build a font, and set it.
     */
    protected void previewFont() {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    resultName = (String)lstFontName.getSelectedValue();
                    final String resultSizeName = (String)lstFontSize.getSelectedValue();
                    final int size = Integer.parseInt(resultSizeName);
                    isItalic = chkItalic.isSelected();
                    isBold = chkBold.isSelected();
                    int attrs = Font.PLAIN;
                    if (isBold) {
                        attrs = Font.BOLD;
                    }
                    if (isItalic) {
                        attrs |= Font.ITALIC;
                    }
                    resultFont = new Font(resultName, attrs, size);
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                lblPreview.setFont(resultFont);
                            }
                        });
                }
            };
        CismetThreadPool.execute(new Thread(r, "FontChooserDialog previewFont()"));
    }

    /**
     * Retrieve the selected font name.
     *
     * @return  DOCUMENT ME!
     */
    public String getSelectedName() {
        return resultName;
    }

    /**
     * Retrieve the selected size.
     *
     * @return  DOCUMENT ME!
     */
    public int getSelectedSize() {
        return resultSize;
    }

    /**
     * Returns the new Font.
     *
     * @return  DOCUMENT ME!
     */
    public Font getReturnStatus() {
        return resultFont;
    }

    /**
     * Selects the given font if available, the given size and style.
     *
     * @param  font    DOCUMENT ME!
     * @param  size    DOCUMENT ME!
     * @param  bold    DOCUMENT ME!
     * @param  italic  DOCUMENT ME!
     */
    public void setSelectedFont(final Font font, final int size, final boolean bold, final boolean italic) {
        lstFontName.setSelectedValue(font.getName(), true);
        lstFontSize.setSelectedValue(new Integer(size).toString(), true);
        chkBold.setSelected(bold);
        chkItalic.setSelected(italic);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panFontAttrib = new javax.swing.JPanel();
        scrFontName = new javax.swing.JScrollPane();
        lstFontName = new JList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        scrFontSize = new javax.swing.JScrollPane();
        lstFontSize = new javax.swing.JList();
        chkBold = new javax.swing.JCheckBox();
        chkItalic = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        panPreview = new javax.swing.JPanel();
        scrPreview = new javax.swing.JScrollPane();
        panPreviewLabel = new javax.swing.JPanel();
        lblPreview = new JLabel(SAMPLE_TEXT, JLabel.CENTER) {

                @Override
                protected void paintComponent(final Graphics g) {
                    final Graphics2D g2d = (Graphics2D)g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    super.paintComponent(g2d);
                }
            };
        ;
        panDialogButtons = new javax.swing.JPanel();
        cmdOK = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosing(final java.awt.event.WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        panFontAttrib.setLayout(new java.awt.GridBagLayout());

        scrFontName.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrFontName.setMinimumSize(new java.awt.Dimension(200, 130));
        scrFontName.setPreferredSize(new java.awt.Dimension(200, 130));

        lstFontName.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstFontName.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                @Override
                public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                    lstFontNameValueChanged(evt);
                }
            });
        scrFontName.setViewportView(lstFontName);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        panFontAttrib.add(scrFontName, gridBagConstraints);

        scrFontSize.setMinimumSize(new java.awt.Dimension(50, 130));
        scrFontSize.setPreferredSize(new java.awt.Dimension(50, 130));

        lstFontSize.setModel(new javax.swing.AbstractListModel() {

                String[] strings = {
                        "8",
                        "9",
                        "10",
                        "11",
                        "12",
                        "14",
                        "16",
                        "18",
                        "20",
                        "24",
                        "30",
                        "36",
                        "48",
                        "60",
                        "72"
                    };

                @Override
                public int getSize() {
                    return strings.length;
                }
                @Override
                public Object getElementAt(final int i) {
                    return strings[i];
                }
            });
        lstFontSize.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstFontSize.setSelectedIndex(DEFAULT_SIZE);
        lstFontSize.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                @Override
                public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                    lstFontSizeValueChanged(evt);
                }
            });
        scrFontSize.setViewportView(lstFontSize);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 10);
        panFontAttrib.add(scrFontSize, gridBagConstraints);

        chkBold.setText(org.openide.util.NbBundle.getMessage(
                FontChooserDialog.class,
                "FontChooserDialog.chkBold.text")); // NOI18N
        chkBold.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkBoldItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        panFontAttrib.add(chkBold, gridBagConstraints);

        chkItalic.setText(org.openide.util.NbBundle.getMessage(
                FontChooserDialog.class,
                "FontChooserDialog.chkItalic.text")); // NOI18N
        chkItalic.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkItalicItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFontAttrib.add(chkItalic, gridBagConstraints);

        jLabel1.setLabelFor(lstFontName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                FontChooserDialog.class,
                "FontChooserDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        panFontAttrib.add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(lstFontSize);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(
                FontChooserDialog.class,
                "FontChooserDialog.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        panFontAttrib.add(jLabel2, gridBagConstraints);

        getContentPane().add(panFontAttrib, java.awt.BorderLayout.NORTH);

        panPreview.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 5, 5));
        panPreview.setMinimumSize(new java.awt.Dimension(400, 100));
        panPreview.setPreferredSize(new java.awt.Dimension(400, 100));
        panPreview.setLayout(new java.awt.BorderLayout());

        scrPreview.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrPreview.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        panPreviewLabel.setBackground(new java.awt.Color(255, 255, 255));
        panPreviewLabel.setLayout(new java.awt.GridBagLayout());
        panPreviewLabel.add(lblPreview, new java.awt.GridBagConstraints());

        scrPreview.setViewportView(panPreviewLabel);

        panPreview.add(scrPreview, java.awt.BorderLayout.CENTER);

        getContentPane().add(panPreview, java.awt.BorderLayout.CENTER);

        panDialogButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panDialogButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

        cmdOK.setText(org.openide.util.NbBundle.getMessage(FontChooserDialog.class, "FontChooserDialog.cmdOK.text")); // NOI18N
        cmdOK.setMaximumSize(new java.awt.Dimension(90, 23));
        cmdOK.setMinimumSize(new java.awt.Dimension(90, 23));
        cmdOK.setPreferredSize(new java.awt.Dimension(90, 23));
        cmdOK.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOKActionPerformed(evt);
                }
            });
        panDialogButtons.add(cmdOK);

        cmdCancel.setText(org.openide.util.NbBundle.getMessage(
                FontChooserDialog.class,
                "FontChooserDialog.cmdCancel.text")); // NOI18N
        cmdCancel.setMaximumSize(new java.awt.Dimension(90, 23));
        cmdCancel.setMinimumSize(new java.awt.Dimension(90, 23));
        cmdCancel.setPreferredSize(new java.awt.Dimension(90, 23));
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCancelActionPerformed(evt);
                }
            });
        panDialogButtons.add(cmdCancel);

        getContentPane().add(panDialogButtons, java.awt.BorderLayout.SOUTH);

        pack();
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void closeDialog(final java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
        doClose();
    }                                                                //GEN-LAST:event_closeDialog

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOKActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOKActionPerformed
        doClose();
    }                                                                         //GEN-LAST:event_cmdOKActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdCancelActionPerformed
        resultFont = null;
        doClose();
    }                                                                             //GEN-LAST:event_cmdCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lstFontSizeValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstFontSizeValueChanged
        previewFont();
    }                                                                                      //GEN-LAST:event_lstFontSizeValueChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lstFontNameValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstFontNameValueChanged
        previewFont();
    }                                                                                      //GEN-LAST:event_lstFontNameValueChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkBoldItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkBoldItemStateChanged
        previewFont();
    }                                                                          //GEN-LAST:event_chkBoldItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkItalicItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkItalicItemStateChanged
        previewFont();
    }                                                                            //GEN-LAST:event_chkItalicItemStateChanged

    /**
     * DOCUMENT ME!
     */
    private void doClose() {
        setVisible(false);
        dispose();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final FontChooserDialog dialog = new FontChooserDialog(new javax.swing.JFrame());
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    dialog.setVisible(true);
                }
            });
    }
}
