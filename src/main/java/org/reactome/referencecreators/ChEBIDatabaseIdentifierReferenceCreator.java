package org.reactome.referencecreators;

import org.reactome.graphnodes.IdentifierNode;
import org.reactome.graphnodes.ReferenceGeneProduct;
import org.reactome.graphnodes.ReferenceMolecule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/30/2023
 */
public class ChEBIDatabaseIdentifierReferenceCreator extends DatabaseIdentifierReferenceCreator {
    public ChEBIDatabaseIdentifierReferenceCreator(String referenceName, Map<String, Set<String>> chebiIdentifierToReferenceIdentifiers) throws IllegalArgumentException {
        super(referenceName, chebiIdentifierToReferenceIdentifiers);
    }

    @Override
    protected List<IdentifierNode> getIdentifierNodes() {
        return new ArrayList<>(
            ReferenceMolecule.fetchReferenceMoleculesForChEBIIdentifiers(getChEBIIdentifiers()).values()
        );
    }

    private Set<String> getChEBIIdentifiers() {
        return getSourceIdentifierToReferenceIdentifiers().keySet();
    }
}
