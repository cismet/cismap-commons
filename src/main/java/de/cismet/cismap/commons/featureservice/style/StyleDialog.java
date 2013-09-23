/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StyleDialog.java
 *
 * Created on 25. Februar 2008, 10:38
 */
package de.cismet.cismap.commons.featureservice.style;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;

import org.apache.commons.io.IOUtils;

import org.bounce.text.LineNumberMargin;
import org.bounce.text.ScrollableEditorPanel;
import org.bounce.text.xml.XMLDocument;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;

import org.jdom.Document;
import org.jdom.output.Format;

// import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.text.ParseException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.PlainDocument;

import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.lookupoptions.gui.OptionsDialog;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;

/**
 * A dialog that lets you alter the FeatureLayers appearance.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class StyleDialog extends JDialog implements ListSelectionListener {

    //~ Static fields/initializers ---------------------------------------------

    // constants: filesystem
    private static final String CISMAP_FOLDER = ".cismap";                                                         // NOI18N
    private static final String DEFAULT_HISTORY_NAME = "defaultStyleHistory.xml";                                  // NOI18N
    private static final String COLORCHOOSER_TITLE = org.openide.util.NbBundle.getMessage(
            StyleDialog.class,
            "StyleDialog.COLORCHOOSER_TITLE");                                                                     // NOI18N
    private static final String FONTCHOOSER_TITLE = org.openide.util.NbBundle.getMessage(
            StyleDialog.class,
            "StyleDialog.FONTCHOOSER_TITLE");                                                                      // NOI18N
    private static final String POINTSYMBOL_FOLDER = "/de/cismet/cismap/commons/featureservice/res/pointsymbols/"; // NOI18N
    // constants: popup
    // FIXME: I18N
    private static final String POPUP_SAVE = org.openide.util.NbBundle.getMessage(
            StyleDialog.class,
            "StyleDialog.POPUP_SAVE");  // NOI18N
    private static final String POPUP_LOAD = org.openide.util.NbBundle.getMessage(
            StyleDialog.class,
            "StyleDialog.POPUP_LOAD");  // NOI18N
    private static final String POPUP_CLEAR = org.openide.util.NbBundle.getMessage(
            StyleDialog.class,
            "StyleDialog.POPUP_CLEAR"); // NOI18N

    /**
     * <editor-fold defaultstate="collapsed" desc="Eventhandling">.
     *
     * @param  evt  DOCUMENT ME!
     */
    private void closeDialog(final java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
        doClose(false);
    }                                                                //GEN-LAST:event_closeDialog

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOKActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOKActionPerformed

        // setLabelAttribute((cbbAttribute.getSelectedItem() == null) ? null :
        // cbbAttribute.getSelectedItem().toString());

        // read content of textfields
        setMinScale(Integer.parseInt(txtMin.getText()));
        setMaxScale(Integer.parseInt(txtMax.getText()));

        try {
            txtMultiplier.commitEdit();
        } catch (ParseException ex) {
            logger.warn("Could not perform a commitEdit()", ex); // NOI18N
        }

        setMultiplier(txtMultiplier.getValue());

        // write new history
        if (defaultHistory != null) {
            writeHistory(defaultHistory, true);
        }

        if (isQueryStringChanged()) {
            if (logger.isDebugEnabled()) {
                logger.debug("setting new Query Template"); // NOI18N
            }

            // this.layerProperties.setQueryTemplate(this.queryEditor.getText(), this.layerProperties.QUERYTYPE_XML);
        }

// // manipulate the returnfeature if (isStyleFeature) { ((StyledFeature)
// feature).setFillingPaint(getStyle().isDrawFill() ? getStyle().getFillColor() : null); ((StyledFeature)
// feature).setLinePaint(getStyle().isDrawLine() ? getStyle().getLineColor() : null); ((StyledFeature)
// feature).setLineWidth(getStyle().getLineWidth()); ((StyledFeature) feature).setTransparency(getStyle().getAlpha());
// ((StyledFeature) feature).setPointAnnotationSymbol(getPointSymbol() == null ? pointSymbol : getPointSymbol());
// ((StyledFeature) feature).setHighlightingEnabled(getStyle().isHighlightFeature()); }
//
// if (isAnnotatedFeature) { ((AnnotatedFeature) feature).setPrimaryAnnotationVisible(getStyle().isDrawLabel());
// ((AnnotatedFeature) feature).setAutoScale(getStyle().isAutoscale()); //((AnnotatedFeature)
// feature).setMaxScaleDenominator(getStyle().getMaxScale()); ((AnnotatedFeature)
// feature).setMinScaleDenominator(getStyle().getMinScale()); ((AnnotatedFeature)
// feature).setPrimaryAnnotation(getStyle().getAnnotationAttribute()); ((AnnotatedFeature)
// feature).setPrimaryAnnotationJustification(getStyle().getAlignment()); ((AnnotatedFeature)
// feature).setPrimaryAnnotationFont(getStyle().getFont()); ((AnnotatedFeature)
// feature).setPrimaryAnnotationPaint(getStyle().getFontColor()); ((AnnotatedFeature)
// feature).setPrimaryAnnotationScaling(getMultiplier()); }
//
// if (isIdFeature) { ((FeatureWithId) feature).setIdExpression(getIdExpression()); }
        doClose(true);
    } //GEN-LAST:event_cmdOKActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdCancelActionPerformed
        doClose(false);
    }                                                                             //GEN-LAST:event_cmdCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkFillPatternItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkFillPatternItemStateChanged
        // not supported or shown
