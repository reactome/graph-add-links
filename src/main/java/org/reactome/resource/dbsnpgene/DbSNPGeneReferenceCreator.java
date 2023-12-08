package org.reactome.resource.dbsnpgene;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class DbSNPGeneReferenceCreator extends ReferenceSequenceReferenceCreator {

    public DbSNPGeneReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("DbSNPGene", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
