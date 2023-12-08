package org.reactome.resource.ncbigene;

import org.reactome.resource.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class NCBIGeneFileProcessor extends UniProtFileProcessor {

    public NCBIGeneFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