// cbbFillPattern.setEnabled((evt.getStateChange() == ItemEvent.SELECTED));
// updatePreview();
    } //GEN-LAST:event_chkFillPatternItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkFillItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkFillItemStateChanged

        cmdFill.setEnabled(chkFill.isSelected());
        getStyle().setDrawFill(chkFill.isSelected());
        updatePreview();
    } //GEN-LAST:event_chkFillItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkLineItemStateChanged(final java.awt.event.ItemEvent evt) {
        switchLineActive(chkLine.isSelected());
        getStyle().setDrawLine(chkLine.isSelected());
        updatePreview();
    }

    /**
     * Enables/disables the components to change the lineproperties.
     *
     * @param  flag  true to enable, false to disable
     */
    private void switchLineActive(final boolean flag) {
        cmdLine.setEnabled(flag);
        sldLineWidth.setEnabled(flag);
        txtLineWidth.setEnabled(flag);
        lblLineWidth.setEnabled(flag);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkLinePatternItemStateChanged(final java.awt.event.ItemEvent evt) {
        // not supported or shown cbbLinePattern.setEnabled((evt.getStateChange() == ItemEvent.SELECTED));
        // updatePreview();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void sldLineWidthStateChanged(final javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_sldLineWidthStateChanged

        // only permit linewidth > 0
        if (sldLineWidth.getValue() == 0) {
            sldLineWidth.setValue(1);
        }

        setLineWidth(sldLineWidth.getValue());
        updatePreview();
    } //GEN-LAST:event_sldLineWidthStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void sldAlphaStateChanged(final javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_sldAlphaStateChanged
        setAlpha(sldAlpha.getValue() / 100.0f);
        updatePreview();
    }                                                                            //GEN-LAST:event_sldAlphaStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdFillActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdFillActionPerformed

        // set current color in the ColorChooser
        colorChooser.setColor(getStyle().getFillColor());

        // show and evaluate ColorChooser (inside Actionlistener)
        final JDialog colorChooserDialog = JColorChooser.createDialog(
                this,
                COLORCHOOSER_TITLE,
                true,
                colorChooser,
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("new filling = " + colorChooser.getColor()); // NOI18N
                        }
                        setFillColor(true, colorChooser.getColor());

                        if (chkSync.isSelected()) {
                            setLineColor(true, BasicStyle.darken(colorChooser.getColor()));
                        }

                        updatePreview();
                    }
                },
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ColorChooser cancelled"); // NOI18N
                        }
                    }
                });

        StaticSwingTools.showDialog(colorChooserDialog);
    } //GEN-LAST:event_cmdFillActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdLineActionPerformed(final java.awt.event.ActionEvent evt) {
        // set current color in the colorchooser
        colorChooser.setColor(getStyle().getLineColor());

        // show and evaluate ColorChooser (inside Actionlistener)
        StaticSwingTools.showDialog(JColorChooser.createDialog(
                this,
                COLORCHOOSER_TITLE,
                true,
                colorChooser,
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("new line color = " + colorChooser.getColor()); // NOI18N
                        }
                        StyleDialog.this.setLineColor(true, colorChooser.getColor());
                        updatePreview();
                    }
                },
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ColorChooser cancelled"); // NOI18N
                        }
                    }
                }));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkActivateLabelsItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkActivateLabelsItemStateChanged

        // enable/disable every labelling-oriented component
        final boolean flag = chkActivateLabels.isSelected();
        setLabelingEnabled(flag);
        lblAnnotationExpression.setEnabled(flag);
        cbbAnnotationExpression.setEnabled(flag);
        lblAlignment.setEnabled(flag);
        radLeft.setEnabled(flag);
        radCenter.setEnabled(flag);
        radRight.setEnabled(flag);
        lblMultiplier.setEnabled(flag);
        txtMultiplier.setEnabled(flag);
        chkAutoscale.setEnabled(flag);
        lblMin.setEnabled(flag);
        txtMin.setEnabled(flag);
        lblMax.setEnabled(flag);
        txtMax.setEnabled(flag);
        lblFontname.setEnabled(flag);
        panLabelButtons.setEnabled(flag);
        cmdChangeTextColor.setEnabled(flag);
        cmdChangeFont.setEnabled(flag);
        updatePreview();
    } //GEN-LAST:event_chkActivateLabelsItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void sldPointSymbolSizeStateChanged(final javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_sldPointSymbolSizeStateChanged
        setPointSymbolSize(sldPointSymbolSize.getValue());
        updatePreview();
    }                                                                                      //GEN-LAST:event_sldPointSymbolSizeStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void sldPointSymbolSizeMouseWheelMoved(final java.awt.event.MouseWheelEvent evt) { //GEN-FIRST:event_sldPointSymbolSizeMouseWheelMoved

        if (sldPointSymbolSize.isEnabled() && sldPointSymbolSize.isFocusOwner()) {
            sldPointSymbolSize.setValue(sldPointSymbolSize.getValue() - evt.getWheelRotation());
        }
    } //GEN-LAST:event_sldPointSymbolSizeMouseWheelMoved

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void sldAlphaMouseWheelMoved(final java.awt.event.MouseWheelEvent evt) { //GEN-FIRST:event_sldAlphaMouseWheelMoved
        sldAlpha.setValue(sldAlpha.getValue() - (evt.getWheelRotation() * 5));
    }                                                                                //GEN-LAST:event_sldAlphaMouseWheelMoved

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdChangeTextColorActionPerformed(final java.awt.event.ActionEvent evt) {
        // set current color in the colorchooser
        colorChooser.setColor(getStyle().getFontColor());

        // show and evaluate ColorChooser (inside Actionlistener)
        StaticSwingTools.showDialog(JColorChooser.createDialog(
                this,
                COLORCHOOSER_TITLE,
                true,
                colorChooser,
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("new font color = " + colorChooser.getColor()); // NOI18N
                        }
                        setFontColor(colorChooser.getColor());

                        updatePreview();
                    }
                },
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ColorChooser cancelled"); // NOI18N
                        }
                    }
                }));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdChangeFontActionPerformed(final java.awt.event.ActionEvent evt) {
        // show and evaluate FontChooser
        final Font temp = getStyle().getFont();
        fontChooser.setSelectedFont(temp, temp.getSize(), temp.isBold(), temp.isItalic());
        StaticSwingTools.showDialog(fontChooser, false);

        if (fontChooser.getReturnStatus() != null) {
            setFontType(fontChooser.getReturnStatus());
            updatePreview();
        }

        setLineWidth(sldLineWidth.getValue());
        updatePreview();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkAutoscaleItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkAutoscaleItemStateChanged
        setAutoscale(chkAutoscale.isSelected());
    }                                                                               //GEN-LAST:event_chkAutoscaleItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbbPointSymbolItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbbPointSymbolItemStateChanged

        // evaluate the selection of the pointsymbol-ComboBox
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            final String selectedPointSymbol = evt.getItem().toString();
            if (logger.isDebugEnabled()) {
                logger.debug("select Point Symbol '" + selectedPointSymbol + "'"); // NOI18N
            }

            if (pointSymbolHM.containsKey(selectedPointSymbol)) {
                this.setPointSymbol(selectedPointSymbol);
            } else {
                logger.warn("unsupported point symbol '" + selectedPointSymbol + "'"); // NOI18N
                setPointSymbol(Style.NO_POINTSYMBOL);
            }
        }

        updatePreview();
    } //GEN-LAST:event_cbbPointSymbolItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkHighlightableItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkHighlightableItemStateChanged
        setHighlighting(chkHighlightable.isSelected());
    }                                                                                   //GEN-LAST:event_chkHighlightableItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkSyncItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkSyncItemStateChanged

        if (evt.getStateChange() == ItemEvent.SELECTED) {
            setLineColor(true, BasicStyle.darken(getStyle().getFillColor()));
            updatePreview();
        }
    } //GEN-LAST:event_chkSyncItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkLinewrapActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkLinewrapActionPerformed

        final XMLEditorKit kit = (XMLEditorKit)queryEditor.getEditorKit();
        kit.setLineWrappingEnabled(chkLinewrap.isSelected());
        queryEditor.updateUI();
    } //GEN-LAST:event_chkLinewrapActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkFillActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_chkFillActionPerformed
    {                                                                         //GEN-HEADEREND:event_chkFillActionPerformed
                                                                              // TODO add your handling code here:
    }                                                                         //GEN-LAST:event_chkFillActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkHighlightableActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_chkHighlightableActionPerformed
    {                                                                                  //GEN-HEADEREND:event_chkHighlightableActionPerformed
                                                                                       // TODO add your handling code
                                                                                       // here:
    }                                                                                  //GEN-LAST:event_chkHighlightableActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbbPointSymbolActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_cbbPointSymbolActionPerformed
    {                                                                                //GEN-HEADEREND:event_cbbPointSymbolActionPerformed
                                                                                     // TODO add your handling code
                                                                                     // here:
    }                                                                                //GEN-LAST:event_cbbPointSymbolActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbbAnnotationExpressionItemStateChanged(final java.awt.event.ItemEvent evt) //GEN-FIRST:event_cbbAnnotationExpressionItemStateChanged
    {                                                                                        //GEN-HEADEREND:event_cbbAnnotationExpressionItemStateChanged

        if (!this.ignoreSelectionEvent && (evt.getStateChange() == ItemEvent.SELECTED)) {
            final String annotationExpression = cbbAnnotationExpression.getSelectedItem().toString();

            if (this.featureServiceAttributes.containsKey(annotationExpression)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("setting annotation expression to '" + annotationExpression
                                + "' (EXPRESSIONTYPE_PROPERTYNAME)"); // NOI18N
                }
                this.layerProperties.setPrimaryAnnotationExpression(
                    annotationExpression,
                    LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("setting annotation expression to '" + annotationExpression
                                + "' (EXPRESSIONTYPE_GROOVY)");       // NOI18N
                }
                this.layerProperties.setPrimaryAnnotationExpression(
                    annotationExpression,
                    LayerProperties.EXPRESSIONTYPE_GROOVY);
            }
        }
    }                                                                 //GEN-LAST:event_cbbAnnotationExpressionItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbbIdExpressionItemStateChanged(final java.awt.event.ItemEvent evt) //GEN-FIRST:event_cbbIdExpressionItemStateChanged
    {                                                                                //GEN-HEADEREND:event_cbbIdExpressionItemStateChanged

        if (!this.ignoreSelectionEvent && (evt.getStateChange() == ItemEvent.SELECTED)) {
            final String idExpression = cbbIdExpression.getSelectedItem().toString();

            if (this.featureServiceAttributes.containsKey(idExpression)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("setting primary key to '" + idExpression + "' (EXPRESSIONTYPE_PROPERTYNAME)"); // NOI18N
                }
                this.layerProperties.setIdExpression(idExpression, LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("setting primary key to '" + idExpression + "' (EXPRESSIONTYPE_GROOVY)");       // NOI18N
                }
                this.layerProperties.setIdExpression(idExpression, LayerProperties.EXPRESSIONTYPE_GROOVY);
            }
        }
    }                                                                                                            //GEN-LAST:event_cbbIdExpressionItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdAddActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdAddActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cmdAddActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkUseQueryStringActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkUseQueryStringActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_chkUseQueryStringActionPerformed

    /**
     * Changes the style of the StyleDialog if the selection of the historylist has changed.
     *
     * @param  evt  ListSelectionEvent
     */
    @Override
    public void valueChanged(final ListSelectionEvent evt) {
        try {
            final Style restoredStyle = (Style)lstHistory.getSelectedValue();
            this.layerProperties.setStyle((Style)restoredStyle.clone());

            this.updateDialog();
            this.updatePreview();
        } catch (Exception ex) {
            logger.error("Fehler beim Auslesen des Styles", ex); // NOI18N
        }
    }

    /**
     * Get the value of featureServiceAttributes.
     *
     * @return  the value of featureServiceAttributes
     */
    public Map<String, FeatureServiceAttribute> getFeatureServiceAttributes() {
        if (!this.isAccepted()) {
            logger.warn("supicious call to 'getFeatureServiceAttributes()', changes not accepted"); // NOI18N
        }

        return featureServiceAttributes;
    }

    /**
     * Set the value of featureServiceAttributes.
     *
     * @param  featureServiceAttributes  new value of featureServiceAttributes
     */
    public void setFeatureServiceAttributes(final Map<String, FeatureServiceAttribute> featureServiceAttributes) {
        this.oldFeatureServiceAttributes = featureServiceAttributes;
        this.featureServiceAttributes = new TreeMap();

        for (final FeatureServiceAttribute fsa : featureServiceAttributes.values()) {
            this.featureServiceAttributes.put(fsa.getName(), fsa.clone());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isGeoAttributeChanged() {
        for (final FeatureServiceAttribute oldAttribute : this.oldFeatureServiceAttributes.values()) {
            if (oldAttribute.isGeometry()) {
                final FeatureServiceAttribute newAttribute = this.featureServiceAttributes.get(oldAttribute.getName());

                if (newAttribute.isSelected() != oldAttribute.isSelected()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAttributeSelectionChanged() {
        for (final FeatureServiceAttribute oldAttribute : this.oldFeatureServiceAttributes.values()) {
            if (!oldAttribute.isGeometry()) {
                final FeatureServiceAttribute newAttribute = this.featureServiceAttributes.get(oldAttribute.getName());

                if (newAttribute.isSelected() != oldAttribute.isSelected()) {
                    return true;
                }
            }
        }

        return false;
    }

    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    private final String home = System.getProperty("user.home");           // NOI18N
    private final String seperator = System.getProperty("file.separator"); // NOI18N
    private final File fileToCismapFolder = new File(home + seperator + CISMAP_FOLDER);
    private TreeMap<String, FeatureAnnotationSymbol> pointSymbolHM = new TreeMap();
    private TreeMap<String, FeatureServiceAttribute> featureServiceAttributes;
    private Map<String, FeatureServiceAttribute> oldFeatureServiceAttributes;
    private FeatureAnnotationSymbol pointSymbol = null;
    private File defaultHistory;
    private JColorChooser colorChooser;
    private FontChooserDialog fontChooser;
    private JPopupMenu popupMenu;
    private JEditorPane queryEditor = new JEditorPane();
    private LayerProperties layerProperties;
    private boolean accepted = false;
    private boolean ignoreSelectionEvent = true;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btgAlignment;
    private javax.swing.ButtonGroup btgGeom;
    private javax.swing.JComboBox cbbAnnotationExpression;
    private javax.swing.JComboBox cbbFillPattern;
    private javax.swing.JComboBox cbbIdExpression;
    private javax.swing.JComboBox cbbLinePattern;
    private javax.swing.JComboBox cbbPointSymbol;
    private javax.swing.JCheckBox chkActivateLabels;
    private javax.swing.JCheckBox chkAutoscale;
    private javax.swing.JCheckBox chkCustomSLD;
    private javax.swing.JCheckBox chkFill;
    private javax.swing.JCheckBox chkFillPattern;
    private javax.swing.JCheckBox chkHighlightable;
    private javax.swing.JCheckBox chkLine;
    private javax.swing.JCheckBox chkLinePattern;
    private javax.swing.JCheckBox chkLinewrap;
    private javax.swing.JCheckBox chkSync;
    private javax.swing.JCheckBox chkUseQueryString;
    private javax.swing.JButton cmdAdd;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdChangeFont;
    private javax.swing.JButton cmdChangeTextColor;
    private javax.swing.JButton cmdFill;
    private javax.swing.JButton cmdLine;
    private javax.swing.JButton cmdOK;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblAlignment;
    private javax.swing.JLabel lblAlpha;
    private javax.swing.JLabel lblAnnotationExpression;
    private javax.swing.JLabel lblFontname;
    private javax.swing.JLabel lblHistory;
    private javax.swing.JLabel lblIdExpression;
    private javax.swing.JLabel lblLineWidth;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblMultiplier;
    private javax.swing.JLabel lblPointSymbol;
    private javax.swing.JLabel lblPointSymbolSize;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JList lstHistory;
    private javax.swing.JPanel panAlignment;
    private javax.swing.JPanel panAttribGeo;
    private javax.swing.JPanel panAttribNorm;
    private javax.swing.JPanel panAttribSeparator;
    private javax.swing.JPanel panDialogButtons;
    private javax.swing.JPanel panFill;
    private javax.swing.JPanel panFillColor;
    private javax.swing.JPanel panFontColor;
    private javax.swing.JPanel panInfo;
    private javax.swing.JPanel panInfoComp;
    private javax.swing.JPanel panLabelButtons;
    private javax.swing.JPanel panLabeling;
    private javax.swing.JPanel panLineColor;
    private javax.swing.JPanel panMain;
    private javax.swing.JPanel panPreview;
    private javax.swing.JPanel panQueryCheckbox;
    private javax.swing.JPanel panRules;
    private javax.swing.JPanel panRulesButtons;
    private javax.swing.JPanel panRulesScroll;
    private javax.swing.JPanel panSLDDefinition;
    private javax.swing.JPanel panScale;
    private javax.swing.JPanel panScrollpane;
    private javax.swing.JPanel panTabAttrib;
    private javax.swing.JPanel panTabFill;
    private javax.swing.JPanel panTabLabeling;
    private javax.swing.JPanel panTabQuery;
    private javax.swing.JPanel panTabRules;
    private javax.swing.JPanel panTabs;
    private javax.swing.JPanel panTransColor;
    private javax.swing.JPanel panTransWhite;
    private javax.swing.JRadioButton radCenter;
    private javax.swing.JRadioButton radLeft;
    private javax.swing.JRadioButton radRight;
    private javax.swing.JScrollPane scpQuery;
    private javax.swing.JScrollPane scrHistory;
    private javax.swing.JSlider sldAlpha;
    private javax.swing.JSlider sldLineWidth;
    private javax.swing.JSlider sldPointSymbolSize;
    private javax.swing.JTabbedPane tbpTabs;
    private javax.swing.JTextField txtLineWidth;
    private javax.swing.JFormattedTextField txtMax;
    private javax.swing.JFormattedTextField txtMin;
    private javax.swing.JFormattedTextField txtMultiplier;
    private javax.swing.JTextField txtPointSymbolSize;
    private javax.swing.JTextField txtTransparency;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    /**
     * Constructor for new StyleDialog-objects.
     *
     * @param  parent  parent-frame of this dialog
     * @param  modal   true, if the dialog should block the parent
     */
    public StyleDialog(final Frame parent, final boolean modal) {
        super(parent, modal);

        try {
            logger.info("Erstelle StyleDialog"); // NOI18N
            this.layerProperties = new DefaultLayerProperties();

            createPointSymbols();
            initComponents();
            createXMLEditor();
            setLocationRelativeTo(this.getParent());

            colorChooser = new JColorChooser();
            fontChooser = new FontChooserDialog(this, FONTCHOOSER_TITLE);

            // create historylist
            createHistoryListPopupMenu();
            lstHistory.setCellRenderer(new StyleHistoryListCellRenderer());
            lstHistory.addListSelectionListener(this);
            lstHistory.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        if (e.isPopupTrigger() && !popupMenu.isVisible()) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }

                    @Override
                    public void mousePressed(final MouseEvent e) {
                        if (e.isPopupTrigger() && !popupMenu.isVisible()) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });

            // load the defaultHistory if available
            defaultHistory = searchDefaultHistory();
            loadHistory(defaultHistory);

            // create listener for XML-editor
            queryEditor.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(final DocumentEvent e) {
                        chkUseQueryString.setSelected(true);
                        if (logger.isDebugEnabled()) {
                            logger.debug(e.getChange(e.getDocument().getDefaultRootElement()));
                        }
                    }

                    @Override
                    public void removeUpdate(final DocumentEvent e) {
                        chkUseQueryString.setSelected(true);
                    }

                    @Override
                    public void changedUpdate(final DocumentEvent e) {
                        chkUseQueryString.setSelected(true);
                    }
                });

            // hide not implemented functions
            chkFillPattern.setVisible(false);
            cbbFillPattern.setVisible(false);
            chkLinePattern.setVisible(false);
            cbbLinePattern.setVisible(false);

            // not yet!
            // this.updateDialog();
            // this.updatePreview();

        } catch (Throwable t) {
            logger.error("could not create StyleDialog: " + t.getMessage(), t); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        // Log4J initialisieren
        Log4JQuickConfig.configure4LumbermillOnLocalhost();

        try {
            // Look&Feel auf das des Navigators setzen
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (Exception ex) {
        }

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final StyleDialog dialog;

                    try {
                        dialog = new StyleDialog(new javax.swing.JFrame(), true);
                        dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                                @Override
                                public void windowClosing(final java.awt.event.WindowEvent e) {
                                    System.exit(0);
                                }
                            });
                        dialog.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void sldLineWidthMouseWheelMoved(final java.awt.event.MouseWheelEvent evt) { //GEN-FIRST:event_sldLineWidthMouseWheelMoved

        if (sldLineWidth.isEnabled() && sldLineWidth.isFocusOwner()) {
            sldLineWidth.setValue(sldLineWidth.getValue() - evt.getWheelRotation());
        }
    } //GEN-LAST:event_sldLineWidthMouseWheelMoved

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void radRightActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_radRightActionPerformed
        setAlignment(JLabel.RIGHT_ALIGNMENT);
    }                                                                            //GEN-LAST:event_radRightActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void radLeftActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_radLeftActionPerformed
        setAlignment(JLabel.LEFT_ALIGNMENT);
    }                                                                           //GEN-LAST:event_radLeftActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void radCenterActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_radCenterActionPerformed
        setAlignment(JLabel.CENTER_ALIGNMENT);
    }                                                                             //GEN-LAST:event_radCenterActionPerformed

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        panTabRules = new javax.swing.JPanel();
        panRulesButtons = new javax.swing.JPanel();
        cmdAdd = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        panRulesScroll = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        panRules = new javax.swing.JPanel();
        btgGeom = new javax.swing.ButtonGroup();
        btgAlignment = new javax.swing.ButtonGroup();
        panTabQuery = new javax.swing.JPanel();
        panScrollpane = new javax.swing.JPanel();
        scpQuery = new javax.swing.JScrollPane();
        panQueryCheckbox = new javax.swing.JPanel();
        chkLinewrap = new javax.swing.JCheckBox();
        chkUseQueryString = new javax.swing.JCheckBox();
        panMain = new javax.swing.JPanel();
        panInfo = new javax.swing.JPanel();
        panInfoComp = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        panPreview = new StylePreviewPanel();
        lblPreview = new javax.swing.JLabel();
        panTabs = new javax.swing.JPanel();
        tbpTabs = new javax.swing.JTabbedPane();
        panTabFill = new javax.swing.JPanel();
        panFill = new javax.swing.JPanel();
        chkFill = new javax.swing.JCheckBox();
        chkFillPattern = new javax.swing.JCheckBox();
        cbbFillPattern = new javax.swing.JComboBox();
        chkLine = new javax.swing.JCheckBox();
        chkLinePattern = new javax.swing.JCheckBox();
        cbbLinePattern = new javax.swing.JComboBox();
        chkSync = new javax.swing.JCheckBox();
        chkHighlightable = new javax.swing.JCheckBox();
        lblLineWidth = new javax.swing.JLabel();
        sldLineWidth = new javax.swing.JSlider();
        txtLineWidth = new javax.swing.JTextField();
        lblAlpha = new javax.swing.JLabel();
        txtTransparency = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        panTransWhite = new javax.swing.JPanel();
        sldAlpha = new javax.swing.JSlider();
        panTransColor = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        panFillColor = new javax.swing.JPanel();
        cmdFill = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        panLineColor = new javax.swing.JPanel();
        cmdLine = new javax.swing.JButton();
        scrHistory = new javax.swing.JScrollPane();
        lstHistory = new javax.swing.JList();
        lblHistory = new javax.swing.JLabel();
        lblPointSymbol = new javax.swing.JLabel();
        cbbPointSymbol = new javax.swing.JComboBox();
        cbbPointSymbol.setModel(new DefaultComboBoxModel(new Vector(this.pointSymbolHM.keySet())));
        lblPointSymbolSize = new javax.swing.JLabel();
        sldPointSymbolSize = new javax.swing.JSlider();
        txtPointSymbolSize = new javax.swing.JTextField();
        panTabLabeling = new javax.swing.JPanel();
        panLabeling = new javax.swing.JPanel();
        chkActivateLabels = new javax.swing.JCheckBox();
        lblAnnotationExpression = new javax.swing.JLabel();
        cbbAnnotationExpression = new javax.swing.JComboBox();
        panLabelButtons = new javax.swing.JPanel();
        cmdChangeTextColor = new javax.swing.JButton();
        cmdChangeFont = new javax.swing.JButton();
        lblFontname = new javax.swing.JLabel();
        panFontColor = new javax.swing.JPanel();
        panScale = new javax.swing.JPanel();
        lblMin = new javax.swing.JLabel();
        txtMin = new javax.swing.JFormattedTextField();
        lblMax = new javax.swing.JLabel();
        txtMax = new javax.swing.JFormattedTextField();
        chkAutoscale = new javax.swing.JCheckBox();
        panAlignment = new javax.swing.JPanel();
        radLeft = new javax.swing.JRadioButton();
        radCenter = new javax.swing.JRadioButton();
        radRight = new javax.swing.JRadioButton();
        lblAlignment = new javax.swing.JLabel();
        lblMultiplier = new javax.swing.JLabel();
        txtMultiplier = new javax.swing.JFormattedTextField();
        panTabAttrib = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        panAttribGeo = new javax.swing.JPanel();
        panAttribSeparator = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        panAttribNorm = new javax.swing.JPanel();
        lblIdExpression = new javax.swing.JLabel();
        cbbIdExpression = new javax.swing.JComboBox();
        panSLDDefinition = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        chkCustomSLD = new javax.swing.JCheckBox();
        panDialogButtons = new javax.swing.JPanel();
        cmdOK = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();

        panTabRules.setLayout(new java.awt.BorderLayout());

        panRulesButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panRulesButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        cmdAdd.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/rule_add.png")));      // NOI18N
        cmdAdd.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.cmdAdd.text")); // NOI18N
        cmdAdd.setMargin(new java.awt.Insets(2, 5, 2, 5));
        panRulesButtons.add(cmdAdd);

        cmdRemove.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/rule_remove.png")));         // NOI18N
        cmdRemove.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.cmdRemove.text")); // NOI18N
        cmdRemove.setMargin(new java.awt.Insets(2, 5, 2, 5));
        panRulesButtons.add(cmdRemove);

        panTabRules.add(panRulesButtons, java.awt.BorderLayout.SOUTH);

        panRulesScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panRulesScroll.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        panRules.setBackground(new java.awt.Color(255, 255, 255));
        panRules.setLayout(new java.awt.GridLayout(1, 0));
        jScrollPane1.setViewportView(panRules);

        panRulesScroll.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panTabRules.add(panRulesScroll, java.awt.BorderLayout.CENTER);

        panTabQuery.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createTitledBorder("Query bearbeiten")));
        panTabQuery.setLayout(new java.awt.BorderLayout());

        panScrollpane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panScrollpane.setLayout(new java.awt.BorderLayout());
        panScrollpane.add(scpQuery, java.awt.BorderLayout.CENTER);

        panTabQuery.add(panScrollpane, java.awt.BorderLayout.CENTER);

        panQueryCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panQueryCheckbox.setLayout(new java.awt.GridLayout(2, 0));

        chkLinewrap.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.chkLinewrap.text")); // NOI18N
        chkLinewrap.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkLinewrapActionPerformed(evt);
                }
            });
        panQueryCheckbox.add(chkLinewrap);

        chkUseQueryString.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.chkUseQueryString.text")); // NOI18N
        chkUseQueryString.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkUseQueryStringActionPerformed(evt);
                }
            });
        panQueryCheckbox.add(chkUseQueryString);

        panTabQuery.add(panQueryCheckbox, java.awt.BorderLayout.SOUTH);

        setTitle(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.title")); // NOI18N
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(685, 461));
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosing(final java.awt.event.WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        panMain.setMinimumSize(new java.awt.Dimension(620, 433));
        panMain.setPreferredSize(new java.awt.Dimension(620, 433));
        panMain.setLayout(new java.awt.BorderLayout());

        panInfo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    javax.swing.BorderFactory.createEtchedBorder()),
                javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panInfo.setLayout(new java.awt.BorderLayout());

        panInfoComp.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/style.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panInfoComp.add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(jLabel1);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.jLabel2.text")); // NOI18N
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panInfoComp.add(jLabel2, gridBagConstraints);

        panInfo.add(panInfoComp, java.awt.BorderLayout.NORTH);

        jPanel3.setPreferredSize(new java.awt.Dimension(150, 220));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setMinimumSize(new java.awt.Dimension(150, 200));
        jPanel1.setPreferredSize(new java.awt.Dimension(150, 150));
        jPanel1.setLayout(new java.awt.BorderLayout());

        panPreview.setBackground(new java.awt.Color(255, 255, 255));

        final javax.swing.GroupLayout panPreviewLayout = new javax.swing.GroupLayout(panPreview);
        panPreview.setLayout(panPreviewLayout);
        panPreviewLayout.setHorizontalGroup(
            panPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                146,
                Short.MAX_VALUE));
        panPreviewLayout.setVerticalGroup(
            panPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                187,
                Short.MAX_VALUE));

        jPanel1.add(panPreview, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jPanel1, gridBagConstraints);

        lblPreview.setLabelFor(panPreview);
        lblPreview.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblPreview.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        jPanel3.add(lblPreview, gridBagConstraints);

        panInfo.add(jPanel3, java.awt.BorderLayout.SOUTH);

        panMain.add(panInfo, java.awt.BorderLayout.WEST);

        panTabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 5, 5));
        panTabs.setLayout(new java.awt.BorderLayout());

        tbpTabs.setToolTipText("");

        panTabFill.setLayout(new java.awt.BorderLayout());

        panFill.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panFill.setLayout(new java.awt.GridBagLayout());

        chkFill.setSelected(true);
        chkFill.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.chkFill.text")); // NOI18N
        chkFill.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkFillItemStateChanged(evt);
                }
            });
        chkFill.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkFillActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(chkFill, gridBagConstraints);

        chkFillPattern.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.chkFillPattern.text")); // NOI18N
        chkFillPattern.setEnabled(false);
        chkFillPattern.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkFillPatternItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(chkFillPattern, gridBagConstraints);

        cbbFillPattern.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbbFillPattern.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(cbbFillPattern, gridBagConstraints);

        chkLine.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.chkLine.text")); // NOI18N
        chkLine.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkLineItemStateChanged(evt);
                }
            });
        chkLine.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkLineActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 10);
        panFill.add(chkLine, gridBagConstraints);

        chkLinePattern.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.chkLinePattern.text")); // NOI18N
        chkLinePattern.setEnabled(false);
        chkLinePattern.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkLinePatternItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(chkLinePattern, gridBagConstraints);

        cbbLinePattern.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbbLinePattern.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(cbbLinePattern, gridBagConstraints);

        chkSync.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.chkLineSync.text")); // NOI18N
        chkSync.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkSyncItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 10);
        panFill.add(chkSync, gridBagConstraints);

        chkHighlightable.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.chkHighlightable.text")); // NOI18N
        chkHighlightable.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkHighlightableItemStateChanged(evt);
                }
            });
        chkHighlightable.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkHighlightableActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        panFill.add(chkHighlightable, gridBagConstraints);

        lblLineWidth.setLabelFor(sldLineWidth);
        lblLineWidth.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblLineWidth.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(lblLineWidth, gridBagConstraints);

        sldLineWidth.setMajorTickSpacing(10);
        sldLineWidth.setMaximum(20);
        sldLineWidth.setMinorTickSpacing(1);
        sldLineWidth.setPaintLabels(true);
        sldLineWidth.setSnapToTicks(true);
        sldLineWidth.setValue(1);
        sldLineWidth.setEnabled(false);
        sldLineWidth.setMinimumSize(new java.awt.Dimension(130, 37));
        sldLineWidth.setPreferredSize(new java.awt.Dimension(130, 37));
        sldLineWidth.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

                @Override
                public void mouseWheelMoved(final java.awt.event.MouseWheelEvent evt) {
                    sldLineWidthMouseWheelMoved(evt);
                }
            });
        sldLineWidth.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(final javax.swing.event.ChangeEvent evt) {
                    sldLineWidthStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(sldLineWidth, gridBagConstraints);

        txtLineWidth.setColumns(2);
        txtLineWidth.setEditable(false);
        txtLineWidth.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtLineWidth.setText("1");
        txtLineWidth.setEnabled(false);
        txtLineWidth.setFocusable(false);
        txtLineWidth.setMinimumSize(new java.awt.Dimension(35, 20));
        txtLineWidth.setPreferredSize(new java.awt.Dimension(35, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(txtLineWidth, gridBagConstraints);

        lblAlpha.setLabelFor(jPanel7);
        lblAlpha.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblAlpha.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(lblAlpha, gridBagConstraints);

        txtTransparency.setColumns(3);
        txtTransparency.setEditable(false);
        txtTransparency.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTransparency.setText("100");
        txtTransparency.setFocusable(false);
        txtTransparency.setMinimumSize(new java.awt.Dimension(35, 20));
        txtTransparency.setPreferredSize(new java.awt.Dimension(35, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(txtTransparency, gridBagConstraints);

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));

        panTransWhite.setBackground(new java.awt.Color(255, 255, 255));
        panTransWhite.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        panTransWhite.setMaximumSize(new java.awt.Dimension(14, 14));
        panTransWhite.setMinimumSize(new java.awt.Dimension(14, 14));
        panTransWhite.setPreferredSize(new java.awt.Dimension(14, 14));

        final javax.swing.GroupLayout panTransWhiteLayout = new javax.swing.GroupLayout(panTransWhite);
        panTransWhite.setLayout(panTransWhiteLayout);
        panTransWhiteLayout.setHorizontalGroup(
            panTransWhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                12,
                Short.MAX_VALUE));
        panTransWhiteLayout.setVerticalGroup(
            panTransWhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                12,
                Short.MAX_VALUE));

        jPanel7.add(panTransWhite);

        sldAlpha.setMajorTickSpacing(10);
        sldAlpha.setMinorTickSpacing(1);
        sldAlpha.setSnapToTicks(true);
        sldAlpha.setValue(100);
        sldAlpha.setMinimumSize(new java.awt.Dimension(100, 23));
        sldAlpha.setPreferredSize(new java.awt.Dimension(100, 23));
        sldAlpha.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

                @Override
                public void mouseWheelMoved(final java.awt.event.MouseWheelEvent evt) {
                    sldAlphaMouseWheelMoved(evt);
                }
            });
        sldAlpha.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(final javax.swing.event.ChangeEvent evt) {
                    sldAlphaStateChanged(evt);
                }
            });
        jPanel7.add(sldAlpha);

        panTransColor.setBackground(new java.awt.Color(0, 180, 0));
        panTransColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        panTransColor.setMaximumSize(new java.awt.Dimension(14, 14));
        panTransColor.setMinimumSize(new java.awt.Dimension(14, 14));
        panTransColor.setPreferredSize(new java.awt.Dimension(14, 14));

        final javax.swing.GroupLayout panTransColorLayout = new javax.swing.GroupLayout(panTransColor);
        panTransColor.setLayout(panTransColorLayout);
        panTransColorLayout.setHorizontalGroup(
            panTransColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                12,
                Short.MAX_VALUE));
        panTransColorLayout.setVerticalGroup(
            panTransColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                12,
                Short.MAX_VALUE));

        jPanel7.add(panTransColor);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(jPanel7, gridBagConstraints);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        panFillColor.setBackground(new java.awt.Color(0, 180, 0));
        panFillColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        panFillColor.setMaximumSize(new java.awt.Dimension(35, 15));
        panFillColor.setMinimumSize(new java.awt.Dimension(35, 15));
        panFillColor.setPreferredSize(new java.awt.Dimension(35, 15));

        final javax.swing.GroupLayout panFillColorLayout = new javax.swing.GroupLayout(panFillColor);
        panFillColor.setLayout(panFillColorLayout);
        panFillColorLayout.setHorizontalGroup(
            panFillColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                33,
                Short.MAX_VALUE));
        panFillColorLayout.setVerticalGroup(
            panFillColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                13,
                Short.MAX_VALUE));

        jPanel8.add(panFillColor);

        cmdFill.setText("...");
        cmdFill.setMaximumSize(new java.awt.Dimension(90, 18));
        cmdFill.setMinimumSize(new java.awt.Dimension(30, 18));
        cmdFill.setPreferredSize(new java.awt.Dimension(30, 18));
        cmdFill.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdFillActionPerformed(evt);
                }
            });
        jPanel8.add(cmdFill);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(jPanel8, gridBagConstraints);

        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        panLineColor.setBackground(new java.awt.Color(0, 125, 0));
        panLineColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        panLineColor.setMaximumSize(new java.awt.Dimension(35, 15));
        panLineColor.setMinimumSize(new java.awt.Dimension(35, 15));
        panLineColor.setPreferredSize(new java.awt.Dimension(35, 15));

        final javax.swing.GroupLayout panLineColorLayout = new javax.swing.GroupLayout(panLineColor);
        panLineColor.setLayout(panLineColorLayout);
        panLineColorLayout.setHorizontalGroup(
            panLineColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                33,
                Short.MAX_VALUE));
        panLineColorLayout.setVerticalGroup(
            panLineColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                13,
                Short.MAX_VALUE));

        jPanel9.add(panLineColor);

        cmdLine.setText("...");
        cmdLine.setEnabled(false);
        cmdLine.setMaximumSize(new java.awt.Dimension(30, 18));
        cmdLine.setMinimumSize(new java.awt.Dimension(30, 18));
        cmdLine.setPreferredSize(new java.awt.Dimension(30, 18));
        cmdLine.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdLineActionPerformed(evt);
                }
            });
        jPanel9.add(cmdLine);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 10);
        panFill.add(jPanel9, gridBagConstraints);

        scrHistory.setMinimumSize(new java.awt.Dimension(80, 50));
        scrHistory.setPreferredSize(new java.awt.Dimension(80, 50));

        lstHistory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrHistory.setViewportView(lstHistory);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 30);
        panFill.add(scrHistory, gridBagConstraints);

        lblHistory.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        lblHistory.setLabelFor(lstHistory);
        lblHistory.setText("history.xml");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        panFill.add(lblHistory, gridBagConstraints);

        lblPointSymbol.setLabelFor(cbbPointSymbol);
        lblPointSymbol.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.lblPointSymbol.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
        panFill.add(lblPointSymbol, gridBagConstraints);

        cbbPointSymbol.setMaximumRowCount(9);
        cbbPointSymbol.setMinimumSize(new java.awt.Dimension(45, 25));
        cbbPointSymbol.setPreferredSize(new java.awt.Dimension(45, 25));
        cbbPointSymbol.setRenderer(new PointSymbolListRenderer());
        cbbPointSymbol.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbbPointSymbolItemStateChanged(evt);
                }
            });
        cbbPointSymbol.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbbPointSymbolActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
        panFill.add(cbbPointSymbol, gridBagConstraints);

        lblPointSymbolSize.setLabelFor(sldPointSymbolSize);
        lblPointSymbolSize.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.lblPointSymbolSize.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 10);
        panFill.add(lblPointSymbolSize, gridBagConstraints);

        sldPointSymbolSize.setMajorTickSpacing(10);
        sldPointSymbolSize.setMaximum(Style.MAX_POINTSYMBOLSIZE);
        sldPointSymbolSize.setMinimum(Style.MIN_POINTSYMBOLSIZE);
        sldPointSymbolSize.setMinorTickSpacing(1);
        sldPointSymbolSize.setPaintLabels(true);
        sldPointSymbolSize.setSnapToTicks(true);
        sldPointSymbolSize.setValue(Style.MIN_POINTSYMBOLSIZE);
        sldPointSymbolSize.setMinimumSize(new java.awt.Dimension(130, 37));
        sldPointSymbolSize.setPreferredSize(new java.awt.Dimension(130, 37));
        sldPointSymbolSize.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

                @Override
                public void mouseWheelMoved(final java.awt.event.MouseWheelEvent evt) {
                    sldPointSymbolSizeMouseWheelMoved(evt);
                }
            });
        sldPointSymbolSize.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(final javax.swing.event.ChangeEvent evt) {
                    sldPointSymbolSizeStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(sldPointSymbolSize, gridBagConstraints);

        txtPointSymbolSize.setColumns(2);
        txtPointSymbolSize.setEditable(false);
        txtPointSymbolSize.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPointSymbolSize.setText("10");
        txtPointSymbolSize.setFocusable(false);
        txtPointSymbolSize.setMinimumSize(new java.awt.Dimension(35, 20));
        txtPointSymbolSize.setPreferredSize(new java.awt.Dimension(35, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(txtPointSymbolSize, gridBagConstraints);

        panTabFill.add(panFill, java.awt.BorderLayout.WEST);

        tbpTabs.addTab(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.tbpTabs.tab1.title"),
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/style_color.png")),
            panTabFill); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                panTabLabeling,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                panTabLabeling,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                panTabLabeling,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        panTabLabeling.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 20));

        panLabeling.setLayout(new java.awt.GridBagLayout());

        chkActivateLabels.setSelected(true);
        chkActivateLabels.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.chkActivateLabels.text")); // NOI18N
        chkActivateLabels.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkActivateLabelsItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        panLabeling.add(chkActivateLabels, gridBagConstraints);

        lblAnnotationExpression.setLabelFor(cbbAnnotationExpression);
        lblAnnotationExpression.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.lblAttrib.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 15);
        panLabeling.add(lblAnnotationExpression, gridBagConstraints);

        cbbAnnotationExpression.setEditable(true);
        cbbAnnotationExpression.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbbAnnotationExpressionItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panLabeling.add(cbbAnnotationExpression, gridBagConstraints);

        panLabelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(
                org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.panLabelButtons.border.title"))); // NOI18N
        panLabelButtons.setLayout(new java.awt.GridBagLayout());

        cmdChangeTextColor.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.cmdChangeColor.text")); // NOI18N
        cmdChangeTextColor.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdChangeTextColorActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        panLabelButtons.add(cmdChangeTextColor, gridBagConstraints);

        cmdChangeFont.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.cmdChangeFont.text")); // NOI18N
        cmdChangeFont.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdChangeFontActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        panLabelButtons.add(cmdChangeFont, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        panLabelButtons.add(lblFontname, gridBagConstraints);

        panFontColor.setBackground(new java.awt.Color(0, 0, 0));
        panFontColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        panFontColor.setMinimumSize(new java.awt.Dimension(20, 20));
        panFontColor.setPreferredSize(new java.awt.Dimension(20, 20));

        final javax.swing.GroupLayout panFontColorLayout = new javax.swing.GroupLayout(panFontColor);
        panFontColor.setLayout(panFontColorLayout);
        panFontColorLayout.setHorizontalGroup(
            panFontColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                18,
                Short.MAX_VALUE));
        panFontColorLayout.setVerticalGroup(
            panFontColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                18,
                Short.MAX_VALUE));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 0);
        panLabelButtons.add(panFontColor, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        panLabeling.add(panLabelButtons, gridBagConstraints);

        panScale.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        lblMin.setLabelFor(txtMin);
        lblMin.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblMin.text")); // NOI18N
        panScale.add(lblMin);

        txtMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
                new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtMin.setText("1");
        txtMin.setMinimumSize(new java.awt.Dimension(60, 20));
        txtMin.setPreferredSize(new java.awt.Dimension(60, 20));
        panScale.add(txtMin);

        lblMax.setLabelFor(txtMax);
        lblMax.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblMax.text")); // NOI18N
        panScale.add(lblMax);

        txtMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
                new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtMax.setText("2500");
        txtMax.setMinimumSize(new java.awt.Dimension(60, 20));
        txtMax.setPreferredSize(new java.awt.Dimension(60, 20));
        panScale.add(txtMax);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        panLabeling.add(panScale, gridBagConstraints);

        chkAutoscale.setSelected(true);
        chkAutoscale.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.chkAutoscale.text")); // NOI18N
        chkAutoscale.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chkAutoscaleItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        panLabeling.add(chkAutoscale, gridBagConstraints);

        panAlignment.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        btgAlignment.add(radLeft);
        radLeft.setSelected(true);
        radLeft.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.radleft.text")); // NOI18N
        radLeft.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    radLeftActionPerformed(evt);
                }
            });
        panAlignment.add(radLeft);

        btgAlignment.add(radCenter);
        radCenter.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.radCenter.text")); // NOI18N
        radCenter.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    radCenterActionPerformed(evt);
                }
            });
        panAlignment.add(radCenter);

        btgAlignment.add(radRight);
        radRight.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.radRight.text")); // NOI18N
        radRight.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    radRightActionPerformed(evt);
                }
            });
        panAlignment.add(radRight);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panLabeling.add(panAlignment, gridBagConstraints);

        lblAlignment.setLabelFor(panAlignment);
        lblAlignment.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblAlignment.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 15);
        panLabeling.add(lblAlignment, gridBagConstraints);

        lblMultiplier.setLabelFor(txtMultiplier);
        lblMultiplier.setText(org.openide.util.NbBundle.getMessage(
                StyleDialog.class,
                "StyleDialog.lblMultiplier.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 15);
        panLabeling.add(lblMultiplier, gridBagConstraints);

        txtMultiplier.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
                new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
        txtMultiplier.setText("1,00");
        txtMultiplier.setMinimumSize(new java.awt.Dimension(40, 20));
        txtMultiplier.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panLabeling.add(txtMultiplier, gridBagConstraints);

        panTabLabeling.add(panLabeling);

        tbpTabs.addTab(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.tbpTabs.tab2.title"),
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/labelling.png")),
            panTabLabeling); // NOI18N

        panTabAttrib.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createTitledBorder(
                    org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.panTabAttrib.border.title")))); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                panTabAttrib,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        panTabAttrib.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createTitledBorder(
                    org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.panTabAttrib.border.title")))); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                panTabAttrib,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        panTabAttrib.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createTitledBorder(
                    org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.panTabAttrib.border.title")))); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                panTabAttrib,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        panTabAttrib.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        jPanel2.setLayout(new java.awt.GridBagLayout());

        panAttribGeo.setLayout(new javax.swing.BoxLayout(panAttribGeo, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 20);
        jPanel2.add(panAttribGeo, gridBagConstraints);

        panAttribSeparator.setLayout(new java.awt.BorderLayout());
        panAttribSeparator.add(jSeparator1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel2.add(panAttribSeparator, gridBagConstraints);

        panAttribNorm.setLayout(new javax.swing.BoxLayout(panAttribNorm, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 20);
        jPanel2.add(panAttribNorm, gridBagConstraints);

        lblIdExpression.setLabelFor(cbbIdExpression);
        lblIdExpression.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.lblPrimary.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 10);
        jPanel2.add(lblIdExpression, gridBagConstraints);

        cbbIdExpression.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbbIdExpressionItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(cbbIdExpression, gridBagConstraints);

        panTabAttrib.add(jPanel2);

        tbpTabs.addTab(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.tbpTabs.tab3.title"),
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/attributes.png")),
            panTabAttrib); // NOI18N
        panTabAttrib.getAccessibleContext().setAccessibleName(null);

        panSLDDefinition.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
                javax.swing.BorderFactory.createTitledBorder("SLD Definition")));
        panSLDDefinition.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.GridBagLayout());

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                chkCustomSLD,
                org.jdesktop.beansbinding.ELProperty.create("${!selected}"),
                jEditorPane1,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jScrollPane2.setViewportView(jEditorPane1);
        jEditorPane1.getAccessibleContext().setAccessibleName("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel4.add(jScrollPane2, gridBagConstraints);

        chkCustomSLD.setSelected(true);
        chkCustomSLD.setText("Expertenmodus");
        chkCustomSLD.setToolTipText("");
        chkCustomSLD.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icon-unlock.png"))); // NOI18N
        chkCustomSLD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon-unlock.png")));                 // NOI18N
        chkCustomSLD.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icon-lock.png")));           // NOI18N
        chkCustomSLD.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    chkCustomSLDActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(chkCustomSLD, gridBagConstraints);

        panSLDDefinition.add(jPanel4, java.awt.BorderLayout.CENTER);

        tbpTabs.addTab("SLD Definition", panSLDDefinition);

        panTabs.add(tbpTabs, java.awt.BorderLayout.CENTER);
        tbpTabs.getAccessibleContext()
                .setAccessibleName(org.openide.util.NbBundle.getMessage(
                        StyleDialog.class,
                        "StyleDialog.tbpTabs.tab1.title")); // NOI18N

        panMain.add(panTabs, java.awt.BorderLayout.CENTER);

        getContentPane().add(panMain, java.awt.BorderLayout.CENTER);

        panDialogButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, -5));
        panDialogButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));

        cmdOK.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.cmdIOK.text")); // NOI18N
        cmdOK.setMaximumSize(new java.awt.Dimension(88, 23));
        cmdOK.setMinimumSize(new java.awt.Dimension(88, 23));
        cmdOK.setPreferredSize(new java.awt.Dimension(88, 23));
        cmdOK.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOKActionPerformed(evt);
                }
            });
        panDialogButtons.add(cmdOK);

        cmdCancel.setText(org.openide.util.NbBundle.getMessage(StyleDialog.class, "StyleDialog.cmdCancel.text")); // NOI18N
        cmdCancel.setMaximumSize(new java.awt.Dimension(88, 23));
        cmdCancel.setMinimumSize(new java.awt.Dimension(88, 23));
        cmdCancel.setPreferredSize(new java.awt.Dimension(88, 23));
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCancelActionPerformed(evt);
                }
            });
        panDialogButtons.add(cmdCancel);

        getContentPane().add(panDialogButtons, java.awt.BorderLayout.SOUTH);

        bindingGroup.bind();

        pack();
    } // </editor-fold>//GEN-END:initComponents

    //~ Instance fields --------------------------------------------------------

    private String layerName;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkLineActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkLineActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_chkLineActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chkCustomSLDActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkCustomSLDActionPerformed
        if (!chkCustomSLD.isSelected()) {
            final int i = JOptionPane.showConfirmDialog(
                    this,
                    "Das aktivieren des Expertenmodus übernimmt die Füllfarbe,\nLinienfarbe und Linienstaerke aus dem einfachen Stil",
                    "Sind Sie sicher?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (i == JOptionPane.YES_OPTION) {
                jEditorPane1.setText(getSLDStyle(true));
            } else {
                chkCustomSLD.setSelected(true);
                return;
            }
        }
        tbpTabs.setEnabledAt(tbpTabs.indexOfComponent(panTabFill), chkCustomSLD.isSelected());
        tbpTabs.setEnabledAt(tbpTabs.indexOfComponent(panTabLabeling), chkCustomSLD.isSelected());
        tbpTabs.setEnabledAt(tbpTabs.indexOfComponent(panTabAttrib), chkCustomSLD.isSelected());
    }                                                                                //GEN-LAST:event_chkCustomSLDActionPerformed

    /**
     * Returns a modified CloneableFeature.
     *
     * @return  CloneableFeature with the current style
     */
