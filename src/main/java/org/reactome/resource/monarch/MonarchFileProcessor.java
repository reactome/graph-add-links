package org.reactome.resource.monarch;

import org.reactome.fileprocessors.UniProtFileProcessor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/22/2023
 */
public class MonarchFileProcessor extends UniProtFileProcessor {

    public MonarchFileProcessor(Path filePath) throws IOException {
        super(filePath);
    }
}
