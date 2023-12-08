package org.reactome.referencecreators;

import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.ReferenceGeneProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/30/2023
 */
public class UniProtDatabaseIdentifierReferenceCreator extends DatabaseIdentifierReferenceCreator {
    public UniProtDatabaseIdentifierReferenceCreator(String referenceName, Map<String, Set<String>> uniprotIdentifierToReferenceIdentifiers) throws IllegalArgumentException {
        super(referenceName, uniprotIdentifierToReferenceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return new ArrayList<>(
            ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers()).values()
        );
    }

    private Set<String> getUniProtIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
    }
}
