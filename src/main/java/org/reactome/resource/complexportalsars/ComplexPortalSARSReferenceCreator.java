package org.reactome.resource.complexportal.sars;

import org.reactome.graphnodes.IdentifierNode;
import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class ComplexPortalSARSReferenceCreator extends DatabaseIdentifierReferenceCreator {

    public ComplexPortalSARSReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ComplexPortalSARS", uniProtToResourceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return null;
    }
}
