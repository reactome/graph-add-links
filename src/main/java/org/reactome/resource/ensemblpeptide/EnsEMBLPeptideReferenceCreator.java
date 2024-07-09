package org.reactome.resource.ensemblpeptide;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLPeptideReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public EnsEMBLPeptideReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("EnsEMBLPeptide", uniProtToResourceIdentifiers);
    }
}
