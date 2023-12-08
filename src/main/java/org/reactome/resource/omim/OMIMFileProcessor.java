package org.reactome.resource.omim;

import org.reactome.resource.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class OMIMFileProcessor extends UniProtFileProcessor {

    public OMIMFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
