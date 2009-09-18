/*
 * StyleDia//log.java
 *
 * Created on 25. Februar 2008, 10:38
 */
package de.cismet.cismap.commons.featureservice.style;

import de.cismet.cismap.commons.RestrictedFileSystemView;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import de.cismet.cismap.commons.features.AnnotatedFeature;
import de.cismet.cismap.commons.features.CloneableFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.featureservice.WFSOperator;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import org.bounce.text.LineNumberMargin;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
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
import org.bounce.text.ScrollableEditorPanel;
import org.bounce.text.xml.XMLDocument;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * A dialo.
 * @author  nh
 */
public class StyleDialog extends JDialog implements ListSelectionListener {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.featureservice.style.StyleDialog");
    // Hashmap-Keys
    
    private static final String ATTRI_NORM = "AttributeNormal";
    private static final String ATTRI_GEOM = "AttributeGeometry";
    private static final String ATTRI_NORM_SELECTED = "AttributeNormalSelected";
    private static final String ATTRI_GEOM_SELECTED = "AttributeGeometrySelected";
    private static final String ATTRI_PRIMARY = "AttributePrimary";
    
    // Lokalisierungskonstanten
    private static final String CISMAP_FOLDER = ".cismap";
    private static final String DEFAULT_HISTORY_NAME = "defaultStyleHistory.xml";
    private static final String COLORCHOOSER_TITLE = "Farbe w\u00E4hlen";
    private static final String FONTCHOOSER_TITLE = "Schriftart w\u00E4hlen";
    private static final String POINTSYMBOL_FOLDER = "/de/cismet/cismap/commons/featureservice/res/pointsymbols/";
    private final String home = System.getProperty("user.home");
    private final String seperator = System.getProperty("file.separator");
    private final File fileToCismapFolder = new File(home + seperator + CISMAP_FOLDER);
    
    // Popup-Konstanten
    private final static String POPUP_SAVE = "speichern";
    private final static String POPUP_LOAD = "laden";
    private final static String POPUP_CLEAR = "l\u00F6schen";
    
    private BasicStyle style;
    private Vector pointSymbolList = new Vector();
    private HashMap<String, FeatureAnnotationSymbol> pointSymbolHM = new HashMap<String, FeatureAnnotationSymbol>();
    private FeatureAnnotationSymbol fas = null;
    private CloneableFeature feature = null;
    private HashMap<String, Object> styleAttribHM = new HashMap<String, Object>();
    private Frame parent;
    private File defaultHistory;
    private JColorChooser colorChooser;
    private FontChooserDialog fontChooser;
    private JPopupMenu popupMenu;
    private SAXBuilder builder = new SAXBuilder();
    private JEditorPane queryEditor = new JEditorPane();
    private boolean isStyleFeature = false;
    private boolean isAnnotatedFeature = false;
    private boolean isIdFeature = false;

    /**
     * Konstruktor, der StyleDialog-Objekte anlegt.
     * @param parent Vater-Frame des Dialogs
     * @param modal true, wenn der Dialog Eingaben auf dem Vater-Frame blockieren soll
     */
    public StyleDialog(Frame parent, boolean modal) {
        super(parent, modal);
        log.info("Erstelle StyleDialog");
        this.parent = parent;
        createPointSymbols(); // FeatureAnnotationSymbole erstellen
        initComponents();     // Komponenten erstellen
        createXMLEditor();    // XML-Editor
        panTabQuery.setVisible(false);
        setLocationRelativeTo(parent); // Über Parent-Frame zentrieren
        style = StyleFactory.createDefaultStyle();
        
        colorChooser = new JColorChooser();
        fontChooser = new FontChooserDialog(this, FONTCHOOSER_TITLE);

        // HistoryList initialieren und Parameter setzen
        createHistoryListPopupMenu();
        lstHistory.setCellRenderer(new StyleHistoryListCellRenderer());
        lstHistory.addListSelectionListener(this);
        lstHistory.addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && !popupMenu.isVisible()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && !popupMenu.isVisible()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        // Versuche Standard-History zu laden
        searchDefaultHistory();
        loadHistory(defaultHistory);

        // Listener für Editorpane erstellen
        queryEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                chkUseQueryString.setSelected(true);
                log.debug(e.getChange(e.getDocument().getDefaultRootElement()));
            }

            public void removeUpdate(DocumentEvent e) {
                chkUseQueryString.setSelected(true);
            }

            public void changedUpdate(DocumentEvent e) {
                chkUseQueryString.setSelected(true);
            }
        });

        // noch nicht implementierte Funktionen nicht zeigen
        // TODO Pattern unterstützen !!!
        chkFillPattern.setVisible(false);
        cbbFillPattern.setVisible(false);
        chkLinePattern.setVisible(false);
        cbbLinePattern.setVisible(false);

        updatePreview();
    }

    // <editor-fold defaultstate="collapsed" desc="Returnwert-Bestimmung">
    
    /**
     * Liefert das veränderte CloneableFeature mit dem aktuell eingestellten 
     * Style für den WFS.
     * @return eingestellter Style als CloneableFeature
     */
    public CloneableFeature getReturnStatus() {
        return feature;
    }

    /**
     * Schließt den Dialog und setzt dabei den Rückgabewert.
     * @param retStatus Rückgabewert
     */
    private void doClose(CloneableFeature retStatus) {
        tbpTabs.setSelectedComponent(panTabFill);
        feature = retStatus;
        lstHistory.setSelectedIndex(-1);
        setVisible(false);
        dispose();
    }
// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Anzeige-Methoden">
    
    /**
     * 
     */
    private void createXMLEditor() {
        try {
            XMLEditorKit kit = new XMLEditorKit(true);
            kit.setWrapStyleWord(true);
            kit.setLineWrappingEnabled(chkLinewrap.isSelected());

            queryEditor.setEditorKit(kit);
            queryEditor.setFont(new Font("Monospace", Font.PLAIN, 12));
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
            kit.setStyle(XMLStyleConstants.NAMESPACE_NAME,  Color.GREEN.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.NAMESPACE_VALUE, Color.MAGENTA.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.NAMESPACE_PREFIX, Color.GREEN.darker(), Font.PLAIN);
            kit.setStyle(XMLStyleConstants.SPECIAL, Color.BLACK, Font.PLAIN);

            // ScrollableEditorPanel forces the queryEditor to resize
            ScrollableEditorPanel editorPanel = new ScrollableEditorPanel(queryEditor);

            scpQuery.setViewportView(editorPanel);

            // Add the number margin as a Row Header View
            scpQuery.setRowHeaderView(new LineNumberMargin(queryEditor));
        } catch (Exception ex) {
            log.error("Fehler beim Erstellen des QueryEditors", ex);
        }
    }
    
    /**
     * FeatureAnnotationSymbole erstellen und in Vector "pointSymbolList" speichern.
     * Dieser wird in initComponents() für das ComboboxModel verwendet.
     */
    private void createPointSymbols() {
        pointSymbolList.addElement(Style.NO_POINTSYMBOL);
        pointSymbolHM.put(Style.NO_POINTSYMBOL, null);

        FeatureAnnotationSymbol pushPin = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "pushpin.png")).getImage());
        pushPin.setSweetSpotX(0.14d);
        pushPin.setSweetSpotY(1.0d);
        pointSymbolList.addElement("pushpin.png");
        pointSymbolHM.put("pushpin.png", pushPin);

        FeatureAnnotationSymbol arrowBlue = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "arrow-blue-down.png")).getImage());
        arrowBlue.setSweetSpotX(0.5d);
        arrowBlue.setSweetSpotY(1.0d);
        pointSymbolList.addElement("arrow-blue-down.png");
        pointSymbolHM.put("arrow-blue-down.png", arrowBlue);

        FeatureAnnotationSymbol arrowGreen = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "arrow-green-down.png")).getImage());
        arrowGreen.setSweetSpotX(0.5d);
        arrowGreen.setSweetSpotY(1.0d);
        pointSymbolList.addElement("arrow-green-down.png");
        pointSymbolHM.put("arrow-green-down.png", arrowGreen);

        FeatureAnnotationSymbol flagBlack = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "flag-black.png")).getImage());
        flagBlack.setSweetSpotX(0.18d);
        flagBlack.setSweetSpotY(0.96d);
        pointSymbolList.addElement("flag-black.png");
        pointSymbolHM.put("flag-black.png", flagBlack);

        FeatureAnnotationSymbol flagBlue = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "flag-blue.png")).getImage());
        flagBlue.setSweetSpotX(0.18d);
        flagBlue.setSweetSpotY(0.96d);
        pointSymbolList.addElement("flag-blue.png");
        pointSymbolHM.put("flag-blue.png", flagBlue);

        FeatureAnnotationSymbol flagGreen = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "flag-green.png")).getImage());
        flagGreen.setSweetSpotX(0.18d);
        flagGreen.setSweetSpotY(0.96d);
        pointSymbolList.addElement("flag-green.png");
        pointSymbolHM.put("flag-green.png", flagGreen);

        FeatureAnnotationSymbol flagRed = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "flag-red.png")).getImage());
        flagRed.setSweetSpotX(0.18d);
        flagRed.setSweetSpotY(0.96d);
        pointSymbolList.addElement("flag-red.png");
        pointSymbolHM.put("flag-red.png", flagRed);

        FeatureAnnotationSymbol flagYellow = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "flag-yellow.png")).getImage());
        flagYellow.setSweetSpotX(0.18d);
        flagYellow.setSweetSpotY(0.96d);
        pointSymbolList.addElement("flag-yellow.png");
        pointSymbolHM.put("flag-yellow.png", flagYellow);

        FeatureAnnotationSymbol starBlack = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "star-black.png")).getImage());
        starBlack.setSweetSpotX(0.5d);
        starBlack.setSweetSpotY(0.5d);
        pointSymbolList.addElement("star-black.png");
        pointSymbolHM.put("star-black.png", starBlack);

        FeatureAnnotationSymbol starYellow = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "star-yellow.png")).getImage());
        starYellow.setSweetSpotX(0.5d);
        starYellow.setSweetSpotY(0.5d);
        pointSymbolList.addElement("star-yellow.png");
        pointSymbolHM.put("star-yellow.png", starYellow);

        FeatureAnnotationSymbol infoButton = new FeatureAnnotationSymbol(new ImageIcon(getClass().getResource(POINTSYMBOL_FOLDER + "info.png")).getImage());
        infoButton.setSweetSpotX(0.5d);
        infoButton.setSweetSpotY(0.5d);
        pointSymbolList.addElement("info.png");
        pointSymbolHM.put("info.png", infoButton);
    }
    
    /**
     * Ruft update() des StylePreviewPanels auf und stößt dessen repaint() an.
     */
    private void updatePreview() {
//        log.debug("updatePreview");
        ((StylePreviewPanel) panPreview).update(chkLine.isSelected(),
                chkFill.isSelected(),
                getLabelingEnabled(),
                getPointSymbol(),
                getPointSymbolSize(),
                getLineWidth(),
                getAlpha(),
                getLineColor(),
                getFillColor(), 
                getFontType(),
                getFontColor());
        if (cbbPointSymbol.getSelectedItem().equals(Style.NO_POINTSYMBOL) 
                && !sldLineWidth.getValueIsAdjusting() 
                && !sldPointSymbolSize.getValueIsAdjusting()) {
            if (fas == null) {
                fas = new FeatureAnnotationSymbol(((StylePreviewPanel) panPreview).getPointSymbol());
                fas.setSweetSpotX(0.5d);
                fas.setSweetSpotY(0.5d);
            } else {
                fas.setImage(((StylePreviewPanel) panPreview).getPointSymbol());
            }
        }
    }

    /**
     * Dunkelt die Übergebene Farbe auf 60% ab.
     * @param c abzudunkelnde Farbe
     * @return abgedunkelte Farbe
     */
    public static Color darken(Color c) {
        int r = new Double(Math.floor(c.getRed() * 0.6d)).intValue();
        int g = new Double(Math.floor(c.getGreen() * 0.6d)).intValue();
        int b = new Double(Math.floor(c.getBlue() * 0.6d)).intValue();
        return new Color(r, g, b);
    }

    /**
     * Hellt die Übergebene Farbe um ca. 66% auf;
     * @param c aufzuhellende Farbe
     * @return aufgehellte Farbe
     */
    public static Color lighten(Color c) {
        int r = new Double(Math.floor(c.getRed() / 0.6d)).intValue();
        if (r > 255) {
            r = 255;
        }
        int g = new Double(Math.floor(c.getGreen() / 0.6d)).intValue();
        if (g > 255) {
            g = 255;
        }
        int b = new Double(Math.floor(c.getBlue() / 0.6d)).intValue();
        if (b > 255) {
            b = 255;
        }
        return new Color(r, g, b);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="History-Methoden">
    
    /**
     * Sucht im Standardpfad nach der defaultStyleHistory.xml-Datei. Falls diese nicht
     * vorhanden ist, wird sie (falls möglich) angelegt und als default angesehen.
     * setzt die Variable defaultHistory auf die gefundene History oder null im Fehlerfall.
     */
    private void searchDefaultHistory() {
        log.debug("Suche nach " + DEFAULT_HISTORY_NAME);
        if (fileToCismapFolder.exists() && fileToCismapFolder.isDirectory()) { // .cismap existiert
            // testen, ob defaultStyleHistory.xml existiert
            File test = new File(fileToCismapFolder.getPath() + seperator + DEFAULT_HISTORY_NAME);
            if (test.exists() && test.isFile() && test.canRead() && test.canWrite()) {
                log.debug(DEFAULT_HISTORY_NAME + " gefunden");
                defaultHistory = test;
            } else {
                createDefaultHistory();
            }
        } else { // .cismap-Ordner existiert nicht, somit auch keine History
            fileToCismapFolder.mkdir();
            createDefaultHistory();
        }
    }

    /**
     * Versucht eine neue defaultStyleHistory.xml in /user/.cismap/ zu erstellen.
     * @return true, nur wenn History erfolgreich erstellt wurde, false sonst
     */
    private boolean createDefaultHistory() {
        FileWriter writer = null;
        try {
            // defaultStyleHistory.xml anlegen
            defaultHistory = new File(fileToCismapFolder.getPath() + seperator + DEFAULT_HISTORY_NAME);
            defaultHistory.createNewFile();
            log.debug(DEFAULT_HISTORY_NAME + " erfolgreich angelegt");
            return true;
        } catch (IOException ex) {
            log.error(DEFAULT_HISTORY_NAME + " konnte nicht erstellt werden", ex);
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception skip) {}
        }
    }

    /**
     * Schreibt die aktuelle History in eine XML-Datei.
     * @param f Zieldatei
     * @param onClose true, wenn der aktuelle Style zusätzlich geschrieben werden soll, 
     * nur beim Schließen des Dialogs notwendig, sonst sollte false verwendet werden
     */
    private void writeHistory(final File f, final boolean onClose) {
        log.debug("writeHistory(" + f + ")");
        Runnable write = new Runnable() {
            public void run() {
                FileWriter writer = null;
                try {
                    f.createNewFile();
                    if (f.canWrite()) {
                        // Element nur erstellen wenn der Dialog geschlossen wird, kein doppeltes Speichern
                        if (onClose) {
                            BasicStyle s = new BasicStyle(chkFill.isSelected(), getFillColor(),
                                    chkLine.isSelected(), getLineColor(), getLineWidth(), chkHighlightable.isSelected(),
                                    getAlpha(), (String) cbbPointSymbol.getSelectedItem(), getPointSymbolSize(),
                                    getLabelingEnabled(), getFont(), getFontColor(), getLabelAttribute(), getAlignment(),
                                    getMinScale(), getMaxScale(), getMultiplier(), getAutoscale());
                            ((StyleHistoryListModel)lstHistory.getModel()).addStyle(s);
                        }
                        
                        XMLOutputter out = new XMLOutputter("\t", true);
                        out.setTextTrim(true);
                        Document doc = new Document(((StyleHistoryListModel)lstHistory.getModel()).getElement());
                        writer = new FileWriter(f);
                        out.output(doc, writer);
                        
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                lblHistory.setText(f.getName());
                            }
                        });
                    }
                } catch (Exception ex) {
                    log.error("Fehler beim Schreiben der History", ex);
                    JOptionPane.showMessageDialog(StyleDialog.this,
                            "<html>Fehler beim Schreiben der History<br>" + ex.getMessage() + "</html>",
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try {
                        writer.close();
                    } catch (Exception skip) {}
                }
            }
        };
        CismetThreadPool.execute(write);
    }

    /**
     * Lädt eine History aus einer XML-Datei.
     * @param f Datei als Fileobjekt
     */
    private void loadHistory(final File f) {
        log.debug("loadHistory(" + f + ")");
        Runnable load = new Runnable() {
            public void run() {
                try {
                    final StyleHistoryListModel model = new StyleHistoryListModel(f);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            lstHistory.setModel(model);
                            lblHistory.setText(f.getName());
                            defaultHistory = f;
                            log.debug(f + " erfolgreich geladen");
                        }
                    });
                } catch (Exception ex) {
                    log.error("Fehler beim Laden der History", ex);
                    JOptionPane.showMessageDialog(StyleDialog.this,
                            "<html>Fehler beim Laden der History<br>" + ex.getMessage() + "</html>",
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        CismetThreadPool.execute(load);
    }

    /**
     * Erstellt das PopupMenu zum Laden und Speichern der History
     */
    private void createHistoryListPopupMenu() {
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if ((f.isFile() && f.getName().endsWith(".xml")) || f.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "XML Dateien (*.xml)";
            }
        };

        JMenuItem save = new JMenuItem();
        save.setText(POPUP_SAVE);
        try {
            save.setIcon(new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/save.png")));
        } catch (Exception skipIcon) {}
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
                    int returnValue = fc.showSaveDialog(StyleDialog.this);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File dst = fc.getSelectedFile();
                        if (!dst.getName().endsWith(".xml")) {
                            dst = new File(dst.toString() + ".xml");
                        }
                        writeHistory(dst, false);
                        defaultHistory = dst;
                    }
                } catch (Throwable ex) {
                    log.error("Fehler bei Öffnen-Dialog der StyleHistory", ex);
                }
            }
        });

        JMenuItem open = new JMenuItem();
        open.setText(POPUP_LOAD);
        try {
            open.setIcon(new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/open.png")));
        } catch (Exception skipIcon) {}
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
                    int returnVal = fc.showOpenDialog(StyleDialog.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File fileToLoad = fc.getSelectedFile();
                        loadHistory(fileToLoad);
                    }
                } catch (Throwable ex) {
                    log.error("Fehler bei Öffnen-Dialog der StyleHistory", ex);
                }
            }
        });

        JMenuItem clear = new JMenuItem();
        clear.setText(POPUP_CLEAR);
        try {
            clear.setIcon(new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/delete_history.png")));
        } catch (Exception skipIcon) {}
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StyleHistoryListModel model = (StyleHistoryListModel) lstHistory.getModel();
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
// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Setters & Getters">
    
    /**
     * Setzt neue Werte für die einzelnen Parameter.
     * // TODO Pattern unterstützen
     * // TODO Anzeigeregeln unterstützen
     * @param f Feature, das als Vorlage für die Konfiguration genommen wird
     */
    public void setFeature(CloneableFeature f) {
        log.debug("Setzte StyleFeature im StyleDialog");
        if (f != null && f != feature) {
            try {
                this.feature = f;
                // falls StyledFeature, dann Style anpassen
                if (f instanceof StyledFeature) {
                    isStyleFeature = true;
                    setFillColor(((StyledFeature) f).getFillingPaint()!=null, (Color) ((StyledFeature) f).getFillingPaint());
                    setLineColor(((StyledFeature) f).getLinePaint()!=null, (Color) ((StyledFeature) f).getLinePaint());
                    setLineWidth(((StyledFeature) f).getLineWidth());
                    FeatureAnnotationSymbol s = ((StyledFeature) f).getPointAnnotationSymbol();
                    if (pointSymbolHM.containsValue(s)) {
                        for (String key : pointSymbolHM.keySet()) {
                            if (pointSymbolHM.get(key) == s) {
                                setPointSymbol(key);
                                cbbPointSymbol.setSelectedItem(key);
                                break;
                            }
                        }
                    } else {
                        setPointSymbol(Style.NO_POINTSYMBOL);
                        cbbPointSymbol.setSelectedItem(Style.NO_POINTSYMBOL);
                        fas = s;
                    }
                    setAlpha(((StyledFeature) f).getTransparency());
                    setHighlighting(((StyledFeature) f).isHighlightingEnabled());
                }

                // falls AnnotatedFeature, dann Labeling anpassen
                if (f instanceof AnnotatedFeature) {
                    isAnnotatedFeature = true;
                    setLabelingEnabled(((AnnotatedFeature) f).isPrimaryAnnotationVisible());
                    setMaxScale(((AnnotatedFeature) f).getMaxScaleDenominator());
                    setMinScale(((AnnotatedFeature) f).getMinScaleDenominator());
                    setLabelAttribute(((AnnotatedFeature) f).getPrimaryAnnotation());
                    setFontType(((AnnotatedFeature) f).getPrimaryAnnotationFont());
                    setFontColor((Color) ((AnnotatedFeature) f).getPrimaryAnnotationPaint());
                    setAlignment(((AnnotatedFeature) f).getPrimaryAnnotationJustification());
                    setAutoscale(((AnnotatedFeature) f).isAutoscale());
                    setMultiplier(((AnnotatedFeature) f).getPrimaryAnnotationScaling());
                }

                if (f instanceof FeatureWithId) {
                    isIdFeature = true;
                    setPrimaryIdExpression(((FeatureWithId)f).getIdExpression());
                }
            } catch (Exception ex) {
                log.error("Feahler beim Setzen des StyleFeatures", ex);
            }
            
            updatePreview();
        }
    }

    /**
     * Setzt die Attribute im Attribute-Tab. Ordnet nach Geometrie- und 
     * Nicht-Geometrie-Attributen, wobei Geo-Attribute nur einzeln selektierbar sind.
     * @param attributes Elementliste mit Attributen
     */
    public void setAttributes(List<Element> attributes) {
        log.debug("Setzte Attibute im StyleDialog");
        resetAttributeVariables();
        try {
            for (Element e : attributes) {
                log.debug("Attribut \""+e.getAttributeValue("name")+"\"");
                // falls Geometrie erzeuge RadioButton
                if (e.getAttributeValue("type").equals(WFSOperator.GEO_PROPERTY_TYPE)) {
                    createGeoAttributeButton(e.getAttributeValue("name"));
                } else { // sonst == normales Attribut, erzeuge CheckBox
                    createNormalAttributeButton(e.getAttributeValue("name"));
                }
                cbbPrimary.addItem(e.getAttributeValue("name"));
            }
            cbbAttribute.setSelectedItem(getLabelAttribute());
        } catch (Exception ex) {
            log.error("Fehler beim Hinzufuegen der Attribute", ex);
        }
    }
    
    /**
     * Resettet alle Attributvariablen.
     */
    private void resetAttributeVariables() {
        log.debug("resetAttributeVariables");
        styleAttribHM.put(ATTRI_GEOM, new Vector<Component>());
        styleAttribHM.put(ATTRI_NORM, new Vector<Component>());
        cbbAttribute.removeAllItems();
        cbbPrimary.removeAllItems();
        panAttribGeo.removeAll();
        panAttribNorm.removeAll();
        btgGeom = new ButtonGroup();
    }
    
    /**
     * Wird von setAttributes() aufgerufen, falls ein Geometrieattribut gefunden wurde.
     * Erzeugt einen JRadioButton für dieses Attribut und fügt es in die Attributliste
     * und den Button in den Attribut-Tab ein.
     * @param name Name des Attributs als String
     */
    private void createGeoAttributeButton(String name) {
        log.debug("Geo-Attribut \""+name+"\" adden");
        // Geom-Attribut aus "normaler" Attributeliste entfernen
        ((Vector<String>)styleAttribHM.get(ATTRI_NORM_SELECTED)).remove(name);
        final JRadioButton rb = new JRadioButton(name);
        
        // erstelle ItemStateListener, der die HashMap aktuell hält
        rb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setSelectedGeoAttribute(((JRadioButton)e.getItem()).getText());
                }
            }
        });
        
        // selektiere den Radiobutton
        if (getSelectedGeoAttribute() != null && getSelectedGeoAttribute().equals(name)) {
            rb.setSelected(true);
        }
        
        // RadioButton in Buttongroup und Panel hinzufügen
        btgGeom.add(rb);
        ((Vector<Component>)styleAttribHM.get(ATTRI_GEOM)).add(rb);
        panAttribGeo.add(rb);
    }
    
    /**
     * Wird von setAttributes() aufgerufen, falls ein normales Attribut gefunden wurde.
     * Erzeugt einen JCheckBox für dieses Attribut und fügt es in die Attributliste
     * und die CheckBox in den Attribut-Tab ein.
     * @param name Name des Attributs als String
     */
    private void createNormalAttributeButton(String name) {
        log.debug("Attribut \""+name+"\" adden");
        final JCheckBox cb = new JCheckBox(name, false);
        // teste, ob Attribut in der Selektion vorhanden ist
        for (String s : getSelectedAttributes()) {
            if (s.equals(cb.getText())) { // wenn ja, setze auf selektiert
                cb.setSelected(true);
                cbbAttribute.addItem(cb.getText());
                break;
            }
        }
        
        // erstelle ItemStateListener, der die HashMap aktuell hält
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Vector<String> sel = (Vector<String>)styleAttribHM.get(ATTRI_NORM_SELECTED);
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    sel.add(cb.getText());
                    cbbAttribute.addItem(cb.getText());
                } else {
                    sel.removeElement(cb.getText());
                    cbbAttribute.removeItem(cb.getText());
                }
            }
        });
        panAttribNorm.add(cb);
    }
    
    /**
     * Setzt die Liste der selektierten Attribute neu.
     * @param sel Vector<String> mit zu selektierenden Attributen
     */
    public void setSelectedAttributes (List<String> selected) {
        styleAttribHM.put(ATTRI_NORM_SELECTED, selected);
    }
    
    /**
     * Liefert alle angewählten Attribute (inkl. dem Geometrie-Attribut)
     */
    public Vector<String> getSelectedAttributes() {
        Vector<String> attributes = new Vector<String>();
        try {
            attributes.addAll((Vector<String>)styleAttribHM.get(ATTRI_NORM_SELECTED));
        } catch (Exception empty) {}
        
        if (getSelectedGeoAttribute() != null) {
            attributes.add(getSelectedGeoAttribute());
        }
        return attributes;
    }
    
    /**
     * Setzt die Liste der selektierten Attribute neu.
     * @param sel Vector<String> mit zu selektierenden Attributen
     */
    public void setSelectedGeoAttribute (String geom) {
        styleAttribHM.put(ATTRI_GEOM_SELECTED, geom);
    }
    
    /**
     * Liefert das ausgewählte Geometrie-Attribut.
     */
    public String getSelectedGeoAttribute() {
        return (String) styleAttribHM.get(ATTRI_GEOM_SELECTED);
    }
    
    
    /**
     * Ändert die Füllfarbe und setzt die Checkbox auf (Füllfarbe == null)
     * @param fillColor neue Füllfarbe
     */
    public void setFillColor(boolean paint, Color fillColor) {
        style.setPaintFill(paint);
        style.setFillColor(fillColor);
        panFillColor.setBackground(fillColor);
        panTransColor.setBackground(fillColor);
        if (paint) {
            chkFill.setSelected(true);
        } else {
            chkFill.setSelected(false);
        }
    }

    /**
     * Liefert die Füllfarbe.
     */
    public Color getFillColor() {
        return style.getFillColor();
    }

    /**
     * Ändert die Linienfarbe und setzt die Checkbox auf (LinienFarbe == null)
     * @param lineColor neue Linienfarbe
     */
    public void setLineColor(boolean paint, Color lineColor) {
        style.setPaintLine(paint);
        style.setLineColor(lineColor);
        panLineColor.setBackground(lineColor);
        if (paint) {
            chkLine.setSelected(true);
        } else {
            chkLine.setSelected(false);
        }
    }

    /**
     * Liefert die Linienfarbe.
     */
    public Color getLineColor() {
        return style.getLineColor();
    }

    /**
     * Ändert die Liniendicke und setzt den Text der Textbox den Wert.
     * @param lineWidth neue Liniendicke
     */
    public void setLineWidth(int lineWidth) {
        style.setLineWidth(lineWidth);
        if (sldLineWidth.getValue() != lineWidth) {
            sldLineWidth.setValue(lineWidth);
        }
        txtLineWidth.setText("" + lineWidth);
    }

    /**
     * Liefert die ganzzahlige Liniendicke.
     */
    public int getLineWidth() {
        return style.getLineWidth();
    }

    /**
     * Setzt die Transparenz des Features neu.
     * @param alpha neue Transparenz als float
     */
    public void setAlpha(float alpha) {
        style.setAlpha(alpha);
        int a = Math.round(alpha * 100);
        if (sldAlpha.getValue() != a) {
            sldAlpha.setValue(a);
        }
        txtTransparency.setText("" + a);
    }

    /**
     * Liefert die Transparenz eines Features, wobei 0 = durchsichtig.
     */
    public float getAlpha() {
        return style.getAlpha();
    }

    /**
     * Setzt das PunktSymbol des Features neu.
     * @param pointSymbol das neue FeatureAnnotationSymbol
     */
    public void setPointSymbol(String pointSymbol) {
        if (pointSymbol.equals(Style.NO_POINTSYMBOL)) {
            activatePointSymbolSize();
        } else {
            deactivatePointSymbolSize();
        }
        style.setPointSymbol(pointSymbol);
    }

    /**
     * Liefert das Symbol für Punktgeometrien.
     */
    public FeatureAnnotationSymbol getPointSymbol() {
        return pointSymbolHM.get(style.getPointSymbol());
    }
    
    /**
     * Setzt die Groeße des Punktsymbols (falls kein Icon gewählt).
     * @param size neue Groeße des Punktsymbols
     */
    public void setPointSymbolSize(int size) {
        style.setPointSymbolSize(size);
        txtPointSymbolSize.setText(size+"");
    }
    
    /**
     * Liefert die Größe des Punktgeometrie-Symbols.
     */
    public int getPointSymbolSize() {
        return style.getPointSymbolSize();
    }

    /**
     * Activates the components to manipulate the size of the featureannotationsymbol.
     */
    private void activatePointSymbolSize() {
        setPointSymbolSizeActivated(true);
    }
    
    /**
     * Deactivates the components to manipulate the size of the featureannotationsymbol.
     */
    private void deactivatePointSymbolSize() {
        setPointSymbolSizeActivated(false);
    }
    
    /**
     * Enables or disables the components to manipulate the size of the featureannotationsymbol.
     * @param flag true to enable, false to disable
     */
    private void setPointSymbolSizeActivated(boolean flag) {
        lblPointSymbolSize.setEnabled(flag);
        sldPointSymbolSize.setEnabled(flag);
        txtPointSymbolSize.setEnabled(flag);
    }
    
    /**
     * Setzt ein boolean-Wert, ob die Features einen Hover-Effekt haben.
     * @param flag true, wenn Hover-Effekt aktiv sein soll
     */
    public void setHighlighting(boolean flag) {
        style.setHighlightFeature(flag);
        chkHighlightable.setSelected(flag);
    }

    /**
     * Liefert true, wenn Features unter der Maus hervorgehoben werden, sonst false.
     */
    public boolean getHighlighting() {
        return style.isHighlightFeature();
    }
    
    /**
     * Schaltet die Featurebeschriftung an oder ab.
     * @param enable true, falls Beschriftung angeschaltet werden soll, sonst false
     */
    public void setLabelingEnabled(boolean flag) {
        style.setPaintLabel(flag);
        if (flag != chkActivateLabels.isSelected()) {
            chkActivateLabels.setSelected(flag);
        }
    }
    
    /**
     * Liefert true, wenn die Featurebeschriftung gezeigt wird.
     */
    public boolean getLabelingEnabled() {
        return style.isPaintLabel();
    }
    
    /**
     * Setzt die maximale Skalierung, bei der die Features beschriftet sind.
     * @param max neue max. Skalierung
     */
    public void setMaxScale(int max) {
        style.setMaxScale(max);
        if (!txtMax.getText().equals(max+"")) {
            txtMax.setText(max+"");
        }
    }
    
    /**
     * Liefert den Maximalwert bei dem die Beschriftung angezeigt wird.
     */
    public int getMaxScale() {
        return style.getMaxScale();
    }
    
    /**
     * Setzt die minimale Skalierung, bei der die Features noch beschriftet sind.
     * @param max neue min. Skalierung
     */
    public void setMinScale(int min) {
        style.setMinScale(min);
        if (!txtMin.getText().equals(min+"")) {
            txtMin.setText(min+"");
        }
    }
    
    /**
     * Liefert den Minimalwert bei dem die Beschriftung noch angezeigt wird.
     */
    public int getMinScale() {
        return style.getMinScale();
    }
    
    /**
     * Setzt die Beschriftung der Features (Groovy)
     * @param attrib neuer String
     */
    public void setLabelAttribute(String attrib) {
        style.setAttribute(attrib);
        cbbAttribute.setSelectedItem(attrib);
    }
    
    /**
     * Liefert den Text der Beschriftung der Features.
     */
    public String getLabelAttribute() {
        return (style.getAttribute() == null) ? "" : style.getAttribute();
    }
    
    /**
     * Setzt die horizontale Ausrichtung der Beschriftung.
     * @param align Float-Wert der neuen Ausrichtung
     */
    public void setAlignment(float align) {
        style.setAlignment(align);
        if (align == JLabel.LEFT_ALIGNMENT) {
            radLeft.setSelected(true);
        } else if (align == JLabel.CENTER_ALIGNMENT) {
            radCenter.setSelected(true);
        } else {
            radRight.setSelected(true);
        }
    }
    
    /**
     * Liefert die Ausrichtung der Beschriftung.
     */
    public float getAlignment() {
        return style.getAlignment();
    }
    
    /**
     * Setzt den neuen Scaling-Multiplier der Beschriftung.
     * @param multi neuer Multiplier
     */
    public void setMultiplier(Object multi) {
        if (multi instanceof Double) {
            style.setMultiplier((Double)multi);
        } else if (multi instanceof Long){
            style.setMultiplier(new Double((Long)multi));
        }
        txtMultiplier.setValue(multi);
    }
    
    /**
     * Liefert den Scaling-Multiplier der Beschriftung.
     */
    public double getMultiplier() {
        Object o = style.getMultiplier();
        if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Long) {
            long l = (Long) o;
            double d = l;
            return d;
        } else {
            return 1.0d;
        }
    }
    
    /**
     * Setzt eine neue Schriftfarbe für die Featurebeschriftung.
     * @param fontColor neue Schriftfarbe
     */
    public void setFontColor(Color fontColor) {
        style.setFontColor(fontColor);
        panFontColor.setBackground(fontColor);
    }

    /**
     * Liefert die Schriftfarbe der Beschriftung.
     */
    public Color getFontColor() {
        return style.getFontColor();
    }

    /**
     * Setzt einen neuen Schrifttypus für die Featurebeschriftung.
     * @param fontType neuer Schrifttyp
     */
    public void setFontType(Font fontType) {
        style.setFont(fontType);
        StringBuffer name = new StringBuffer(fontType.getSize() + "pt ");
        name.append(fontType.getName());
        if (fontType.isBold()) {
            name.append(" Bold");
        }
        if (fontType.isItalic()) {
            name.append(" Italic");
        }
        lblFontname.setText(name.toString());
    }

    /**
     * Liefert die Schriftart der Beschriftung.
     */
    public Font getFontType() {
        return style.getFont();
    }
    
    /**
     * Bestimmt, ob die Beschriftung mitskaliert werden soll oder nicht.
     * @param flag true, falls Autoskalierung erwünscht
     */
    public void setAutoscale(boolean flag) {
        style.setAutoscale(flag);
        if (chkAutoscale.isSelected() != flag) {
            chkAutoscale.setSelected(flag);
        }
    }
    
    /**
     * Liefert true, wenn die Schriftgröße mit dem Zoomlevel skaliert wird.
     */
    public boolean getAutoscale() {
        return style.isAutoscale();
    }
    
    /**
     * Setzt ein Attribut als Primarschlüssel.
     * @param id Attributname
     */
    public void setPrimaryIdExpression(String id) {
        styleAttribHM.put(ATTRI_PRIMARY, id);
        cbbPrimary.setSelectedItem(id);
    }
    
    /**
     * Returns the attribute that is used as primaryid.
     */
    public String getPrimaryIdExpression() {
        return (String) styleAttribHM.get(ATTRI_PRIMARY);
    }
    
    /**
     * Sets a string as text in the queryEditorpane.
     * @param s querystring
     */
    public void setQueryString(String s) {
        queryEditor.setText(s);
    }
    
    /**
     * Returns the querystring of the queryEditorpane.
     */
    public String getQueryString() {
        return queryEditor.getText();
    }
    
    /**
     * Returns whether the query was changed in the queryEditorpane.
     * @return true if changed, else false
     */
    public boolean isQueryChanged() {
        return chkUseQueryString.isSelected();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            lstHistory.updateUI();
        }
        super.setVisible(b);
    }
    
