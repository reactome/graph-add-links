package org.reactome.resource.kegg;

import org.reactome.resource.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/27/2023
 */
public class KEGGFileProcessor extends UniProtFileProcessor {

    public KEGGFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
