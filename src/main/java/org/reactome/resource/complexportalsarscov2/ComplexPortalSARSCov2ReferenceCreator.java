package org.reactome.resource.complexportal.sarscov2;

import org.reactome.graphnodes.IdentifierNode;
import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class ComplexPortalSARSCov2ReferenceCreator extends DatabaseIdentifierReferenceCreator {

    public ComplexPortalSARSCov2ReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ComplexPortalSARSCov2", uniProtToResourceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return null;
    }
}