// </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="Main-Methode">
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Log4J initialisieren
        Log4JQuickConfig.configure4LumbermillOnLocalhost();

        try {
            // Look&Feel auf das des Navigators setzen
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (Exception ex) {
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    StyleDialog dialog = new StyleDialog(new javax.swing.JFrame(), true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            System.exit(0);
                        }
                    });
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    log.error("ERROR", ex);
                }
            }
        });
    }
// </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

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
        cbbPointSymbol.setModel(new DefaultComboBoxModel(pointSymbolList));
        lblPointSymbolSize = new javax.swing.JLabel();
        sldPointSymbolSize = new javax.swing.JSlider();
        txtPointSymbolSize = new javax.swing.JTextField();
        panTabLabeling = new javax.swing.JPanel();
        panLabeling = new javax.swing.JPanel();
        chkActivateLabels = new javax.swing.JCheckBox();
        lblAttrib = new javax.swing.JLabel();
        cbbAttribute = new javax.swing.JComboBox();
        panLabelButtons = new javax.swing.JPanel();
        cmdChangeColor = new javax.swing.JButton();
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
        lblPrimary = new javax.swing.JLabel();
        cbbPrimary = new javax.swing.JComboBox();
        panDialogButtons = new javax.swing.JPanel();
        cmdOK = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();

        panTabRules.setLayout(new java.awt.BorderLayout());

        panRulesButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        panRulesButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        cmdAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/rule_add.png"))); // NOI18N
        cmdAdd.setText("neu");
        cmdAdd.setMargin(new java.awt.Insets(2, 5, 2, 5));
        cmdAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddActionPerformed(evt);
            }
        });
        panRulesButtons.add(cmdAdd);

        cmdRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/rule_remove.png"))); // NOI18N
        cmdRemove.setText("löschen");
        cmdRemove.setMargin(new java.awt.Insets(2, 5, 2, 5));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveActionPerformed(evt);
            }
        });
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

        panTabQuery.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10), javax.swing.BorderFactory.createTitledBorder("Query bearbeiten")));
        panTabQuery.setLayout(new java.awt.BorderLayout());

        panScrollpane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panScrollpane.setLayout(new java.awt.BorderLayout());
        panScrollpane.add(scpQuery, java.awt.BorderLayout.CENTER);

        panTabQuery.add(panScrollpane, java.awt.BorderLayout.CENTER);

        panQueryCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panQueryCheckbox.setLayout(new java.awt.GridLayout(2, 0));

        chkLinewrap.setText("Zeilenumbruch");
        chkLinewrap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLinewrapActionPerformed(evt);
            }
        });
        panQueryCheckbox.add(chkLinewrap);

        chkUseQueryString.setText("benutze dieses Benutzer-Query");
        panQueryCheckbox.add(chkUseQueryString);

        panTabQuery.add(panQueryCheckbox, java.awt.BorderLayout.SOUTH);

        setTitle("Style anpassen");
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(685, 461));
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        panMain.setMinimumSize(new java.awt.Dimension(620, 433));
        panMain.setPreferredSize(new java.awt.Dimension(620, 433));
        panMain.setLayout(new java.awt.BorderLayout());

        panInfo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createEtchedBorder()), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panInfo.setLayout(new java.awt.BorderLayout());

        panInfoComp.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/style.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panInfoComp.add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(jLabel1);
        jLabel2.setText("<html>Dieser Dialog dient der<br>Anpassung der visuellen<br>Gestaltung des WFS-Layers</html>");
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

        javax.swing.GroupLayout panPreviewLayout = new javax.swing.GroupLayout(panPreview);
        panPreview.setLayout(panPreviewLayout);
        panPreviewLayout.setHorizontalGroup(
            panPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 146, Short.MAX_VALUE)
        );
        panPreviewLayout.setVerticalGroup(
            panPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 187, Short.MAX_VALUE)
        );

        jPanel1.add(panPreview, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jPanel1, gridBagConstraints);

        lblPreview.setLabelFor(panPreview);
        lblPreview.setText("Vorschau:");
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

        panTabFill.setLayout(new java.awt.BorderLayout());

        panFill.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panFill.setLayout(new java.awt.GridBagLayout());

        chkFill.setSelected(true);
        chkFill.setText("Füllung:");
        chkFill.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkFillItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(chkFill, gridBagConstraints);

        chkFillPattern.setText("Füllmuster:");
        chkFillPattern.setEnabled(false);
        chkFillPattern.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkFillPatternItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(chkFillPattern, gridBagConstraints);

        cbbFillPattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbbFillPattern.setEnabled(false);
        cbbFillPattern.setMinimumSize(new java.awt.Dimension(56, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(cbbFillPattern, gridBagConstraints);

        chkLine.setText("Linie:");
        chkLine.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkLineItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 10);
        panFill.add(chkLine, gridBagConstraints);

        chkLinePattern.setText("Linienmuster:");
        chkLinePattern.setEnabled(false);
        chkLinePattern.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkLinePatternItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(chkLinePattern, gridBagConstraints);

        cbbLinePattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbbLinePattern.setEnabled(false);
        cbbLinePattern.setMinimumSize(new java.awt.Dimension(56, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(cbbLinePattern, gridBagConstraints);

        chkSync.setText("Linien- an Füllfarbe angleichen");
        chkSync.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
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

        chkHighlightable.setText("Geometrie unter der Maus hervorheben");
        chkHighlightable.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkHighlightableItemStateChanged(evt);
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
        lblLineWidth.setText("Liniendicke (px):");
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
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sldLineWidthMouseWheelMoved(evt);
            }
        });
        sldLineWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
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

        txtLineWidth.setText("1");
        txtLineWidth.setEnabled(false);
        txtLineWidth.setMinimumSize(new java.awt.Dimension(30, 20));
        txtLineWidth.setPreferredSize(new java.awt.Dimension(30, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(txtLineWidth, gridBagConstraints);

        lblAlpha.setLabelFor(jPanel7);
        lblAlpha.setText("Alpha (%):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        panFill.add(lblAlpha, gridBagConstraints);

        txtTransparency.setText("100");
        txtTransparency.setMinimumSize(new java.awt.Dimension(30, 20));
        txtTransparency.setPreferredSize(new java.awt.Dimension(30, 20));
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

        javax.swing.GroupLayout panTransWhiteLayout = new javax.swing.GroupLayout(panTransWhite);
        panTransWhite.setLayout(panTransWhiteLayout);
        panTransWhiteLayout.setHorizontalGroup(
            panTransWhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        panTransWhiteLayout.setVerticalGroup(
            panTransWhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        jPanel7.add(panTransWhite);

        sldAlpha.setMajorTickSpacing(10);
        sldAlpha.setMinorTickSpacing(1);
        sldAlpha.setSnapToTicks(true);
        sldAlpha.setValue(100);
        sldAlpha.setMinimumSize(new java.awt.Dimension(100, 23));
        sldAlpha.setPreferredSize(new java.awt.Dimension(100, 23));
        sldAlpha.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sldAlphaMouseWheelMoved(evt);
            }
        });
        sldAlpha.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldAlphaStateChanged(evt);
            }
        });
        jPanel7.add(sldAlpha);

        panTransColor.setBackground(new java.awt.Color(0, 180, 0));
        panTransColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        panTransColor.setMaximumSize(new java.awt.Dimension(14, 14));
        panTransColor.setMinimumSize(new java.awt.Dimension(14, 14));
        panTransColor.setPreferredSize(new java.awt.Dimension(14, 14));

        javax.swing.GroupLayout panTransColorLayout = new javax.swing.GroupLayout(panTransColor);
        panTransColor.setLayout(panTransColorLayout);
        panTransColorLayout.setHorizontalGroup(
            panTransColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        panTransColorLayout.setVerticalGroup(
            panTransColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

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

        javax.swing.GroupLayout panFillColorLayout = new javax.swing.GroupLayout(panFillColor);
        panFillColor.setLayout(panFillColorLayout);
        panFillColorLayout.setHorizontalGroup(
            panFillColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panFillColorLayout.setVerticalGroup(
            panFillColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        jPanel8.add(panFillColor);

        cmdFill.setText("...");
        cmdFill.setMaximumSize(new java.awt.Dimension(90, 18));
        cmdFill.setMinimumSize(new java.awt.Dimension(30, 18));
        cmdFill.setPreferredSize(new java.awt.Dimension(30, 18));
        cmdFill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        javax.swing.GroupLayout panLineColorLayout = new javax.swing.GroupLayout(panLineColor);
        panLineColor.setLayout(panLineColorLayout);
        panLineColorLayout.setHorizontalGroup(
            panLineColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 33, Short.MAX_VALUE)
        );
        panLineColorLayout.setVerticalGroup(
            panLineColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        jPanel9.add(panLineColor);

        cmdLine.setText("...");
        cmdLine.setEnabled(false);
        cmdLine.setMaximumSize(new java.awt.Dimension(30, 18));
        cmdLine.setMinimumSize(new java.awt.Dimension(30, 18));
        cmdLine.setPreferredSize(new java.awt.Dimension(30, 18));
        cmdLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        lblHistory.setFont(new java.awt.Font("Tahoma", 0, 9));
        lblHistory.setLabelFor(lstHistory);
        lblHistory.setText("history.xml");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        panFill.add(lblHistory, gridBagConstraints);

        lblPointSymbol.setLabelFor(cbbPointSymbol);
        lblPointSymbol.setText("Punktsymbol:");
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
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbbPointSymbolItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
        panFill.add(cbbPointSymbol, gridBagConstraints);

        lblPointSymbolSize.setLabelFor(sldPointSymbolSize);
        lblPointSymbolSize.setText("Punktsymbolgröße:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 10);
        panFill.add(lblPointSymbolSize, gridBagConstraints);

        sldPointSymbolSize.setMajorTickSpacing(10);
        sldPointSymbolSize.setMaximum(30);
        sldPointSymbolSize.setMinimum(10);
        sldPointSymbolSize.setMinorTickSpacing(1);
        sldPointSymbolSize.setPaintLabels(true);
        sldPointSymbolSize.setSnapToTicks(true);
        sldPointSymbolSize.setValue(10);
        sldPointSymbolSize.setMinimumSize(new java.awt.Dimension(130, 37));
        sldPointSymbolSize.setPreferredSize(new java.awt.Dimension(130, 37));
        sldPointSymbolSize.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sldPointSymbolSizeMouseWheelMoved(evt);
            }
        });
        sldPointSymbolSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
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

        txtPointSymbolSize.setText("10");
        txtPointSymbolSize.setMinimumSize(new java.awt.Dimension(30, 20));
        txtPointSymbolSize.setPreferredSize(new java.awt.Dimension(30, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panFill.add(txtPointSymbolSize, gridBagConstraints);

        panTabFill.add(panFill, java.awt.BorderLayout.WEST);

        tbpTabs.addTab("Füllung & Linien", new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/style_color.png")), panTabFill); // NOI18N

        panTabLabeling.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 20));

        panLabeling.setLayout(new java.awt.GridBagLayout());

        chkActivateLabels.setSelected(true);
        chkActivateLabels.setText("Labels anschalten");
        chkActivateLabels.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkActivateLabelsItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        panLabeling.add(chkActivateLabels, gridBagConstraints);

        lblAttrib.setLabelFor(cbbAttribute);
        lblAttrib.setText("Label-Attribut:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 15);
        panLabeling.add(lblAttrib, gridBagConstraints);

        cbbAttribute.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panLabeling.add(cbbAttribute, gridBagConstraints);

        panLabelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder("Schriftdetails"));
        panLabelButtons.setLayout(new java.awt.GridBagLayout());

        cmdChangeColor.setText("Farbe wechseln");
        cmdChangeColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdChangeColorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        panLabelButtons.add(cmdChangeColor, gridBagConstraints);

        cmdChangeFont.setText("Schrift wechseln");
        cmdChangeFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        javax.swing.GroupLayout panFontColorLayout = new javax.swing.GroupLayout(panFontColor);
        panFontColor.setLayout(panFontColorLayout);
        panFontColorLayout.setHorizontalGroup(
            panFontColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );
        panFontColorLayout.setVerticalGroup(
            panFontColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );

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
        lblMin.setText("Sichtbar von 1:");
        panScale.add(lblMin);

        txtMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtMin.setText("1");
        txtMin.setMinimumSize(new java.awt.Dimension(60, 20));
        txtMin.setPreferredSize(new java.awt.Dimension(60, 20));
        panScale.add(txtMin);

        lblMax.setLabelFor(txtMax);
        lblMax.setText("bis 1:");
        panScale.add(lblMax);

        txtMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
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
        chkAutoscale.setText("Größe der Skalierung anpassen");
        chkAutoscale.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
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
        radLeft.setText("links");
        radLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radLeftActionPerformed(evt);
            }
        });
        panAlignment.add(radLeft);

        btgAlignment.add(radCenter);
        radCenter.setText("zentriert");
        radCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCenterActionPerformed(evt);
            }
        });
        panAlignment.add(radCenter);

        btgAlignment.add(radRight);
        radRight.setText("rechts");
        radRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
        lblAlignment.setText("Ausrichtung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 15);
        panLabeling.add(lblAlignment, gridBagConstraints);

        lblMultiplier.setLabelFor(txtMultiplier);
        lblMultiplier.setText("Größenmultiplikator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 15);
        panLabeling.add(lblMultiplier, gridBagConstraints);

        txtMultiplier.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.00"))));
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

        tbpTabs.addTab("Beschriftung", new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/labelling.png")), panTabLabeling); // NOI18N

        panTabAttrib.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10), javax.swing.BorderFactory.createTitledBorder("Abzufragende Attribute auswählen")));
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

        lblPrimary.setLabelFor(cbbPrimary);
        lblPrimary.setText("Primärschlüssel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 10);
        jPanel2.add(lblPrimary, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(cbbPrimary, gridBagConstraints);

        panTabAttrib.add(jPanel2);

        tbpTabs.addTab("Attribute", new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/featureservice/res/attributes.png")), panTabAttrib); // NOI18N
        panTabAttrib.getAccessibleContext().setAccessibleName(null);

        panTabs.add(tbpTabs, java.awt.BorderLayout.CENTER);

        panMain.add(panTabs, java.awt.BorderLayout.CENTER);

        getContentPane().add(panMain, java.awt.BorderLayout.CENTER);

        panDialogButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, -5));
        panDialogButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));

        cmdOK.setText("OK");
        cmdOK.setMaximumSize(new java.awt.Dimension(88, 23));
        cmdOK.setMinimumSize(new java.awt.Dimension(88, 23));
        cmdOK.setPreferredSize(new java.awt.Dimension(88, 23));
        cmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdOKActionPerformed(evt);
            }
        });
        panDialogButtons.add(cmdOK);

        cmdCancel.setText("Abbrechen");
        cmdCancel.setMaximumSize(new java.awt.Dimension(88, 23));
        cmdCancel.setMinimumSize(new java.awt.Dimension(88, 23));
        cmdCancel.setPreferredSize(new java.awt.Dimension(88, 23));
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });
        panDialogButtons.add(cmdCancel);

        getContentPane().add(panDialogButtons, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
   
    // <editor-fold defaultstate="collapsed" desc="Eventhandling">
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(null);
    }//GEN-LAST:event_closeDialog

    private void cmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOKActionPerformed
        // Textfelder auslesen und in Hashmap speichern
        setLabelAttribute((cbbAttribute.getSelectedItem() == null) ? null : cbbAttribute.getSelectedItem().toString());
        setMinScale(Integer.parseInt(txtMin.getText()));
        setMaxScale(Integer.parseInt(txtMax.getText()));
        try {
            txtMultiplier.commitEdit();
        } catch (ParseException ex) {
            log.warn("Konnte kein commitEdit() durchführen", ex);
        }
        setMultiplier(txtMultiplier.getValue());

        // History neu schreiben
        if (defaultHistory != null) {
            writeHistory(defaultHistory, true);
        }

        // Falls das Feature ein AnnotatedFeature war, dann setze neue Werte
        if (isStyleFeature) {
            ((StyledFeature) feature).setFillingPaint(chkFill.isSelected() ? getFillColor() : null);
            ((StyledFeature) feature).setLinePaint(chkLine.isSelected() ? getLineColor() : null);
            ((StyledFeature) feature).setLineWidth(getLineWidth());
            ((StyledFeature) feature).setTransparency(getAlpha());
            ((StyledFeature) feature).setPointAnnotationSymbol(getPointSymbol() == null ? fas : getPointSymbol());
            ((StyledFeature) feature).setHighlightingEnabled(getHighlighting());
        }

        // Falls das Feature ein AnnotatedFeature war, dann setze neue Werte//GEN-LAST:event_cmdOKActionPerformed
        if (isAnnotatedFeature) {
            ((AnnotatedFeature) feature).setPrimaryAnnotationVisible(getLabelingEnabled());
            ((AnnotatedFeature) feature).setAutoScale(getAutoscale());
            ((AnnotatedFeature) feature).setMaxScaleDenominator(getMaxScale());
            ((AnnotatedFeature) feature).setMinScaleDenominator(getMinScale());
            ((AnnotatedFeature) feature).setPrimaryAnnotation(getLabelAttribute());
            ((AnnotatedFeature) feature).setPrimaryAnnotationJustification(getAlignment());
            ((AnnotatedFeature) feature).setPrimaryAnnotationFont(getFontType());
            ((AnnotatedFeature) feature).setPrimaryAnnotationPaint(getFontColor());
            ((AnnotatedFeature) feature).setPrimaryAnnotationScaling(getMultiplier());
        }

        if (isIdFeature) {
            ((FeatureWithId) feature).setIdExpression(getPrimaryIdExpression());
        }

        // TODO Regeln anwenden oder irgendwie setzen
        doClose(feature);
    }                                    

    private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancelActionPerformed
        doClose(null);
    }//GEN-LAST:event_cmdCancelActionPerformed

    /**
     * Schaltet die Linien-Komponenten an oder aus (Slider, Farbbutton)
     * @param flag boolean-Flag
     */
    private void switchLineActive(boolean flag) {
        cmdLine.setEnabled(flag);
        sldLineWidth.setEnabled(flag);
        txtLineWidth.setEnabled(flag);
        lblLineWidth.setEnabled(flag);
    }
    
    private void cmdAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddActionPerformed
        // TODO Regeln hinzufügen im Moment nicht benötigt, da nicht implementiert
    }//GEN-LAST:event_cmdAddActionPerformed

    private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveActionPerformed
        // TODO Regeln hinzufügen im Moment nicht benötigt, da nicht implementiert
    }//GEN-LAST:event_cmdRemoveActionPerformed

