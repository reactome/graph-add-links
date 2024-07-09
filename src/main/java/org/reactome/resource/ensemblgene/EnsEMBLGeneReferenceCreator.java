package org.reactome.resource.ensemblgene;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLGeneReferenceCreator extends ReferenceSequenceReferenceCreator {

    public EnsEMBLGeneReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("EnsEMBLGene", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