// public CloneableFeature getReturnStatus()
// {
// return feature;
// }

    public String getSLDStyle() {
        return chkCustomSLD.isSelected() ? "" : getSLDStyle(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   simpleStyle  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSLDStyle(final boolean simpleStyle) {
        if (simpleStyle) {
            String sld =
                "<sld:StyledLayerDescriptor xmlns:sld=\"http://www.opengis.net/sld\" xmlns:se=\"http://www.opengis.net/se\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n"
                        + "  xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"\n"
                        + "  xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns=\"http://www.opengis.net/sld\" version=\"1.1.0\"\n"
                        + "  xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd\">\n"
                        + "  <sld:NamedLayer>\n"
                        + "    <!--  This styling file shows the use of SLD styling -->\n"
                        + "    <sld:Name>"
                        + layerName
                        + "</sld:Name>\n" // todo correct layer name
                        + "        <sld:UserStyle>\n"
                        + "            <sld:Name>"
                        + layerName
                        + "</sld:Name>\n"
                        + "    <sld:Title>"
                        + layerName
                        + "</sld:Title>\n"
                        + "            <sld:FeatureTypeStyle>\n"
                        + "                <sld:Name>"
                        + layerName
                        + "</sld:Name>\n"
                        + "                <sld:Rule>\n"
                        + "                    <sld:Name>"
                        + layerName
                        + "</sld:Name>\n";
            sld += "                    <sld:PolygonSymbolizer uom=\"http://www.opengeospatial/se/units/pixel\">\n";

            if (chkFill.isSelected()) {
                sld += "                        <sld:Fill>\n";
                sld += "                            <sld:CssParameter name=\"fill\">";
                sld += "#"
                            + Integer.toHexString(panFillColor.getBackground().getRGB()).substring(2).toUpperCase();
                sld += "</sld:CssParameter>\n";

                if (panFillColor.getBackground().getAlpha() != 255) {
                    sld += "                            <sld:CssParameter name=\"fill-opacity\">";
                    sld += (float)((panFillColor.getBackground().getAlpha() * 100) / 255)
                                * 0.01f;
                    sld += "</sld:CssParameter>\n";
                }
                sld += "                        </sld:Fill>\n";
            }
            if (chkLine.isSelected()) {
                sld += "                        <sld:Stroke>\n";
                sld += "                            <sld:CssParameter name=\"stroke\">";
                sld += "#"
                            + Integer.toHexString(panLineColor.getBackground().getRGB()).substring(2).toUpperCase();
                sld += "</sld:CssParameter>\n";
                if (panLineColor.getBackground().getAlpha() != 255) {
                    sld += "                            <sld:CssParameter name=\"stroke-opacity\">";
                    sld += (float)((panLineColor.getBackground().getAlpha() * 100) / 255)
                                * 0.01f;
                    sld += "</sld:CssParameter>\n";
                }
                sld += "                            <sld:CssParameter name=\"stroke-width\">";
                sld += sldLineWidth.getValue();
                sld += "</sld:CssParameter>\n";
//"                            <!--<se:SvgParameter name=\"stroke-dasharray\">5,7.5,10,2.5</se:SvgParameter>-->\n" +
//"                            <se:SvgParameter name=\"stroke-linecap\">butt</se:SvgParameter>\n" +
                sld += "                        </sld:Stroke>\n";
            }

            sld += "                    </sld:PolygonSymbolizer>\n";
            sld += ("                </sld:Rule>\n"
                            + "            </sld:FeatureTypeStyle>\n"
                            + "        </sld:UserStyle>\n"
                            + "    </sld:NamedLayer>\n"
                            + "</sld:StyledLayerDescriptor>");
            return sld;                    // new StringReader(sld);
        } else {
            return jEditorPane1.getText(); // new StringReader(jEditorPane1.getText());
        }
    }

    /**
     * Get the value of layerProperties.
     *
     * @return  the value of layerProperties
     */
    public LayerProperties getLayerProperties() {
        if (!this.isAccepted()) {
            logger.warn("supicious call to 'getLayerProperties()', changes not accepted"); // NOI18N
        }

        return this.layerProperties;
    }

    /**
     * Set the value of layerProperties.
     *
     * @param  layerProperties  new value of layerProperties
     */
    protected void setLayerProperties(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties.clone();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Style getStyle() {
        return this.layerProperties.getStyle();
    }

    /**
     * Closes the dialog and sets the returnvalue.
     *
     * @param  accepted  retStatus new returnvalue
     */
    private void doClose(final boolean accepted) {
        this.setAccepted(accepted);
        tbpTabs.setSelectedComponent(panTabFill);
        lstHistory.setSelectedIndex(-1);
        setVisible(false);
        dispose();
    }

    /**
     * Creates the XML-editor and adds it to the scrollpane.
     */
    private void createXMLEditor() {
        try {
            final XMLEditorKit kit = new XMLEditorKit(true);
            kit.setWrapStyleWord(true);
            kit.setLineWrappingEnabled(chkLinewrap.isSelected());

            queryEditor.setEditorKit(kit);
            queryEditor.setFont(new Font("Monospace", Font.PLAIN, 12)); // NOI18N
            queryEditor.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
            queryEditor.getDocument().putProperty(XMLDocument.AUTO_INDENTATION_ATTRIBUTE, new Boolean(true));
            queryEditor.getDocument().putProperty(XMLDocument.TAG_COMPLETION_ATTRIBUTE, new Boolean(true));

            // Set style
            kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, Color.GREEN.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.ATTRIBUTE_VALUE, Color.MAGENTA.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.COMMENT, Color.GRAY, Font.PLAIN);
            kit.setStyle(XMLStyleConstants.DECLARATION, Color.DARK_GRAY, Font.BOLD);
            kit.setStyle(XMLStyleConstants.ELEMENT_NAME, Color.BLUE, Font.PLAIN);
            kit.setStyle(XMLStyleConstants.ELEMENT_PREFIX, Color.BLUE, Font.PLAIN);
            kit.setStyle(XMLStyleConstants.ELEMENT_VALUE, Color.BLACK, Font.BOLD);
            kit.setStyle(XMLStyleConstants.NAMESPACE_NAME, Color.GREEN.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.NAMESPACE_VALUE, Color.MAGENTA.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.NAMESPACE_PREFIX, Color.GREEN.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.SPECIAL, Color.BLACK, Font.PLAIN);

            // ScrollableEditorPanel forces the queryEditor to resize
            final ScrollableEditorPanel editorPanel = new ScrollableEditorPanel(queryEditor);

            scpQuery.setViewportView(editorPanel);

            // Add the number margin as a Row Header View
            scpQuery.setRowHeaderView(new LineNumberMargin(queryEditor));
        } catch (Exception ex) {
            logger.error("Error during the creation of the QueryEditor", ex); // NOI18N
        }
    }

    /**
     * Creates all default FeatureAnnotationSymbols and stores them in the pointSymbol-Hashmap.
     */
    private void createPointSymbols() {
        // pointSymbolList.addElement(getStyle().NO_POINTSYMBOL);
        pointSymbolHM.put(Style.NO_POINTSYMBOL, null);
        pointSymbolHM.put(Style.AUTO_POINTSYMBOL, null);
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getResource(POINTSYMBOL_FOLDER + "pushpin.png")); // NOI18N
        }

        final FeatureAnnotationSymbol pushPin = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "pushpin.png")).getImage()); // NOI18N
        pushPin.setSweetSpotX(0.14d);
        pushPin.setSweetSpotY(1.0d);

        // pointSymbolList.addElement("pushpin.png");
        pointSymbolHM.put("pushpin.png", pushPin); // NOI18N

        final FeatureAnnotationSymbol arrowBlue = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "arrow-blue-down.png")).getImage()); // NOI18N
        arrowBlue.setSweetSpotX(0.5d);
        arrowBlue.setSweetSpotY(1.0d);

        // pointSymbolList.addElement("arrow-blue-down.png");
        pointSymbolHM.put("arrow-blue-down.png", arrowBlue); // NOI18N

        final FeatureAnnotationSymbol arrowGreen = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "arrow-green-down.png")).getImage()); // NOI18N
        arrowGreen.setSweetSpotX(0.5d);
        arrowGreen.setSweetSpotY(1.0d);

        // pointSymbolList.addElement("arrow-green-down.png");
        pointSymbolHM.put("arrow-green-down.png", arrowGreen); // NOI18N

        final FeatureAnnotationSymbol flagBlack = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "flag-black.png")).getImage()); // NOI18N
        flagBlack.setSweetSpotX(0.18d);
        flagBlack.setSweetSpotY(0.96d);

        // pointSymbolList.addElement("flag-black.png");
        pointSymbolHM.put("flag-black.png", flagBlack); // NOI18N

        final FeatureAnnotationSymbol flagBlue = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "flag-blue.png")).getImage()); // NOI18N
        flagBlue.setSweetSpotX(0.18d);
        flagBlue.setSweetSpotY(0.96d);

        // pointSymbolList.addElement("flag-blue.png");
        pointSymbolHM.put("flag-blue.png", flagBlue); // NOI18N

        final FeatureAnnotationSymbol flagGreen = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "flag-green.png")).getImage()); // NOI18N
        flagGreen.setSweetSpotX(0.18d);
        flagGreen.setSweetSpotY(0.96d);

        // pointSymbolList.addElement("flag-green.png");
        pointSymbolHM.put("flag-green.png", flagGreen); // NOI18N

        final FeatureAnnotationSymbol flagRed = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "flag-red.png")).getImage()); // NOI18N
        flagRed.setSweetSpotX(0.18d);
        flagRed.setSweetSpotY(0.96d);

        // pointSymbolList.addElement("flag-red.png");
        pointSymbolHM.put("flag-red.png", flagRed); // NOI18N

        final FeatureAnnotationSymbol flagYellow = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "flag-yellow.png")).getImage()); // NOI18N
        flagYellow.setSweetSpotX(0.18d);
        flagYellow.setSweetSpotY(0.96d);

        // pointSymbolList.addElement("flag-yellow.png");
        pointSymbolHM.put("flag-yellow.png", flagYellow); // NOI18N

        final FeatureAnnotationSymbol starBlack = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "star-black.png")).getImage()); // NOI18N
        starBlack.setSweetSpotX(0.5d);
        starBlack.setSweetSpotY(0.5d);

        // pointSymbolList.addElement("star-black.png");
        pointSymbolHM.put("star-black.png", starBlack); // NOI18N

        final FeatureAnnotationSymbol starYellow = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "star-yellow.png")).getImage()); // NOI18N
        starYellow.setSweetSpotX(0.5d);
        starYellow.setSweetSpotY(0.5d);

        // pointSymbolList.addElement("star-yellow.png");
        pointSymbolHM.put("star-yellow.png", starYellow); // NOI18N

        final FeatureAnnotationSymbol infoButton = new FeatureAnnotationSymbol(new ImageIcon(
                    getClass().getResource(POINTSYMBOL_FOLDER + "info.png")).getImage()); // NOI18N
        infoButton.setSweetSpotX(0.5d);
        infoButton.setSweetSpotY(0.5d);

        // pointSymbolList.addElement("info.png");
        pointSymbolHM.put("info.png", infoButton); // NOI18N
    }

    /**
     * Calls update() of the StylePreviewPanel.
     */
    private void updatePreview() {
        ((StylePreviewPanel)panPreview).update(getStyle(), getStyle().getPointSymbol());

        if (cbbPointSymbol.getSelectedItem().equals(getStyle().AUTO_POINTSYMBOL)
                    && !sldLineWidth.getValueIsAdjusting() && !sldPointSymbolSize.getValueIsAdjusting()) {
            if (pointSymbol == null) {
                // pointSymbol = new FeatureAnnotationSymbol(((StylePreviewPanel) panPreview).getPointSymbol());
                // pointSymbol.setSweetSpotX(0.5d); pointSymbol.setSweetSpotY(0.5d);

                this.pointSymbol = ((BasicStyle)this.layerProperties.getStyle()).createAutoPointSymbol();
            } else {
                pointSymbol.setImage(((StylePreviewPanel)panPreview).getPointSymbol());
            }
        }
    }

    /**
     * Searches the defaultStyleHistory.xml-file in the default-directory. If the file doesn't exist it will be created
     * if possible.
     *
     * @return  searches the defaultStyleHistory.xml-file in the default-directory.
     */
    private File searchDefaultHistory() {
        if (logger.isDebugEnabled()) {
            logger.debug("search for " + DEFAULT_HISTORY_NAME); // NOI18N
        }

        if (fileToCismapFolder.exists() && fileToCismapFolder.isDirectory()) { // .cismap exists

            // does defaultStyleHistory.xml exist?
            final File test = new File(fileToCismapFolder.getPath() + seperator + DEFAULT_HISTORY_NAME);

            if (test.exists() && test.isFile() && test.canRead() && test.canWrite()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(DEFAULT_HISTORY_NAME + " found"); // NOI18N
                }

                return test;
            } else {
                return createDefaultHistory();
            }
        } else { // .cismap doesn't exist, hence no history
            fileToCismapFolder.mkdir();

            return createDefaultHistory();
        }
    }

    /**
     * Tries to create a new defaultStyleHistory.xml in /user/.cismap/
     *
     * @return  file-object if successfully created or null
     */
    private File createDefaultHistory() {
        try {
            // create defaultStyleHistory.xml
            final File newFile = new File(fileToCismapFolder.getPath() + seperator + DEFAULT_HISTORY_NAME);
            newFile.createNewFile();
            if (logger.isDebugEnabled()) {
                logger.debug(DEFAULT_HISTORY_NAME + " successfully created"); // NOI18N
            }

            return newFile;
        } catch (IOException ex) {
            logger.error(DEFAULT_HISTORY_NAME + " could not create", ex); // NOI18N

            return null;
        }
    }

    /**
     * Writes the current historylist-content into a XML-file.
     *
     * @param  f        targetfile
     * @param  onClose  true, if the current style should be added to the history before writing, else false
     */
    private void writeHistory(final File f, final boolean onClose) {
        if (logger.isDebugEnabled()) {
            logger.debug("writeHistory(" + f + ")"); // NOI18N
        }

        final Runnable writeHistoryRunnable = new Runnable() {

                @Override
                public void run() {
                    FileWriter writer = null;

                    try {
                        f.createNewFile();

                        if (f.canWrite()) {
                            if (onClose) {
                                ((StyleHistoryListModel)lstHistory.getModel()).addStyle((Style)getStyle().clone());
                            }

                            final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat()); // NOI18N

                            final Document doc = new Document(((StyleHistoryListModel)lstHistory.getModel())
                                            .toElement());
                            writer = new FileWriter(f);
                            out.output(doc, writer);

                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        lblHistory.setText(f.getName());
                                    }
                                });
                        }
                    } catch (Exception ex) {
                        logger.error("Error during writing the history.", ex); // NOI18N
                        JOptionPane.showMessageDialog(
                            StaticSwingTools.getParentFrame(StyleDialog.this),
                            org.openide.util.NbBundle.getMessage(
                                StyleDialog.class,
                                "StyleDialog.writeHistory(File,boolean).JOptionPane.message",
                                new Object[] { ex.getMessage() }),             // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                StyleDialog.class,
                                "StyleDialog.writeHistory(File,boolean).JOptionPane.title"),
                            JOptionPane.ERROR_MESSAGE);                        // NOI18N
                    } finally {
                        try {
                            writer.close();
                        } catch (Exception skip) {
                        }
                    }
                }
            };
        CismetThreadPool.execute(writeHistoryRunnable);
    }

    /**
     * Loads the history from a XML-file.
     *
     * @param  f  historyfile
     */
    private void loadHistory(final File f) {
        if (logger.isDebugEnabled()) {
            logger.debug("loadHistory(" + f + ")"); // NOI18N
        }

        final Runnable loadHistoryRunnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        final StyleHistoryListModel model = new StyleHistoryListModel(f);
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    lstHistory.setModel(model);
                                    lblHistory.setText(f.getName());
                                    defaultHistory = f;
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(f + " successfully loaded"); // NOI18N
                                    }
                                }
                            });
                    } catch (Exception ex) {
                        logger.error("Error during loading of the history", ex);  // NOI18N
                        JOptionPane.showMessageDialog(
                            StaticSwingTools.getParentFrame(StyleDialog.this),
                            org.openide.util.NbBundle.getMessage(
                                StyleDialog.class,
                                "StyleDialog.loadHistory().JOptionPane.message",
                                new Object[] { ex.getMessage() }),                // NOI18N
                            org.openide.util.NbBundle.getMessage(
                                StyleDialog.class,
                                "StyleDialog.loadHistory().JOptionPane.title"),
                            JOptionPane.ERROR_MESSAGE);                           // NOI18N
                    }
                }
            };
        CismetThreadPool.execute(loadHistoryRunnable);
    }

    /**
     * Create the popupmenu to manipulate the stylehistory.
     */
    private void createHistoryListPopupMenu() {
        final FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    if ((f.isFile() && f.getName().endsWith(".xml")) || f.isDirectory()) // NOI18N
                    {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            StyleDialog.class,
                            "StyleDialog.createHistoryListPopupMenu().description"); // NOI18N
                }
            };

        final JMenuItem save = new JMenuItem();
        save.setText(POPUP_SAVE);

        try {
            save.setIcon(new ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/featureservice/res/save.png"))); // NOI18N
        } catch (Exception skipIcon) {
        }

        save.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    try {
                        JFileChooser fc;

                        try {
                            fc = new JFileChooser(home + seperator + CISMAP_FOLDER);
                        } catch (Exception bug) {
                            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
                            fc = new JFileChooser(home + seperator + CISMAP_FOLDER, new RestrictedFileSystemView());
                        }

                        fc.setFileFilter(filter);
                        fc.setMultiSelectionEnabled(false);
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

                        final int returnValue = fc.showSaveDialog(StyleDialog.this);

                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            File dst = fc.getSelectedFile();

                            if (!dst.getName().endsWith(".xml"))         // NOI18N
                            {
                                dst = new File(dst.toString() + ".xml"); // NOI18N
                            }

                            writeHistory(dst, false);
                            defaultHistory = dst;
                        }
                    } catch (Throwable ex) {
                        logger.error("Error during opening the Open Dialog of the style history", ex); // NOI18N
                    }
                }
            });

        final JMenuItem open = new JMenuItem();
        open.setText(POPUP_LOAD);

        try {
            open.setIcon(new ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/featureservice/res/open.png"))); // NOI18N
        } catch (Exception skipIcon) {
        }

        open.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    try {
                        JFileChooser fc;

                        try {
                            fc = new JFileChooser(home + seperator + CISMAP_FOLDER);
                        } catch (Exception bug) {
                            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
                            fc = new JFileChooser(home + seperator + CISMAP_FOLDER, new RestrictedFileSystemView());
                        }

                        fc.setFileFilter(filter);
                        fc.setMultiSelectionEnabled(false);
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

                        final int returnVal = fc.showOpenDialog(StyleDialog.this);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            final File fileToLoad = fc.getSelectedFile();
                            loadHistory(fileToLoad);
                        }
                    } catch (Throwable ex) {
                        logger.error("Error in open dialog of the StyleHistory", ex); // NOI18N
                    }
                }
            });

        final JMenuItem clear = new JMenuItem();
        clear.setText(POPUP_CLEAR);

        try {
            clear.setIcon(new ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/featureservice/res/delete_history.png"))); // NOI18N
        } catch (Exception skipIcon) {
        }

        clear.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final StyleHistoryListModel model = (StyleHistoryListModel)lstHistory.getModel();
                    model.clear();
                    lstHistory.repaint();
                }
            });

        popupMenu = new JPopupMenu();
        popupMenu.add(save);
        popupMenu.add(open);
        popupMenu.add(new JSeparator());
        popupMenu.add(clear);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerProperties           DOCUMENT ME!
     * @param  featureServiceAttributes  DOCUMENT ME!
     * @param  query                     DOCUMENT ME!
     */
    public void configureDialog(final LayerProperties layerProperties,
            final Map<String, FeatureServiceAttribute> featureServiceAttributes,
            final Object query) {
        configureDialog(null, "default", layerProperties, featureServiceAttributes, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sldDefinition   DOCUMENT ME!
     * @param  featureService  DOCUMENT ME!
     */
    public void configureDialog(final Reader sldDefinition, final AbstractFeatureService featureService) {
        configureDialog(
            sldDefinition,
            featureService.getName(),
            featureService.getLayerProperties(),
            featureService.getFeatureServiceAttributes(),
            featureService.getQuery());
    }

    /**
     * Configures the dialog based on the delivered AbstractfeatureService.
     *
     * @param  sldDefinition             DOCUMENT ME!
     * @param  layerName                 DOCUMENT ME!
     * @param  layerProperties           DOCUMENT ME!
     * @param  featureServiceAttributes  featureservice to get attributes from
     * @param  query                     DOCUMENT ME!
     */
    public void configureDialog(final Reader sldDefinition,
            final String layerName,
            final LayerProperties layerProperties,
            final Map<String, FeatureServiceAttribute> featureServiceAttributes,
            final Object query) {
        try {
            this.layerName = layerName;
            chkCustomSLD.setSelected(sldDefinition == null);
            tbpTabs.setEnabledAt(tbpTabs.indexOfComponent(panTabFill), chkCustomSLD.isSelected());
            tbpTabs.setEnabledAt(tbpTabs.indexOfComponent(panTabLabeling), chkCustomSLD.isSelected());
            tbpTabs.setEnabledAt(tbpTabs.indexOfComponent(panTabAttrib), chkCustomSLD.isSelected());

            this.setAccepted(false);
            this.setFeatureServiceAttributes(featureServiceAttributes);
            this.setLayerProperties(layerProperties);
            if (logger.isDebugEnabled()) {
                logger.debug("QueryType: " + this.layerProperties.getQueryType()); // NOI18N
            }

            if ((this.layerProperties.getQueryType() != LayerProperties.QUERYTYPE_UNDEFINED) && (query != null)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Layer supports query, adding query dialog");           // NOI18N
                }
                tbpTabs.addTab(
                    "Query Editor",
                    new javax.swing.ImageIcon(
                        getClass().getResource("/de/cismet/cismap/commons/featureservice/res/editor.png")),
                    panTabQuery);                                                        // NOI18N
                setQueryString(query.toString());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Layer does not support query, removing query dialog"); // NOI18N
                }
                tbpTabs.remove(panTabQuery);
            }

            if (sldDefinition != null) {
                // jEditorPane1.setDocument();
                jEditorPane1.setText(IOUtils.toString(sldDefinition));
            }

            this.updateDialog();
            this.updateAttributes();
            this.updatePreview();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    /**
     * Reassigns all variables based on the actual Layer Properties.
     */
    private void updateDialog() {
        panFillColor.setBackground(getStyle().getFillColor());
        panTransColor.setBackground(getStyle().getFillColor());
        chkFill.setSelected(getStyle().isDrawFill());

        panLineColor.setBackground(getStyle().getLineColor());
        chkLine.setSelected(getStyle().isDrawLine());

        chkSync.setSelected(false);
        chkHighlightable.setSelected(getStyle().isHighlightFeature());

        sldLineWidth.setValue(getStyle().getLineWidth());
        setAlpha(getStyle().getAlpha());

        if (!pointSymbolHM.containsKey(getStyle().getPointSymbolFilename())) {
            logger.warn("unkown point symbol: '" + getStyle().getPointSymbolFilename() + "', adding to list"); // NOI18N
            pointSymbolHM.put(getStyle().getPointSymbolFilename(), getStyle().getPointSymbol());
            cbbPointSymbol.setModel(new DefaultComboBoxModel(new Vector<String>(this.pointSymbolHM.keySet())));
        }

        cbbPointSymbol.setSelectedItem(getStyle().getPointSymbolFilename());

        if (getStyle().getPointSymbolFilename().equals(getStyle().AUTO_POINTSYMBOL)) {
            setPointSymbolSizeActivated(true);
            sldPointSymbolSize.setValue(getStyle().getPointSymbolSize());
        } else {
            setPointSymbolSizeActivated(false);
            sldPointSymbolSize.setValue(getStyle().MIN_POINTSYMBOLSIZE);
        }

        chkActivateLabels.setSelected(getStyle().isDrawLabel());
        setFontType(getStyle().getFont());
        panFontColor.setBackground(getStyle().getFontColor());

        setAlignment(getStyle().getAlignment());
        txtMin.setText("" + getStyle().getMinScale()); // NOI18N
        txtMax.setText("" + getStyle().getMaxScale()); // NOI18N
        setMultiplier(getStyle().getMultiplier());
        chkAutoscale.setSelected(getStyle().isAutoscale());
    }

    /**
     * DOCUMENT ME!
     */
    private void updateAttributes() {
        // set Attributes ..........................................................

        cbbAnnotationExpression.removeAllItems();
        cbbIdExpression.removeAllItems();
        panAttribGeo.removeAll();
        panAttribNorm.removeAll();
        btgGeom = new ButtonGroup();

        // the stored expression may null or no feature service attribute
        this.ignoreSelectionEvent = true;

        if (this.layerProperties.isIdExpressionEnabled()) {
            final String idExpression = this.layerProperties.getIdExpression();

            if ((idExpression == null)
                        || ((this.layerProperties.getIdExpressionType()
                                == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME)
                            && !this.featureServiceAttributes.containsKey(idExpression))) {
                boolean expressionSet = false;

                for (final FeatureServiceAttribute fsa : this.featureServiceAttributes.values()) {
                    if (!fsa.isGeometry() && fsa.isSelected()) {
                        this.layerProperties.setIdExpression(fsa.getName(),
                            LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                        logger.warn("idExpression is null or not in attriute list, setting to '" + fsa.getName()
                                    + "' (" + fsa.getType() + ", EXPRESSIONTYPE_PROPERTYNAME)"); // NOI18N
                        expressionSet = true;

                        break;
                    }
                }

                // no selected attributes?!
                if (!expressionSet) {
                    for (final FeatureServiceAttribute fsa : this.featureServiceAttributes.values()) {
                        if (!fsa.isGeometry()) {
                            fsa.setSelected(true);
                            this.layerProperties.setIdExpression(fsa.getName(),
                                LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                            logger.warn("idExpression is null or not in attriute list, setting to '" + fsa.getName()
                                        + "' (EXPRESSIONTYPE_PROPERTYNAME) and forcing attribute enabled"); // NOI18N
                            expressionSet = true;

                            break;
                        }
                    }
                }

                if (!expressionSet) {
                    logger.error("no valid id expression could be dertimed from the list of available attributes"); // NOI18N
                }
            } else if (this.layerProperties.getIdExpressionType() == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME) {
                if (!this.featureServiceAttributes.get(idExpression).isSelected()) {
                    logger.warn("idExpression '" + idExpression + "' is not selected, forcing selected");           // NOI18N
                    this.featureServiceAttributes.get(idExpression).setSelected(true);
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("the selected layer does not support id expressions");                                 // NOI18N
            }
            this.cbbIdExpression.setEnabled(false);
        }

        final String annotationExpression = this.layerProperties.getPrimaryAnnotationExpression();

        if ((annotationExpression == null)
                    || ((this.layerProperties.getPrimaryAnnotationExpressionType()
                            == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME)
                        && !this.featureServiceAttributes.containsKey(annotationExpression))) {
            boolean expressionSet = false;

            for (final FeatureServiceAttribute fsa : this.featureServiceAttributes.values()) {
                if (!fsa.isGeometry() && fsa.isSelected()) {
                    this.layerProperties.setPrimaryAnnotationExpression(fsa.getName(),
                        LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                    logger.warn("annotationExpressionExpression is null or not in attriute list, setting to '"
                                + fsa.getName() + "' (EXPRESSIONTYPE_PROPERTYNAME)"); // NOI18N
                    expressionSet = true;

                    break;
                }
            }

            // no selected attributes?!
            if (!expressionSet) {
                for (final FeatureServiceAttribute fsa : this.featureServiceAttributes.values()) {
                    if (!fsa.isGeometry()) {
                        fsa.setSelected(true);
                        this.layerProperties.setPrimaryAnnotationExpression(fsa.getName(),
                            LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                        logger.warn("annotationExpressionExpression is null or not in attriute list, setting to '"
                                    + fsa.getName() + "' (EXPRESSIONTYPE_PROPERTYNAME) and forcing attribute enabled"); // NOI18N
                        expressionSet = true;

                        break;
                    }
                }
            }

            if (!expressionSet) {
                logger.error(
                    "no valid annotationExpression expression could be determined from the list of available attributes"); // NOI18N
            }
        } else if (this.layerProperties.getPrimaryAnnotationExpressionType()
                    == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME) {
            if (!this.featureServiceAttributes.get(annotationExpression).isSelected()) {
                logger.warn("annotationExpression '" + annotationExpression + "' is not selected, forcing selected");      // NOI18N
                this.featureServiceAttributes.get(annotationExpression).setSelected(true);
            }
        }

        // initialise the combo boxes and check boxes
        for (final FeatureServiceAttribute fsa : this.featureServiceAttributes.values()) {
            if (!fsa.isGeometry()) {
                this.createNormalAttributeButton(fsa);
            } else {
                this.createGeoAttributeButton(fsa);
            }
        }

        if (this.layerProperties.isIdExpressionEnabled()) {
            cbbIdExpression.setSelectedItem(this.layerProperties.getIdExpression());
        }

        cbbAnnotationExpression.setSelectedItem(this.layerProperties.getPrimaryAnnotationExpression());
        this.ignoreSelectionEvent = false;

        // check if a geo attribute is selected
        if ((this.btgGeom.getSelection() == null) && (this.btgGeom.getButtonCount() > 0)) {
            this.btgGeom.setSelected(this.btgGeom.getElements().nextElement().getModel(), true);
            logger.warn("no geo attribute selected, forcing selection of attribute '"
                        + this.btgGeom.getSelection().getActionCommand() + "'"); // NOI18N
        }
    }

    /**
     * Reassigns all variables based on the delivered feature.
     *
     * @param  attributes  f feature to define the current style of the dialog
     */
// private void setFeature(CloneableFeature f) { logger.debug("Setzte StyleFeature im StyleDialog"); if (f != null && f
// != feature) { try { this.feature = f; if (f instanceof StyledFeature) { isStyleFeature = true;
//
// color im BasicStyle Objekt nicht auf null setzen sondern nur paintFill/Line auf false Paint fillColor = ((StyledFeature)
// f).getFillingPaint(); setFillColor(fillColor != null, fillColor != null ? (Color) ((StyledFeature)
// f).getFillingPaint() : getStyle().getFillColor());
//
// Paint lineColor = ((StyledFeature) f).getLinePaint(); setLineColor(lineColor != null, lineColor != null ? (Color)
// lineColor : getStyle().getLineColor());
//
// setLineWidth(((StyledFeature) f).getLineWidth());
//
// FeatureAnnotationSymbol s = ((StyledFeature) f).getPointAnnotationSymbol(); if (pointSymbolHM.containsValue(s)) { for
// (String key : pointSymbolHM.keySet()) { if (pointSymbolHM.get(key) == s) { setPointSymbol(key);
// cbbPointSymbol.setSelectedItem(key); break; } } } else { setPointSymbol(getStyle().NO_POINTSYMBOL);
// cbbPointSymbol.setSelectedItem(getStyle().NO_POINTSYMBOL); pointSymbol = s; }
//
// setAlpha(((StyledFeature) f).getTransparency()); setHighlighting(((StyledFeature) f).isHighlightingEnabled()); }
//
// if (f instanceof AnnotatedFeature) { isAnnotatedFeature = true; setLabelingEnabled(((AnnotatedFeature)
// f).isPrimaryAnnotationVisible()); setMaxScale(((AnnotatedFeature) f).getMaxScaleDenominator());
// setMinScale(((AnnotatedFeature) f).getMinScaleDenominator()); setLabelAttribute(((AnnotatedFeature)
// f).getPrimaryAnnotation()); setFontType(((AnnotatedFeature) f).getPrimaryAnnotationFont()); setFontColor((Color)
// ((AnnotatedFeature) f).getPrimaryAnnotationPaint()); setAlignment(((AnnotatedFeature)
// f).getPrimaryAnnotationJustification()); setAutoscale(((AnnotatedFeature) f).isAutoscale());
// setMultiplier(((AnnotatedFeature) f).getPrimaryAnnotationScaling()); }
//
// if (f instanceof FeatureWithId) { isIdFeature = true; setIdExpression(((FeatureWithId) f).getIdExpression()); } } catch
// (Exception ex) { logger.error("Fehler beim Setzen des StyleFeatures", ex); }
//
// updatePreview(); } }
    /**
     * Creates components depending on the attributetype and adds them to the attributes-tab.
     *
     * @param  attributes  list with FeatureServiceAttribute
     */
    private void setAttributes(final List<FeatureServiceAttribute> attributes) {
    }

// /**
// * Resets the attributevariables.
// */
// private void resetAttributeVariables()
// {
// logger.debug("resetAttributeVariables");
// //styleAttribHM.put(ATTRI_GEOM, new Vector<Component>());
// //styleAttribHM.put(ATTRI_NORM, new Vector<Component>());
// //styleAttribHM.put(ATTRI_NORM_SELECTED, new Vector<String>());
//
// cbbAttribute.removeAllItems();
// cbbPrimary.removeAllItems();
// panAttribGeo.removeAll();
// panAttribNorm.removeAll();
// btgGeom = new ButtonGroup();
// }
    /**
     * Is called if setFeatureServiceAttributes() found a attribute with a geometrytype. Creates a new RadioButton and
     * adds it to the attribute-panel.
     *
     * @param  fsa  FeatureServiceAttribute
     */
    private void createGeoAttributeButton(final FeatureServiceAttribute fsa) {
        if (logger.isDebugEnabled()) {
            logger.debug("Geo-Attribut \"" + fsa.getName() + "\" adden"); // NOI18N
        }

        // delete attribute from the "normal" attribute-list
        // ((Vector<FeatureServiceAttribute>) styleAttribHM.get(ATTRI_NORM_SELECTED)).remove(fsa);
        final JRadioButton rb = new JRadioButton(fsa.getName());
        rb.setActionCommand(fsa.getName());

        btgGeom.add(rb);

        // select/deselect the RadioButton
        rb.setSelected(fsa.isSelected());
        panAttribGeo.add(rb);

        // create ItemStateListener that keeps the HashMap up-to-date
        rb.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(final ItemEvent e) {
                    if (!ignoreSelectionEvent && (e.getStateChange() == ItemEvent.SELECTED)) {
                        // setSelectedGeoAttribute(fsa.getName());
                        fsa.setSelected(true);
                    } else {
                        fsa.setSelected(false);
                    }
                }
            });
    }

    /**
     * Is called if setFeatureServiceAttributes() found a attribute that is no geometry. Creates a new CheckBox and adds
     * it to the attribute-panel.
     *
     * @param  fsa  FeatureServiceAttribute
     */
    private void createNormalAttributeButton(final FeatureServiceAttribute fsa) {
        if (logger.isDebugEnabled()) {
            logger.debug("Attribut \"" + fsa.getName() + "\" adden"); // NOI18N
        }

        final JCheckBox cb = new JCheckBox(fsa.getName(), false);
        cb.setActionCommand(fsa.getName());

        // select/deselect the CheckBox -> item state change
        cb.setSelected(fsa.isSelected());

        if (fsa.isSelected()) {
            cbbAnnotationExpression.addItem(fsa.getName());
            cbbIdExpression.addItem(fsa.getName());
        }

        cb.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(final ItemEvent e) {
                    if (!ignoreSelectionEvent && (e.getStateChange() == ItemEvent.SELECTED)) {
                        // sel.add(fsa.getName());
                        fsa.setSelected(true);
                        cbbAnnotationExpression.addItem(fsa.getName());
                        cbbIdExpression.addItem(fsa.getName());
                    } else if (!ignoreSelectionEvent && (e.getStateChange() == ItemEvent.DESELECTED)) {
                        // sel.remove(fsa.getName());
                        fsa.setSelected(false);
                        cbbAnnotationExpression.removeItem(fsa.getName());
                        cbbIdExpression.removeItem(fsa.getName());
                    }
                }
            });

        panAttribNorm.add(cb);
    }

    /**
     * Returns a Vector with all selected attributes (including the geometry-attribute).
     *
     * @return  a Vector with all selected attributes (including the geometry-attribute).
     */
    public Vector<String> getSelectedAttributes() {
        final Vector<String> selectedAttributes = new Vector(this.featureServiceAttributes.size());

        for (final FeatureServiceAttribute fsa : this.featureServiceAttributes.values()) {
            if (fsa.isSelected()) {
                selectedAttributes.add(fsa.getName());
            }
        }

        return selectedAttributes;
    }

    /**
     * Returns the selected geometry-attribute.
     *
     * @return  the selected geometry-attribute.
     */
    public String getSelectedGeoAttribute() {
        if (!this.isAccepted()) {
            logger.warn("supicious call to 'getQueryString()', changes not accepted"); // NOI18N
        }

        return this.btgGeom.getSelection().getActionCommand();
    }

    /**
     * Changes the fillingcolor.
     *
     * @param  paint      DOCUMENT ME!
     * @param  fillColor  the new fillingcolor
     */
    private void setFillColor(final boolean paint, final Color fillColor) {
        getStyle().setDrawFill(paint);
        getStyle().setFillColor(fillColor);
        panFillColor.setBackground(fillColor);
        panTransColor.setBackground(fillColor);
        chkFill.setSelected(paint);
    }

    /**
     * Changes the linecolor.
     *
     * @param  paint      DOCUMENT ME!
     * @param  lineColor  the new linecolor
     */
    private void setLineColor(final boolean paint, final Color lineColor) {
        getStyle().setDrawLine(paint);
        getStyle().setLineColor(lineColor);
        panLineColor.setBackground(lineColor);
        chkLine.setSelected(paint);
    }

    /**
     * Changes the linewidth.
     *
     * @param  lineWidth  the new linewidth
     */
    private void setLineWidth(final int lineWidth) {
        getStyle().setLineWidth(lineWidth);

        if (sldLineWidth.getValue() != lineWidth) {
            sldLineWidth.setValue(lineWidth);
        }

        txtLineWidth.setText("" + lineWidth); // NOI18N
    }

    /**
     * Changes the transparency.
     *
     * @param  alpha  the new transparency
     */
    private void setAlpha(final float alpha) {
        getStyle().setAlpha(alpha);

        final int a = Math.round(alpha * 100);

        if (sldAlpha.getValue() != a) {
            sldAlpha.setValue(a);
        }

        txtTransparency.setText("" + a); // NOI18N
    }

    /**
     * Changes the pointsymbol.
     *
     * @param  pointSymbol  name of the new pointsymbol
     */
    private void setPointSymbol(final String pointSymbol) {
        setPointSymbolSizeActivated(pointSymbol.equals(getStyle().AUTO_POINTSYMBOL));
        getStyle().setPointSymbolFilename(pointSymbol);
    }

    /**
     * Returns the FeatureAnnotationSymbol.
     *
     * @param  size  DOCUMENT ME!
     */
// private FeatureAnnotationSymbol getPointSymbol()
// {
// return pointSymbolHM.get(getStyle().getPointSymbolFilename());
// }
    /**
     * Changes the size of the pointsymbol (if "no pointsymbol" is selected).
     *
     * @param  size  size of the new pointsymbol
     */
    private void setPointSymbolSize(final int size) {
        getStyle().setPointSymbolSize(size);
        txtPointSymbolSize.setText(size + ""); // NOI18N
    }

    /**
     * Enables or disables the components to manipulate the size of the pointsymbol.
     *
     * @param  flag  true to enable, false to disable
     */
    private void setPointSymbolSizeActivated(final boolean flag) {
        lblPointSymbolSize.setEnabled(flag);
        sldPointSymbolSize.setEnabled(flag);
        txtPointSymbolSize.setEnabled(flag);
    }

    /**
     * Changes whether the features should hightlight at mouseover or not.
     *
     * @param  flag  true if there should be a rollover-effect
     */
    private void setHighlighting(final boolean flag) {
        getStyle().setHighlightFeature(flag);

        if (chkHighlightable.isSelected() != flag) {
            chkHighlightable.setSelected(flag);
        }
    }

    /**
     * Enables/disables the featurelabels.
     *
     * @param  flag  enable true if labels should be shown, else false
     */
    private void setLabelingEnabled(final boolean flag) {
        getStyle().setDrawLabel(flag);

        if (flag != chkActivateLabels.isSelected()) {
            chkActivateLabels.setSelected(flag);
        }
    }

    /**
     * Changes the maximum scale at which the labels still are visible.
     *
     * @param  max  maximum scale
     */
    private void setMaxScale(final int max) {
        getStyle().setMaxScale(max);

        if (!txtMax.getText().equals(max + "")) // NOI18N
        {
            txtMax.setText(max + "");           // NOI18N
        }
    }

    /**
     * Changes the minimum scale at which the labels still are visible.
     *
     * @param  min  max minimum scale
     */
    private void setMinScale(final int min) {
        getStyle().setMinScale(min);

        if (!txtMin.getText().equals(min + "")) // NOI18N
        {
            txtMin.setText(min + "");           // NOI18N
        }
    }

    /**
     * Changes the horizontal alignment of the label.
     *
     * @param  align  float with the new alignment
     */
    private void setAlignment(final float align) {
        getStyle().setAlignment(align);

        if (align == JLabel.LEFT_ALIGNMENT) {
            radLeft.setSelected(true);
        } else if (align == JLabel.CENTER_ALIGNMENT) {
            radCenter.setSelected(true);
        } else {
            radRight.setSelected(true);
        }
    }

    /**
     * Changes the scaling-multiplier of the labels. Only effective if autoscale is off.
     *
     * @param  multi  the new multiplier
     */
    private void setMultiplier(final Object multi) {
        if (multi instanceof Double) {
            getStyle().setMultiplier((Double)multi);
        } else if (multi instanceof Long) {
            getStyle().setMultiplier(new Double((Long)multi));
        }

        txtMultiplier.setValue(multi);
    }

    /**
     * Returns the scaling-multiplier of the labels.
     *
     * @return  the scaling-multiplier of the labels.
     */
    private double getMultiplier() {
        final Object o = getStyle().getMultiplier();

        if (o instanceof Double) {
            return (Double)o;
        } else if (o instanceof Long) {
            final long l = (Long)o;
            final double d = l;

            return d;
        } else {
            return 1.0d;
        }
    }

    /**
     * Changes the fontcolor of the labels.
     *
     * @param  fontColor  neue Schriftfarbe
     */
    private void setFontColor(final Color fontColor) {
        getStyle().setFontColor(fontColor);
        panFontColor.setBackground(fontColor);
    }

    /**
     * Changes the font of the labels.
     *
     * @param  fontType  the new font
     */
    private void setFontType(final Font fontType) {
        getStyle().setFont(fontType);

        final StringBuffer name = new StringBuffer(fontType.getSize() + "pt "); // NOI18N
        name.append(fontType.getName());

        if (fontType.isBold()) {
            name.append(org.openide.util.NbBundle.getMessage(
                    StyleDialog.class,
                    "StyleDialog.setFontType(Font).name.bold")); // NOI18N
        }

        if (fontType.isItalic()) {
            name.append(org.openide.util.NbBundle.getMessage(
                    StyleDialog.class,
                    "StyleDialog.setFontType(Font).name.italic")); // NOI18N
        }

        lblFontname.setText(name.toString());
    }

    /**
     * Changes whether the label should be scaled automatically with the zoomlevel.
     *
     * @param  flag  true to activate autoscaling
     */
    private void setAutoscale(final boolean flag) {
        getStyle().setAutoscale(flag);

        if (chkAutoscale.isSelected() != flag) {
            chkAutoscale.setSelected(flag);
        }
    }

    /**
     * Changes the primaryattribute.
     *
     * @param  s  id the name of the new primaryattribute
     */
    /* private void setIdExpression(String id)
     * { if (id != null) { //styleAttribHM.put(ATTRI_PRIMARY, id); this.layerProperties.setIdExpression(id,
     * LayerProperties.EXPRESSIONTYPE_PROPERTYNAME); if (cbbPrimary.getSelectedItem() == null ||
     * !cbbPrimary.getSelectedItem().equals(id)) { cbbPrimary.setSelectedItem(id); } }}*/
    /**
     * Changes the label of the features. This String provides Groovy-functionality.
     *
     * @param  s  attrib the new label
     */
    /* private void setLabelAttribute(String attrib)
     * { if (attrib == null) { attrib = ""; } //this.getLayerProperties().setPrimaryAnnotationExpression(attrib,
     * LayerProperties.EXPRESSIONTYPE_PROPERTYNAME); cbbAttribute.setSelectedItem(attrib);}*/
    /**
     * Sets a string as text in the queryEditorpane.
     *
     * @param  s  querystring
     */
    private void setQueryString(final String s) {
        queryEditor.setText(s);
        chkUseQueryString.setSelected(false);
    }

    /**
     * Returns the querystring of the queryEditorpane.
     *
     * @return  the querystring of the queryEditorpane.
     */
    public String getQueryString() {
        if (!this.isAccepted()) {
            logger.warn("supicious call to 'getQueryString()', changes not accepted"); // NOI18N
        }

        return queryEditor.getText();
    }

    /**
     * Returns whether the query string was changed in the queryEditorpane.
     *
     * @return  true if changed, else false
     */
    public boolean isQueryStringChanged() {
        return chkUseQueryString.isSelected();
    }

    /**
     * Get the value of accepted.
     *
     * @return  the value of accepted
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Set the value of accepted.
     *
     * @param  accepted  new value of accepted
     */
    private void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * Overrides the setVisible()-method from JDialog. Updates the UI of the historylist if the StyleDialog is shown and
     * selects the "color and filling"-tab.
     *
     * @param  visible  b true to show, false to hide
     */
    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            lstHistory.updateUI();
            if (chkCustomSLD.isSelected()) {
                tbpTabs.setSelectedComponent(panTabFill);
            } else {
                tbpTabs.setSelectedComponent(panSLDDefinition);
            }
        }

        super.setVisible(visible);
    }
}
