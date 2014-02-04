/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.wms.capabilities.deegree;

import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.owscommon_new.DomainType;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.commons.wms.capabilities.Parameter;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeParameter implements Parameter {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.owscommon_new.Parameter parameter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeParameter object.
     *
     * @param  parameter  DOCUMENT ME!
     */
    public DeegreeParameter(final org.deegree.owscommon_new.Parameter parameter) {
        this.parameter = parameter;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        if (parameter instanceof DomainType) {
            return ((DomainType)parameter).getName().getLocalName();
        }

        return null;
    }

    @Override
    public List<String> getAllowedValues() {
        final List<String> result = new ArrayList<String>();

        if (parameter instanceof DomainType) {
            final DomainType dt = (DomainType)parameter;

            for (final TypedLiteral tl : dt.getValues()) {
                result.add(tl.getValue());
            }
        }

        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof DeegreeParameter) {
            final DeegreeParameter other = (DeegreeParameter)obj;

            return getName().equals(other.getName());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (67 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
        return hash;
    }
}
