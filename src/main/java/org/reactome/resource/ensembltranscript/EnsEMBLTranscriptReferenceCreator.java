package org.reactome.resource.ensembltranscript;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLTranscriptReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public EnsEMBLTranscriptReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("EnsEMBLTranscript", uniProtToResourceIdentifiers);
    }
}
