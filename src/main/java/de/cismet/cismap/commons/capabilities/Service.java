package de.cismet.cismap.commons.capabilities;

/**
 * The service interface provides the contact information of wms and wfs capabilities response
 * documents.
 * This interface should be used to eliminate the deegree dependency for
 * the capabilities parsing.
 *
 * @author therter
 */
public interface Service {
    public String[] getKeywordList();
    public String getAbstract();
    public String getTitle();
    public String getName();
    public String getContactPerson();
    public String getContactOrganization();
    public String getFees();
    public String getAccessConstraints();
}
