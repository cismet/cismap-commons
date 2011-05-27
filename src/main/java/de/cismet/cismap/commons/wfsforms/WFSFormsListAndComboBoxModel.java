/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfsforms;

import org.apache.log4j.Priority;

import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProgressListener;
import org.deegree.model.feature.GMLFeatureCollectionDocument;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JProgressBar;

import de.cismet.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticHtmlTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WFSFormsListAndComboBoxModel extends AbstractListModel implements ComboBoxModel, FeatureProgressListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final int MAX_RETRY = 3;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final String loadingMessage = org.openide.util.NbBundle.getMessage(
            WFSFormsListAndComboBoxModel.class,
            "WFSFormsListAndComboBoxModel.loadingMessage"); // NOI18N
    private final String errorMessage = org.openide.util.NbBundle.getMessage(
            WFSFormsListAndComboBoxModel.class,
            "WFSFormsListAndComboBoxModel.errorMessage");   // NOI18N
    private final Vector<WFSFormFeature> features = new Vector<WFSFormFeature>();
    private final Vector<ActionListener> actionListener = new Vector<ActionListener>();
    private FeatureCollection fc = null;
    private int estimatedFeatureCount = -1;
    private Object selectedValue;
    private boolean started = false;
    private boolean finished = false;
    private boolean error = false;
    private final WFSFormQuery query;
    private final JProgressBar progressBar;
    private final JComponent comp;
    private int max = 0;
    private HashMap latestReplacingValues = null;
    private int retryCounter = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WFSFormsListAndComboBoxModel.
     *
     * @param   query        DOCUMENT ME!
     * @param   comp         DOCUMENT ME!
     * @param   progressBar  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public WFSFormsListAndComboBoxModel(final WFSFormQuery query, final JComponent comp, final JProgressBar progressBar)
            throws Exception {
        this(query, null, comp, progressBar);
    }

    /**
     * Creates a new WFSFormsListAndComboBoxModel object.
     *
     * @param   query            DOCUMENT ME!
     * @param   replacingValues  DOCUMENT ME!
     * @param   comp             DOCUMENT ME!
     * @param   progressBar      DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public WFSFormsListAndComboBoxModel(final WFSFormQuery query,
            final HashMap replacingValues,
            final JComponent comp,
            final JProgressBar progressBar) throws Exception {
        this.progressBar = progressBar;
        this.comp = comp;
        this.query = query;

        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    refresh(replacingValues);
                }
            };
        CismetThreadPool.execute(t);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  replacingValues  DOCUMENT ME!
     */
    public void refresh(final HashMap replacingValues) {
        this.latestReplacingValues = replacingValues;
//        log.fatal("in refresh() --> EventQueue.isDispatchThread():"+EventQueue.isDispatchThread());
        final GMLFeatureCollectionDocument gmlDocument = new GMLFeatureCollectionDocument();
        try {
            if (!started) {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            WFSFormsListAndComboBoxModel.this.comp.setEnabled(false);
                        }
                    });
                if (WFSFormsListAndComboBoxModel.this.progressBar != null) {
                    Color visible = WFSFormsListAndComboBoxModel.this.progressBar.getForeground();
                    visible = new Color(visible.getRed(), visible.getGreen(), visible.getBlue(), 255);
                    final Color visibleCopy = visible;
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                comp.setToolTipText("Empfangen..."); // NOI18N
                                WFSFormsListAndComboBoxModel.this.progressBar.setValue(0);
                                WFSFormsListAndComboBoxModel.this.progressBar.setForeground(visibleCopy);
                                WFSFormsListAndComboBoxModel.this.progressBar.setVisible(true);
                                WFSFormsListAndComboBoxModel.this.progressBar.setIndeterminate(true);
                            }
                        });
                }
                started = false;
                finished = false;
                error = false;
                String postString = query.getWfsQueryString();

                if (replacingValues != null) {
                    final Set keys = replacingValues.keySet();
                    if (log.isDebugEnabled()) {
                        log.debug("replacingValues.keySet()" + replacingValues.keySet()); // NOI18N
                    }
                    for (final Object key : keys) {
                        postString = postString.replaceAll((String)key, (String)replacingValues.get(key));
                    }
                }

                log.info("WFS Query:" + StaticHtmlTools.stringToHTMLString(postString));              // NOI18N
                final String modifiedString = new String(postString.getBytes("UTF-8"), "ISO-8859-1"); // NOI18N

                try {
                    if (log.isDebugEnabled()) {
                        log.debug("in EDT:" + EventQueue.isDispatchThread()); // NOI18N
                    }
                    final InputStream resp = WebAccessManager.getInstance()
                                .doRequest(new URL(query.getServerUrl()), modifiedString, ACCESS_METHODS.POST_REQUEST);

                    if (WFSFormsListAndComboBoxModel.this.progressBar != null) {
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    WFSFormsListAndComboBoxModel.this.progressBar.setIndeterminate(true);
                                }
                            });
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Start parsing of " + WFSFormsListAndComboBoxModel.this.query.getId()); // NOI18N
                    }
                    started = true;
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                WFSFormsListAndComboBoxModel.this.fireContentsChanged(
                                    WFSFormsListAndComboBoxModel.this,
                                    0,
                                    0);
                            }
                        });

                    final long start = System.currentTimeMillis();

                    // FileReader reader = new FileReader("request");

                    gmlDocument.load(new InputStreamReader(
                            resp,
                            Charset.forName("UTF-8")),
                        "http://dummyURL"); // NOI18N
                    // gmlDocument.load(new InputStreamReader(new
                    // FileInputStream("request"),Charset.forName("iso-8859-1")),"http://dummyURL");
                    gmlDocument.addFeatureProgressListener(this);
                    max = gmlDocument.getFeatureCount();
                    if (WFSFormsListAndComboBoxModel.this.progressBar != null) {
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    WFSFormsListAndComboBoxModel.this.progressBar.setIndeterminate(false);
                                    WFSFormsListAndComboBoxModel.this.progressBar.setMaximum(max);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Feature count: " + max); // NOI18N
                                    }
                                }
                            });
                    }
                    fc = gmlDocument.parse();
                    gmlDocument.removeFeatureProgressListener(this);
                    if (log.isDebugEnabled()) {
                        log.debug("Featurecollection " + fc);               // NOI18N
                    }
                    for (int i = 0; i < fc.size(); ++i) {
                        features.add(new WFSFormFeature(fc.getFeature(i), query));
                        if (log.isDebugEnabled()) {
                            log.debug(i + ":" + features.get(i));           // NOI18N
                        }
                    }

                    final long stop = System.currentTimeMillis();
                    if (log.isEnabledFor(Priority.INFO)) {
                        log.info(((stop - start) / 1000.0) + " Sekunden dauerte das Parsen");             // NOI18N
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Ended parsing of " + WFSFormsListAndComboBoxModel.this.query.getId()); // NOI18N
                    }
                    finished = true;
                    error = false;
                    selectedValue = null;
                    WFSFormsListAndComboBoxModel.this.fireContentsChanged(
                        WFSFormsListAndComboBoxModel.this,
                        0,
                        fc.size()
                                - 1);
                    fireActionPerformed(null);
                    if (WFSFormsListAndComboBoxModel.this.progressBar != null) {
                        Color invisible = WFSFormsListAndComboBoxModel.this.progressBar.getForeground();
                        invisible = new Color(invisible.getRed(), invisible.getGreen(), invisible.getBlue(), 0);
                        final Color invisibleCopy = invisible;
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    WFSFormsListAndComboBoxModel.this.progressBar.setForeground(invisibleCopy);
                                }
                            });
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                WFSFormsListAndComboBoxModel.this.comp.setEnabled(true);
                            }
                        });

                    comp.setToolTipText("");
                } catch (Throwable t) {
                    log.error("Error occured as sending a POST request", t); // NOI18N
                    error = true;
                    gmlDocument.removeFeatureProgressListener(this);
                    reportRetrievalError(t);
                }
            }
        } catch (Exception e) {
            log.error("Error while loading the features.", e);               // NOI18N
            gmlDocument.removeFeatureProgressListener(this);
            reportRetrievalError(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cause  DOCUMENT ME!
     */
    private void reportRetrievalError(final Throwable cause) {
        started = false;
        if (retryCounter < MAX_RETRY) {
            ++retryCounter;
            log.info("Retry " + retryCounter + " of " + MAX_RETRY); // NOI18N
            refresh(latestReplacingValues);
        } else {
            error = true;
            retryCounter = 0;
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final Color oldForeground = progressBar.getForeground();
                        progressBar.setIndeterminate(false);
                        progressBar.setForeground(Color.red);
                        progressBar.setValue(progressBar.getMaximum());
                        // refresh view -> show error message
                        final MouseAdapter retryListener = new MouseAdapter() {

                                @Override
                                public void mouseClicked(final MouseEvent e) {
                                    if (e.getClickCount() > 1) {
                                        comp.removeMouseListener(this);
                                        progressBar.setForeground(oldForeground);
                                        final Runnable t = new Runnable() {

                                                @Override
                                                public void run() {
                                                    refresh(latestReplacingValues);
                                                }
                                            };
                                        CismetThreadPool.execute(t);
                                    }
                                }
                            };
                        if (comp instanceof JComboBox) {
                            final Component c = ((JComboBox)comp).getEditor().getEditorComponent();
                            if (c != null) {
                                c.addMouseListener(retryListener);
                            }
                        } else {
                            comp.addMouseListener(retryListener);
                        }
                        if ((cause != null) && (cause.getMessage() != null)) {
                            comp.setToolTipText(cause.getMessage());
                        }
                        fireContentsChanged(this, 0, 0);
                    }
                });
        }
    }

    /**
     * Returns the value at the specified index.
     *
     * @param   index  the requested index
     *
     * @return  the value at <code>index</code>
     */
    @Override
    public Object getElementAt(final int index) {
        if (!finished) {
            return loadingMessage;
        } else if ((index > -1) && (index < features.size())) {
            return features.get(index);
        } else {
            return "Kein Ergebnis";
        }
    }

    /**
     * Returns the length of the list.
     *
     * @return  the length of the list
     */
    @Override
    public int getSize() {
        if (!finished) {
            // log.debug("Size=0");
            return 0;
        } else {
            // log.debug("Size="+features.size());
            return features.size();
        }
    }

    /**
     * Set the selected item. The implementation of this method should notify all registered <code>
     * ListDataListener</code>s that the contents have changed.
     *
     * @param  anItem  the list object to select or <code>null</code> to clear the selection
     */
    @Override
    public void setSelectedItem(final Object anItem) {
        if (log.isDebugEnabled()) {
            log.debug("setSelectedItem:" + anItem.getClass() + "::" + anItem); // NOI18N
        }
        selectedValue = anItem;
    }

    /**
     * Returns the selected item.
     *
     * @return  The selected item or <code>null</code> if there is no selection
     */
    @Override
    public Object getSelectedItem() {
        if (error) {
            return errorMessage;
        } else if (!finished) {
            return org.openide.util.NbBundle.getMessage(
                    WFSFormsListAndComboBoxModel.class,
                    "WFSFormListAndComboBoxModel.getSelectedItem().return"); // NOI18N
        } else if (getSize() == 0) {
            return "";                                                       // NOI18N
        } else {
            return selectedValue;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int countFeatures(final String s) {
        // do this with jdom, because I don't know how to do it with the sax stuff
        try {
            final SAXBuilder builder = new SAXBuilder(false);
            final Document doc = builder.build(new StringReader(s));
            final Element rootObject = doc.getRootElement();
            return rootObject.getChildren("featureMember", Namespace.getNamespace("http://www.opengis.net/gml")).size(); // NOI18N
        } catch (Exception jex) {
            log.warn("error during featurecounting", jex);                                                               // NOI18N
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getEstimatedFeatureCount() {
        return estimatedFeatureCount;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  estimatedFeatureCount  DOCUMENT ME!
     */
    public void setEstimatedFeatureCount(final int estimatedFeatureCount) {
        this.estimatedFeatureCount = estimatedFeatureCount;
    }

    @Override
    public void featureProgress(final int progress) {
        // count +=GMLFeatureCollectionDocument.PROGRESS_CONSTANT;
        if (WFSFormsListAndComboBoxModel.this.progressBar != null) {
            WFSFormsListAndComboBoxModel.this.progressBar.setValue(progress);
        } else {
            log.warn("No Progressbar in WFSGui", new CurrentStackTrace()); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void featureLoadingFinished() {
        // WFSFormsListAndComboBoxModel.this.progressBar.setValue(max);
    }

    /**
     * DOCUMENT ME!
     */
    public void featureProgress() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  a  DOCUMENT ME!
     */
    public void addActionListener(final ActionListener a) {
        actionListener.add(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  a  DOCUMENT ME!
     */
    public void removeActionListener(final ActionListener a) {
        actionListener.remove(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireActionPerformed(final ActionEvent e) {
        for (final ActionListener a : actionListener) {
            a.actionPerformed(e);
        }
    }
}