private void chkLinewrapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLinewrapActionPerformed
XMLEditorKit kit = (XMLEditorKit)queryEditor.getEditorKit();
    kit.setLineWrappingEnabled(chkLinewrap.isSelected());
    queryEditor.updateUI();
}//GEN-LAST:event_chkLinewrapActionPerformed

private void radRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radRightActionPerformed
setAlignment(JLabel.RIGHT_ALIGNMENT);
}//GEN-LAST:event_radRightActionPerformed

private void radCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCenterActionPerformed
setAlignment(JLabel.CENTER_ALIGNMENT);
}//GEN-LAST:event_radCenterActionPerformed

private void radLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLeftActionPerformed
setAlignment(JLabel.LEFT_ALIGNMENT);
}//GEN-LAST:event_radLeftActionPerformed

private void chkAutoscaleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkAutoscaleItemStateChanged
setAutoscale(chkAutoscale.isSelected());
}//GEN-LAST:event_chkAutoscaleItemStateChanged

private void cmdChangeFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdChangeFontActionPerformed
// FontChooser anzeigen und auswerten
        Font temp = getFontType();
        fontChooser.setSelectedFont(temp, temp.getSize(), temp.isBold(), temp.isItalic());
        fontChooser.setVisible(true);
        if (fontChooser.getReturnStatus() != null) {
            setFontType(fontChooser.getReturnStatus());
            updatePreview();
        }
}//GEN-LAST:event_cmdChangeFontActionPerformed

