/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.metasearch;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class SearchTopic extends AbstractAction implements Comparable<SearchTopic> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SearchTopic.class);

    private static final String PATH_TO_ICONS = "/de/cismet/cismap/commons/gui/metasearch/";
    public static final String SELECTED = "selected";

    //~ Instance fields --------------------------------------------------------

    private Collection<SearchClass> searchClasses;
    private String name;
    private String description;
    private String key;
    private String iconName;
    private ImageIcon icon;
    private boolean selected;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchTopic object.
     *
     * @param  name         DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  key          DOCUMENT ME!
     * @param  iconName     DOCUMENT ME!
     * @param  selected     DOCUMENT ME!
     */
    public SearchTopic(final String name,
            final String description,
            final String key,
            final String iconName,
            final boolean selected) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.iconName = iconName;
        this.selected = selected;

        searchClasses = new LinkedHashSet<SearchClass>();

        final URL urlToIcon = getClass().getResource(PATH_TO_ICONS + this.iconName);

        if (urlToIcon != null) {
            this.icon = new ImageIcon(urlToIcon);
            putValue(SMALL_ICON, this.icon);
        } else {
            this.icon = new ImageIcon();
        }

        putValue(SHORT_DESCRIPTION, this.description);
        putValue(ACTION_COMMAND_KEY, this.key);
        putValue(NAME, this.name);
        putValue(SELECTED_KEY, this.selected);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Add a new search class to this search topic. The given search class won't be added if it's already added to this search topic.
     *
     * @param  searchClass  The search class to add.
     */
    public void insert(final SearchClass searchClass) {
        if (searchClass == null) {
            return;
        }

        if (!searchClasses.contains(searchClass)) {
            searchClasses.add(searchClass);
        } else {
            LOG.warn("Search class with domain '" + searchClass.getCidsDomain() + "' and table '"
                        + searchClass.getCidsClass() + "' already exists in search topic '" + getName()
                        + "'. The search class won't be added twice.");
        }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() instanceof AbstractButton) {
            final boolean oldValue = selected;
            selected = ((AbstractButton)event.getSource()).isSelected();
            putValue(SELECTED_KEY, selected);
            firePropertyChange(SELECTED, oldValue, selected);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIconName() {
        return iconName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKey() {
        return key;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchClass> getSearchClasses() {
        return searchClasses;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selected  DOCUMENT ME!
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
        putValue(SELECTED_KEY, this.selected);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SearchTopic)) {
            return false;
        }

        final SearchTopic other = (SearchTopic)obj;

        if ((this.name == null) ? (other.name != null) : (!this.name.equals(other.name))) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : (!this.description.equals(other.description))) {
            return false;
        }
        if ((this.key == null) ? (other.key != null) : (!this.key.equals(other.key))) {
            return false;
        }
        if ((this.iconName == null) ? (other.iconName != null) : (!this.iconName.equals(other.iconName))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (11 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        hash = (11 * hash) + ((this.description != null) ? this.description.hashCode() : 0);
        hash = (11 * hash) + ((this.key != null) ? this.key.hashCode() : 0);
        hash = (11 * hash) + ((this.iconName != null) ? this.iconName.hashCode() : 0);

        return hash;
    }

    @Override
    public int compareTo(final SearchTopic o) {
        if (o == null) {
            return 1;
        }

        return name.compareTo(o.name);
    }
}
