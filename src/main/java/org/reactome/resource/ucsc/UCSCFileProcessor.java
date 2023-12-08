package org.reactome.resource.ucsc;

import org.reactome.resource.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class UCSCFileProcessor extends UniProtFileProcessor {

    public UCSCFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
