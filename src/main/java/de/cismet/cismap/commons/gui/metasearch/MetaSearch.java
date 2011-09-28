/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.metasearch;

import Sirius.server.middleware.types.MetaClass;

import org.apache.log4j.Logger;

import org.jdom.Element;

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class MetaSearch implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MetaSearch.class);

    private static final String CONF_METASEARCH = "metaSearch";
    private static final String CONF_SEARCHTOPIC = "searchTopic";
    private static final String CONF_SEARCHTOPIC_ATTR_NAME = "name";
    private static final String CONF_SEARCHTOPIC_ATTR_DESCRIPTION = "description";
    private static final String CONF_SEARCHTOPIC_ATTR_KEY = "key";
    private static final String CONF_SEARCHTOPIC_ATTR_ICON = "icon";
    private static final String CONF_SEARCHTOPIC_ATTR_SELECTED = "selected";
    private static final String CONF_SEARCHCLASS = "searchClass";
    private static final String CONF_SEARCHCLASS_ATTR_DOMAIN = "domain";
    private static final String CONF_SEARCHCLASS_ATTR_CIDSCLASS = "cidsClass";

    private static MetaSearch instance;

    //~ Instance fields --------------------------------------------------------

    private Collection<SearchTopic> searchTopics;
    private MetaClassCacheService metaClassCacheService;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaSearch object.
     */
    private MetaSearch() {
        searchTopics = new LinkedList<SearchTopic>();
        metaClassCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MetaSearch instance() {
        if (instance == null) {
            instance = new MetaSearch();
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchTopic> getSearchTopics() {
        return searchTopics;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchTopic> getSelectedSearchTopics() {
        final List<SearchTopic> result = new LinkedList<SearchTopic>();

        for (final SearchTopic searchTopic : searchTopics) {
            if (searchTopic.isSelected()) {
                result.add(searchTopic);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SearchClass> getSelectedSearchClasses() {
        final List<SearchClass> result = new LinkedList<SearchClass>();

        for (final SearchTopic searchTopic : searchTopics) {
            if (searchTopic.isSelected()) {
                for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
                    result.add(searchClass);
                }
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<String> getSelectedSearchClassesForQuery() {
        final List<String> result = new LinkedList<String>();

        if (metaClassCacheService == null) {
            LOG.error(
                "There is no MetaClassCacheService available. Conversion from table names to CidsClass ids is not possible.");
            return result;
        }

        for (final SearchTopic searchTopic : searchTopics) {
            if (!searchTopic.isSelected()) {
                continue;
            }

            for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
                final MetaClass metaClass = metaClassCacheService.getMetaClass(searchClass.getCidsDomain(),
                        searchClass.getCidsClass());
                if (metaClass != null) {
                    result.add(metaClass.getKey().toString());
                } else {
                    LOG.error("Could not convert searchClass '" + searchClass.toString()
                                + "' to a MetaClass. This searchClass will not be included in the search.");
                }
            }
        }

        return result;
    }

    @Override
    public void configure(final Element parent) {
        if (parent == null) {
            LOG.info("There is no local configuration for meta search.");
            return;
        }

        final Element metaSearch = parent.getChild(CONF_METASEARCH);
        if (metaSearch == null) {
            LOG.info("There is no local configuration for meta search.");
            return;
        }

        final List<Element> searchTopicElements = getChildren(metaSearch, CONF_SEARCHTOPIC);
        if (searchTopicElements.isEmpty()) {
            LOG.info("There is no local configuration for meta search.");
            return;
        }

        for (final Element searchTopicElement : searchTopicElements) {
            final String name = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_NAME);
            final String description = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_DESCRIPTION);
            final String key = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_KEY);
            final String icon = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_ICON);
            final String selected = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_SELECTED);

            if ((name == null) || (name.trim().length() == 0)) {
                LOG.info("There is a search topic without a valid name. Description: '" + description + "', key: '"
                            + key + "', icon: '" + icon + "', selected: '" + selected + "'.");
                continue;
            }

            boolean isSelected = true;
            if ((selected != null) && (selected.trim().length() > 0)
                        && ("false".equalsIgnoreCase(selected) || "0".equalsIgnoreCase(selected))) {
                isSelected = false;
            }

            final SearchTopic searchTopic = new SearchTopic(name, description, key, icon, isSelected);

            for (final SearchTopic searchTopicFromServer : searchTopics) {
                if (searchTopicFromServer.equals(searchTopic)) {
                    searchTopicFromServer.setSelected(isSelected);
                }
            }
        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        if (parent == null) {
            LOG.warn("The meta search isn't configured.");
            return;
        }

        final Element metaSearch = parent.getChild(CONF_METASEARCH);
        if (metaSearch == null) {
            LOG.warn("The meta search isn't configured.");
            return;
        }

        final List<Element> searchTopicElements = getChildren(metaSearch, CONF_SEARCHTOPIC);
        if (searchTopicElements.isEmpty()) {
            LOG.warn("There are no search topics specified. Add '" + CONF_SEARCHTOPIC
                        + "' tags to specify search topics.");
            return;
        }

        for (final Element searchTopicElement : searchTopicElements) {
            final String name = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_NAME);
            final String description = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_DESCRIPTION);
            final String key = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_KEY);
            final String icon = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_ICON);
            final String selected = searchTopicElement.getAttributeValue(CONF_SEARCHTOPIC_ATTR_SELECTED);

            if ((name == null) || (name.trim().length() == 0)) {
                LOG.warn("There is a search topic without a valid name. Description: '" + description + "', key: '"
                            + key + "', icon: '" + icon + "', selected: '" + selected + "'.");
                continue;
            }

            boolean isSelected = true;
            if ((selected != null) && (selected.trim().length() > 0)
                        && ("false".equalsIgnoreCase(selected) || "0".equalsIgnoreCase(selected))) {
                isSelected = false;
            }

            final SearchTopic searchTopic = new SearchTopic(name, description, key, icon, isSelected);

            final List<Element> searchClassElements = getChildren(searchTopicElement, CONF_SEARCHCLASS);
            if (searchClassElements.isEmpty()) {
                LOG.warn("There are no search classes specified for search topic '" + searchTopic.getName()
                            + "'. This topic will be skipped.");
                continue;
            }

            for (final Element searchClassElement : searchClassElements) {
                final String domain = searchClassElement.getAttributeValue(CONF_SEARCHCLASS_ATTR_DOMAIN);
                final String table = searchClassElement.getAttributeValue(CONF_SEARCHCLASS_ATTR_CIDSCLASS);

                if ((domain == null) || (domain.trim().length() == 0) || (table == null)
                            || (table.trim().length() == 0)) {
                    LOG.warn("Search topic '" + searchTopic.getName()
                                + "' contains at least one invalid search class. Domain: '" + domain + "', table: "
                                + table + "'. This search class will be skipped.");
                    continue;
                }

                final SearchClass searchClass = new SearchClass(domain, table);
                searchTopic.insert(searchClass);
            }

            if (!searchTopics.contains(searchTopic)) {
                searchTopics.add(searchTopic);
            } else {
                LOG.warn("Search topic '" + searchTopic.getName()
                            + "' already exists. The search topic won't be added twice.");
            }
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element result = new Element(CONF_METASEARCH);

        for (final SearchTopic searchTopic : searchTopics) {
            final Element searchTopicElement = new Element(CONF_SEARCHTOPIC);

            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_NAME, searchTopic.getName());
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_DESCRIPTION, searchTopic.getDescription());
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_KEY, searchTopic.getKey());
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_ICON, searchTopic.getIconName());
            searchTopicElement.setAttribute(CONF_SEARCHTOPIC_ATTR_SELECTED, Boolean.toString(searchTopic.isSelected()));

//            for (final SearchClass searchClass : searchTopic.getSearchClasses()) {
//                final Element searchClassElement = new Element(CONF_SEARCHCLASS);
//
//                searchClassElement.setAttribute(CONF_SEARCHCLASS_ATTR_DOMAIN, searchClass.getCidsDomain());
//                searchClassElement.setAttribute(CONF_SEARCHCLASS_ATTR_CIDSCLASS, searchClass.getCidsClass());
//
//                searchTopicElement.addContent(searchClassElement);
//            }

            result.addContent(searchTopicElement);
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parent       DOCUMENT ME!
     * @param   childrenTag  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<Element> getChildren(final Element parent, final String childrenTag) {
        final List<Element> result = new LinkedList<Element>();

        final List children = parent.getChildren(childrenTag);
        if (children != null) {
            for (final Object child : children) {
                if (child instanceof Element) {
                    result.add((Element)child);
                }
            }
        }

        return result;
    }
}