private void cmdChangeColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdChangeColorActionPerformed
// aktuelle Farbe im ColorChooser setzen
        colorChooser.setColor(getFontColor());

        // ColorChooser anzeigen und auswerten (in Actionlistenern)
        JColorChooser.createDialog(this, COLORCHOOSER_TITLE, true, colorChooser, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("neue Schriftfarbe = " + colorChooser.getColor());
                setFontColor(colorChooser.getColor());
                updatePreview();
            }
        }, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("ColorChooser abgebrochen");
            }
        }).setVisible(true);
}//GEN-LAST:event_cmdChangeColorActionPerformed

private void chkActivateLabelsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkActivateLabelsItemStateChanged
// alle Komponenten, die mit Labeling zu tun haben deaktivieren
        boolean flag = chkActivateLabels.isSelected();
        setLabelingEnabled(flag);
        lblAttrib.setEnabled(flag);
        cbbAttribute.setEnabled(flag);
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
        cmdChangeColor.setEnabled(flag);
        cmdChangeFont.setEnabled(flag);
        updatePreview();
}//GEN-LAST:event_chkActivateLabelsItemStateChanged

private void sldPointSymbolSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldPointSymbolSizeStateChanged
setPointSymbolSize(sldPointSymbolSize.getValue());
        updatePreview();
}//GEN-LAST:event_sldPointSymbolSizeStateChanged

