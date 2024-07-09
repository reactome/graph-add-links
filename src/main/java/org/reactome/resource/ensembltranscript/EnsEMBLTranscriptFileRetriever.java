package org.reactome.resource.ensembltranscript;

import org.reactome.otheridentifiers.retrievers.EnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLTranscriptFileRetriever extends EnsEMBLBioMartUniProtRetriever {

    public EnsEMBLTranscriptFileRetriever() {
        super("EnsEMBLTranscript");
    }
}
