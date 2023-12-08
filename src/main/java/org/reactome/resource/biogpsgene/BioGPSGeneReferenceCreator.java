package org.reactome.resource.biogpsgene;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class BioGPSGeneReferenceCreator extends ReferenceSequenceReferenceCreator {

    public BioGPSGeneReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("BioGPSGene", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