private void sldPointSymbolSizeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sldPointSymbolSizeMouseWheelMoved
if (sldPointSymbolSize.isEnabled() && sldPointSymbolSize.isFocusOwner()) {
            sldPointSymbolSize.setValue(sldPointSymbolSize.getValue() - evt.getWheelRotation());
        }
}//GEN-LAST:event_sldPointSymbolSizeMouseWheelMoved

private void cbbPointSymbolItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbbPointSymbolItemStateChanged
// Selektion der ComboBox auswerten und Pointsymbol dementsprechend setzen
        if (pointSymbolHM.get(cbbPointSymbol.getSelectedItem()) != null) {
            setPointSymbol(cbbPointSymbol.getSelectedItem().toString());
        } else {
            setPointSymbol(Style.NO_POINTSYMBOL);
            if (fas == null) {
                fas = new FeatureAnnotationSymbol(((StylePreviewPanel) panPreview).getPointSymbol());
                fas.setSweetSpotX(0.5d);
                fas.setSweetSpotY(0.5d);
            } else {
                fas.setImage(((StylePreviewPanel) panPreview).getPointSymbol());
            }
        }
        updatePreview();
}//GEN-LAST:event_cbbPointSymbolItemStateChanged

private void cmdLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLineActionPerformed
// aktuelle Farbe im ColorChooser setzen
        colorChooser.setColor(getLineColor());

        // ColorChooser anzeigen und auswerten (in Actionlistenern)
        JColorChooser.createDialog(this, COLORCHOOSER_TITLE, true, colorChooser, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("neue Linie = " + colorChooser.getColor());
                setLineColor(true, colorChooser.getColor());
                if (chkSync.isSelected()) {
                    setFillColor(true, lighten(colorChooser.getColor()));
                }
                updatePreview();
            }
        }, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("ColorChooser abgebrochen");
            }
        }).setVisible(true);
}//GEN-LAST:event_cmdLineActionPerformed

