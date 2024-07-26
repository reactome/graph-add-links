package org.reactome.resource.ensembltranscript;

import org.reactome.retrievers.MultipleFileEnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLTranscriptFileRetriever extends MultipleFileEnsEMBLBioMartUniProtRetriever {

    public EnsEMBLTranscriptFileRetriever() {
        super("EnsEMBLTranscript");
    }
}
