package org.reactome.resource.ctdgene;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class CTDGeneReferenceCreator extends ReferenceSequenceReferenceCreator {

    public CTDGeneReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("CTDGene", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