private void cmdFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFillActionPerformed
// aktuelle Farbe im ColorChooser setzen
        colorChooser.setColor(getFillColor());
        
        // ColorChooser anzeigen und auswerten (in Actionlistenern)
        JColorChooser.createDialog(this, COLORCHOOSER_TITLE, true, colorChooser, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("neue F\u00FCllung = " + colorChooser.getColor());
                setFillColor(true, colorChooser.getColor());
                if (chkSync.isSelected()) {
                    setLineColor(true, darken(colorChooser.getColor()));
                }
                updatePreview();
            }
        }, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.debug("ColorChooser abgebrochen");
            }
        }).setVisible(true);
}//GEN-LAST:event_cmdFillActionPerformed

private void sldAlphaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldAlphaStateChanged
setAlpha(sldAlpha.getValue() / 100.0f);
        updatePreview();
}//GEN-LAST:event_sldAlphaStateChanged

private void sldAlphaMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sldAlphaMouseWheelMoved
sldAlpha.setValue(sldAlpha.getValue() - (evt.getWheelRotation() * 5));
}//GEN-LAST:event_sldAlphaMouseWheelMoved

private void sldLineWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldLineWidthStateChanged
// Nie 0 als Liniendicke zulassen
        if (sldLineWidth.getValue() == 0) {
            sldLineWidth.setValue(1);
        }
        setLineWidth(sldLineWidth.getValue());
        updatePreview();
}//GEN-LAST:event_sldLineWidthStateChanged

