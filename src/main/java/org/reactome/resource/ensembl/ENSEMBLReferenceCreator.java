package org.reactome.resource.ensembl;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class EnsemblReferenceCreator extends ReferenceSequenceReferenceCreator {

    public EnsemblReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("ENSEMBL", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
