package org.reactome.resource.ensembltranscript;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLTranscriptReferenceCreator extends ReferenceSequenceReferenceCreator {

    public EnsEMBLTranscriptReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("EnsEMBLTranscript", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.RNA);
    }
}