private void sldLineWidthMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sldLineWidthMouseWheelMoved
if (sldLineWidth.isEnabled() && sldLineWidth.isFocusOwner()) {
            sldLineWidth.setValue(sldLineWidth.getValue() - evt.getWheelRotation());
        }
}//GEN-LAST:event_sldLineWidthMouseWheelMoved

private void chkHighlightableItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkHighlightableItemStateChanged
setHighlighting(chkHighlightable.isSelected());
}//GEN-LAST:event_chkHighlightableItemStateChanged

private void chkSyncItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkSyncItemStateChanged
if (evt.getStateChange() == ItemEvent.SELECTED) {
            setLineColor(true, darken(getFillColor()));
            updatePreview();
        }
}//GEN-LAST:event_chkSyncItemStateChanged

private void chkLinePatternItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkLinePatternItemStateChanged
// momentan noch nicht benutzt oder angezeigt
//        cbbLinePattern.setEnabled((evt.getStateChange() == ItemEvent.SELECTED));
//        updatePreview();
}//GEN-LAST:event_chkLinePatternItemStateChanged

private void chkLineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkLineItemStateChanged
switchLineActive(chkLine.isSelected());
        updatePreview();
}//GEN-LAST:event_chkLineItemStateChanged

private void chkFillPatternItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkFillPatternItemStateChanged
// momentan noch nicht benutzt oder angezeigt
//        cbbFillPattern.setEnabled((evt.getStateChange() == ItemEvent.SELECTED));
//        updatePreview();
}//GEN-LAST:event_chkFillPatternItemStateChanged

private void chkFillItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkFillItemStateChanged
cmdFill.setEnabled(chkFill.isSelected());
        updatePreview();
}//GEN-LAST:event_chkFillItemStateChanged

    /**
     * Reagiert auf Änderungen der Selektion in der Historyliste.
     * @param evt ListSelectionEvent
     */
    public void valueChanged(final ListSelectionEvent evt) {
        Runnable t = new Runnable() {
            public void run() {
                try {
                    final BasicStyle s = (BasicStyle) lstHistory.getSelectedValue();
                    // Parse FillColor
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                style = (BasicStyle) s.clone();
                                setFillColor(s.isPaintFill(), s.getFillColor());
                                setLineColor(s.isPaintLine(), s.getLineColor());
                                chkSync.setSelected(false);
                                chkHighlightable.setSelected(s.isHighlightFeature());
                                setLineWidth(s.getLineWidth());
                                setAlpha(s.getAlpha());
                                setPointSymbol(s.getPointSymbol());
                                cbbPointSymbol.setSelectedItem(s.getPointSymbol());
                                if (s.getPointSymbol().equals(Style.NO_POINTSYMBOL)) {
                                    sldPointSymbolSize.setValue(s.getPointSymbolSize());
                                } else {
                                    sldPointSymbolSize.setValue(Style.MIN_POINTSYMBOLSIZE);
                                }
                                
                                setLabelingEnabled(s.isPaintLabel());
                                setFontType(s.getFont());
                                setFontColor(s.getFontColor());
                                setLabelAttribute(s.getAttribute());
                                setAlignment(s.getAlignment());
                                setMinScale(s.getMinScale());
                                setMaxScale(s.getMaxScale());
                                setMultiplier(s.getMultiplier());
                                setAutoscale(s.isAutoscale());
                                updatePreview();
                            } catch (Exception ex) {
                                log.error("Fehler beim Setzen der Attribute", ex);
                            }
                        }
                    });
                } catch (Exception ex) {
                    log.error("Fehler beim Auslesen der Attribute", ex);
                }
            }
        };
        CismetThreadPool.execute(t);
    }
    // </editor-fold>
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btgAlignment;
    private javax.swing.ButtonGroup btgGeom;
    private javax.swing.JComboBox cbbAttribute;
    private javax.swing.JComboBox cbbFillPattern;
    private javax.swing.JComboBox cbbLinePattern;
    private javax.swing.JComboBox cbbPointSymbol;
    private javax.swing.JComboBox cbbPrimary;
    private javax.swing.JCheckBox chkActivateLabels;
    private javax.swing.JCheckBox chkAutoscale;
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
    private javax.swing.JButton cmdChangeColor;
    private javax.swing.JButton cmdChangeFont;
    private javax.swing.JButton cmdFill;
    private javax.swing.JButton cmdLine;
    private javax.swing.JButton cmdOK;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblAlignment;
    private javax.swing.JLabel lblAlpha;
    private javax.swing.JLabel lblAttrib;
    private javax.swing.JLabel lblFontname;
    private javax.swing.JLabel lblHistory;
    private javax.swing.JLabel lblLineWidth;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblMultiplier;
    private javax.swing.JLabel lblPointSymbol;
    private javax.swing.JLabel lblPointSymbolSize;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JLabel lblPrimary;
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
    // End of variables declaration//GEN-END:variables
}
