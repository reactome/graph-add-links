package org.reactome.resource.refseqpeptide;

import org.reactome.fileprocessors.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class RefSeqPeptideFileProcessor extends UniProtFileProcessor {

    public RefSeqPeptideFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
