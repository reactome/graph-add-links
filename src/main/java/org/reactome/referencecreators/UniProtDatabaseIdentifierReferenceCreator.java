package org.reactome.referencecreators;

import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.ReferenceGeneProduct;

import java.util.*;
import java.util.stream.Collectors;

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
        return ReferenceGeneProduct.fetchReferenceGeneProductsForUniProtIdentifiers(getUniProtIdentifiers())
            .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<String> getUniProtIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
    }
}
